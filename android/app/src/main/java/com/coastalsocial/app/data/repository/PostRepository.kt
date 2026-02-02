package com.coastalsocial.app.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.coastalsocial.app.data.api.ApiService
import com.coastalsocial.app.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun getFeed(page: Int = 1): Result<PostsData> {
        return try {
            val response = apiService.getFeed(page)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Feed laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPosts(username: String, page: Int = 1): Result<PostsData> {
        return try {
            val response = apiService.getUserPosts(username, page)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Posts laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPost(postId: String): Result<Post> {
        return try {
            val response = apiService.getPost(postId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.post!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Post laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(content: String?, imageUri: Uri?, privacy: String = "public"): Result<PostCreateData> {
        return try {
            val contentBody = content?.toRequestBody("text/plain".toMediaTypeOrNull())
            val privacyBody = privacy.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val mediaPart = imageUri?.let { uri ->
                val mimeType = getMimeType(uri) ?: "image/jpeg"
                val extension = getExtensionFromMimeType(mimeType)
                val file = getFileFromUri(uri, extension)
                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("postMedia", file.name, requestBody)
            }

            val response = apiService.createPost(contentBody, privacyBody, mediaPart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Post erstellen fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(postId: Int): Result<Boolean> {
        return try {
            val response = apiService.toggleLike(postId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.liked ?: false)
            } else {
                Result.failure(Exception("Like fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(postId: Int): Result<List<Comment>> {
        return try {
            val response = apiService.getComments(postId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.comments ?: emptyList())
            } else {
                Result.failure(Exception("Kommentare laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: Int, content: String): Result<Unit> {
        return try {
            val response = apiService.addComment(postId, mapOf("content" to content))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Kommentar fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val response = apiService.deletePost(postId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Löschen fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleSave(postId: Int): Result<Boolean> {
        return try {
            val response = apiService.toggleSavePost(postId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.saved ?: false)
            } else {
                Result.failure(Exception("Speichern fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    private fun getExtensionFromMimeType(mimeType: String): String {
        return when (mimeType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/gif" -> ".gif"
            "image/webp" -> ".webp"
            "video/mp4" -> ".mp4"
            "video/mpeg" -> ".mpeg"
            "video/quicktime" -> ".mov"
            else -> ".jpg"
        }
    }

    private fun getFileFromUri(uri: Uri, extension: String = ".jpg"): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}$extension")
        FileOutputStream(file).use { output ->
            inputStream?.copyTo(output)
        }
        return file
    }
}
