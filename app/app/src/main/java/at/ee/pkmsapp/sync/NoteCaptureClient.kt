package at.ee.pkmsapp.sync

import at.ee.pkmsapp.data.CaptureEntity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NoteCaptureClient(
    private val baseUrl: String,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun upload(capture: CaptureEntity) {
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

class NoteCaptureUploadException(message: String) : RuntimeException(message)
