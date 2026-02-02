package com.coastalsocial.app.data.repository

import android.content.Context
import android.net.Uri
import com.coastalsocial.app.data.api.ApiService
import com.coastalsocial.app.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun getUserProfile(username: String): Result<User> {
        return try {
            val response = apiService.getUserProfile(username)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.user!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Profil laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        fullName: String? = null,
        bio: String? = null,
        location: String? = null,
        website: String? = null,
        isPrivate: Boolean? = null
    ): Result<Unit> {
        return try {
            val body = mutableMapOf<String, Any?>()
            fullName?.let { body["fullName"] = it }
            bio?.let { body["bio"] = it }
            location?.let { body["location"] = it }
            website?.let { body["website"] = it }
            isPrivate?.let { body["isPrivate"] = it }

            val response = apiService.updateProfile(body)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Update fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(uri: Uri): Result<String> {
        return try {
            val mimeType = getMimeType(uri) ?: "image/jpeg"
            val extension = getExtensionFromMimeType(mimeType)
            val file = getFileFromUri(uri, extension)
            val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("profilePicture", file.name, requestBody)

            val response = apiService.uploadProfilePicture(part)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.get("profilePicture") ?: "")
            } else {
                Result.failure(Exception("Upload fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadCoverPicture(uri: Uri): Result<String> {
        return try {
            val mimeType = getMimeType(uri) ?: "image/jpeg"
            val extension = getExtensionFromMimeType(mimeType)
            val file = getFileFromUri(uri, extension)
            val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("coverPicture", file.name, requestBody)

            val response = apiService.uploadCoverPicture(part)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.get("coverPicture") ?: "")
            } else {
                Result.failure(Exception("Upload fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.users ?: emptyList())
            } else {
                Result.failure(Exception("Suche fehlgeschlagen"))
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
