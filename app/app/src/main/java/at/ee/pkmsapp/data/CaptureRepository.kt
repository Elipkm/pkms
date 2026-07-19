package at.ee.pkmsapp.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CaptureRepository(
    private val context: Context,
    private val dao: CaptureDao,
) {

    val pendingCount: LiveData<Int> = dao.countWithStatus(CaptureStatus.Pending)
    val syncedCount: LiveData<Int> = dao.countWithStatus(CaptureStatus.Synced)
    val latestCaptures: LiveData<List<CaptureEntity>> = dao.latest(10)

    suspend fun saveNote(content: String): Boolean = saveCapture(
        content = content,
        links = emptyList(),
        imageUris = emptyList(),
        cameraImages = emptyList(),
        categories = emptyList(),
    )

    suspend fun saveCapture(
        content: String,
        links: List<String>,
        imageUris: List<Uri>,
        cameraImages: List<Bitmap>,
        categories: List<String>,
    ): Boolean {
        val trimmed = content.trim()
        val normalizedLinks = links.map { it.trim() }.filter { it.isNotEmpty() }
        val normalizedCategories = categories.map { it.trim() }.filter { it.isNotEmpty() }
        if (trimmed.isEmpty() && normalizedLinks.isEmpty() && imageUris.isEmpty() && cameraImages.isEmpty()) {
            return false
        }

        withContext(Dispatchers.IO) {
            val attachmentFiles = copyAttachments(imageUris, cameraImages)
            dao.insert(
                CaptureEntity(
                    UUID.randomUUID().toString(),
                    trimmed,
                    nowIsoInstant(),
                    "android",
                    CaptureStatus.Pending,
                    null,
                    normalizedLinks.joinToString("\n"),
                    normalizedCategories.joinToString("\n"),
                    attachmentFiles.joinToString("\n") { it.path },
                    attachmentFiles.joinToString("\n") { it.name },
                    attachmentFiles.joinToString("\n") { it.mimeType },
                )
            )
        }
        return true
    }

    suspend fun pending(limit: Int): List<CaptureEntity> = withContext(Dispatchers.IO) {
        dao.capturesWithStatus(CaptureStatus.Pending, limit)
    }

    suspend fun markSynced(id: String) = withContext(Dispatchers.IO) {
        dao.updateStatus(id, CaptureStatus.Synced, nowIsoInstant())
    }

    suspend fun markLocalProblem(id: String) = withContext(Dispatchers.IO) {
        dao.updateStatusOnly(id, CaptureStatus.LocalProblem)
    }

    suspend fun deletePendingCapture(id: String) = withContext(Dispatchers.IO) {
        val capture = dao.findById(id) ?: return@withContext
        if (capture.status == CaptureStatus.Pending || capture.status == CaptureStatus.LocalProblem) {
            deleteLocalFiles(capture)
            dao.deleteById(id)
        }
    }

    suspend fun deleteLocalHistory(id: String) = withContext(Dispatchers.IO) {
        val capture = dao.findById(id) ?: return@withContext
        if (capture.status == CaptureStatus.Synced) {
            deleteLocalFiles(capture)
            dao.deleteById(id)
        }
    }

    private fun copyAttachments(imageUris: List<Uri>, cameraImages: List<Bitmap>): List<StoredAttachment> {
        val directory = File(context.filesDir, "capture-attachments").apply { mkdirs() }
        val stored = mutableListOf<StoredAttachment>()

        imageUris.forEachIndexed { index, uri ->
            val name = displayName(uri) ?: "image-${index + 1}.jpg"
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val target = File(directory, "${UUID.randomUUID()}-${safeFileName(name)}")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            if (target.exists()) {
                stored += StoredAttachment(target.absolutePath, name, mimeType)
            }
        }

        cameraImages.forEachIndexed { index, bitmap ->
            val name = "camera-${System.currentTimeMillis()}-${index + 1}.jpg"
            val target = File(directory, "${UUID.randomUUID()}-$name")
            target.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
            }
            stored += StoredAttachment(target.absolutePath, name, "image/jpeg")
        }

        return stored
    }

    private fun displayName(uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return uri.lastPathSegment
    }

    private fun safeFileName(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "image.jpg" }

    private fun deleteLocalFiles(capture: CaptureEntity) {
        capture.attachmentPaths.orEmpty()
            .split("\n")
            .filter { it.isNotBlank() }
            .forEach { path -> File(path).delete() }
    }
}

data class StoredAttachment(
    val path: String,
    val name: String,
    val mimeType: String,
)

fun nowIsoInstant(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date())
}
