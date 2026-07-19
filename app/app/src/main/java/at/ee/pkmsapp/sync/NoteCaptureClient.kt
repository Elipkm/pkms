package at.ee.pkmsapp.sync

import at.ee.pkmsapp.data.CaptureEntity
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class NoteCaptureClient(
    private val baseUrl: String,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun upload(capture: CaptureEntity) {
        uploadRich(capture)
    }

    private fun uploadRich(capture: CaptureEntity) {
        val payload = JSONObject()
            .put("id", capture.id)
            .put("content", capture.content)
            .put("links", JSONArray(capture.links.toValues()))
            .put("categories", JSONArray(capture.categories.toValues()))
            .put("createdAt", capture.createdAt)
            .toString()
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("capture", null, payload.toRequestBody(mediaType))

        val paths = capture.attachmentPaths.toValues()
        val names = capture.attachmentNames.toValues()
        val mimeTypes = capture.attachmentMimeTypes.toValues()
        paths.forEachIndexed { index, path ->
            val file = File(path)
            if (!file.exists()) {
                throw MissingLocalAttachmentException("Missing local attachment for capture ${capture.id}: $path")
            }
            val name = names.getOrNull(index)?.ifBlank { file.name } ?: file.name
            val mimeType = mimeTypes.getOrNull(index)?.ifBlank { "image/jpeg" } ?: "image/jpeg"
            multipart.addFormDataPart(
                "files",
                name,
                file.asRequestBody(mimeType.toMediaType()),
            )
        }

        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/captures/rich")
            .post(multipart.build())
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return
            }
            if ((response.code == 404 || response.code == 415) && capture.canUsePlainNoteEndpoint()) {
                uploadPlain(capture)
                return
            }
            throw NoteCaptureUploadException("Backend returned HTTP ${response.code}")
        }
    }

    private fun uploadPlain(capture: CaptureEntity) {
        val payload = JSONObject()
            .put("id", capture.id)
            .put("content", capture.content)
            .put("createdAt", capture.createdAt)
            .put("source", capture.source)
            .toString()
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/captures/notes")
            .post(payload.toRequestBody(mediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw NoteCaptureUploadException("Backend returned HTTP ${response.code}")
            }
        }
    }
}

private fun String?.toValues(): List<String> =
    this.orEmpty().split("\n").map { it.trim() }.filter { it.isNotEmpty() }

private fun CaptureEntity.canUsePlainNoteEndpoint(): Boolean =
    content.isNotBlank() &&
        links.toValues().isEmpty() &&
        categories.toValues().isEmpty() &&
        attachmentPaths.toValues().isEmpty()

class NoteCaptureUploadException(message: String) : RuntimeException(message)
class MissingLocalAttachmentException(message: String) : RuntimeException(message)
