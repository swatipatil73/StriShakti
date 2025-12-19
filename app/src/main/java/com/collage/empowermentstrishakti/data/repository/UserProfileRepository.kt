package com.collage.empowermentstrishakti.data.repository

import android.content.Context
import android.net.Uri
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.Profile.OrgDetailsResponse
import com.collage.empowermentstrishakti.data.model.Profile.UpdateUserRequest
import com.collage.empowermentstrishakti.data.model.Profile.UpdateUserResponse
import com.collage.empowermentstrishakti.data.model.Profile.UserProfileResponse
import com.collage.empowermentstrishakti.data.model.Reel.Reel
import com.collage.empowermentstrishakti.data.network.ApiClient.apiService
import com.collage.empowermentstrishakti.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.InputStream

class UserProfileRepository(
    val api: ApiService,
    val sessionManager: SessionManager,
    private val appContext: Context // needed to read Uri streams
) {

    suspend fun getUserProfile(uuid: String): Response<UserProfileResponse> {
        val token = "Bearer ${sessionManager.getToken()}"
        return api.getUserProfile(uuid, token)
    }
    private fun createPartFromString(value: String?): RequestBody? {
        return value?.let {
            it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
    }

    private suspend fun prepareFilePart(partName: String, fileUri: Uri?): MultipartBody.Part? {
        if (fileUri == null) return null

        return withContext(Dispatchers.IO) {
            val contentResolver = appContext.contentResolver
            val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
            val fileName = queryFileName(fileUri) ?: "${System.currentTimeMillis()}"

            // read bytes (safe for typical image sizes). For very large files consider streaming approach.
            val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) return@withContext null

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, fileName, requestBody)
        }
    }

    // Helper to get filename from Uri (best-effort)
    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = appContext.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndexOpenableColumnsDisplayName()
            if (nameIndex >= 0 && it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    // Helper extension to attempt to find display name column index safely
    private fun android.database.Cursor.getColumnIndexOpenableColumnsDisplayName(): Int {
        val displayNameIndex = getColumnIndex("_display_name")
        return if (displayNameIndex >= 0) displayNameIndex else try {
            getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Call API to update user with multipart/form-data
     *
     * @param userId target user id
     * @param userDateOfBirth optional
     * @param userAddress optional
     * @param userFirstName optional
     * @param userLastName optional
     * @param orgId optional numeric id (pass as string)
     * @param subRole optional (e.g. STUDENT)
     * @param profileImageUri optional Uri to profile image
     * @param coverImageUri optional Uri to cover image
     *
     *
     */
    private fun rb(value: Any?): RequestBody? {
        return value?.toString()
            ?.toRequestBody("text/plain".toMediaTypeOrNull())
    }
    suspend fun updateUserMultipart(
        userId: Int,

        // ---------- BASIC ----------
        userDateOfBirth: String? = null,
        userAddress: String? = null,
        userFirstName: String? = null,
        userLastName: String? = null,
        orgId: Int? = null,
        subRole: String? = null,

        // ---------- ROLE FLAGS ----------
        isSwayamsiddha: Boolean? = null,
        isAdiShakti: Boolean? = null,

        // ---------- SWAYAMSIDHA ----------
        universityId: Int? = null,
        collegeId: Int? = null,
        departmentId: Int? = null,
        streamId: Int? = null,
        studyCentreId: Int? = null,
        schoolId: Int? = null,

        // ---------- ADISHAKTI ----------
        localBodyType: String? = null,
        localBodyName: String? = null,
        wardNo: String? = null,

        // ---------- FILES ----------
        profileImageUri: Uri? = null,
        coverImageUri: Uri? = null

    ): Response<UpdateUserResponse> {

        val token = sessionManager.getToken() ?: ""

        return apiService.updateUserMultipart(
            authorization = "Bearer $token",
            id = userId,

            // basic
            userDateOfBirth = rb(userDateOfBirth),
            userAddress = rb(userAddress),
            userFirstName = rb(userFirstName),
            userLastName = rb(userLastName),
            orgId = rb(orgId),
            subRole = rb(subRole),

            // role flags
            isSwayamsiddha = rb(isSwayamsiddha),
            isAdiShakti = rb(isAdiShakti),

            // swayamsidha
            universityId = rb(universityId),
            collegeId = rb(collegeId),
            departmentId = rb(departmentId),
            streamId = rb(streamId),
            studyCentreId = rb(studyCentreId),
            schoolId = rb(schoolId),

            // adishakti
            localBodyType = rb(localBodyType),
            localBodyName = rb(localBodyName),
            wardNo = rb(wardNo),

            // files
            userProfileImagePath = prepareFilePart(
                "userProfileImagePath",
                profileImageUri
            ),
            userCoverProfileImagePath = prepareFilePart(
                "userCoverProfileImagePath",
                coverImageUri
            )
        )
    }



    // keep other repos
    suspend fun getAllUserPosts(): List<Reel> {
        val token = "Bearer ${sessionManager.getToken()}"
        val response = api.getReels(token, page = 0, size = 1000)
        val posts = response.postsData ?: emptyList()
        return posts.map { reel ->
            reel.copy(
                userProfileImageUrl = reel.userProfileImageUrl ?: "",
                postType = reel.postType ?: "",
                description = reel.description ?: "",
                postImageURl = reel.postImageURl ?: "",
                userName = reel.userName ?: "",
                postUploadedAt = reel.postUploadedAt ?: "",
                videoThumbnailUrl = reel.videoThumbnailUrl ?: "",
                postName = reel.postName ?: "",
                userUUID = reel.userUUID ?: "",
                totalCountOFReact = reel.totalCountOFReact ?: 0,
                totalComments = reel.totalComments ?: 0,
                totalViews = reel.totalViews ?: 0,
                postSaved = reel.postSaved ?: false,
                userReactStatus = reel.userReactStatus ?: false,
                topComments = reel.topComments ?: emptyList(),
                commentsAndReacts = reel.commentsAndReacts ?: emptyList()
            )
        }
    }
    private fun authHeader(): String {
        // sessionManager.getToken() should return raw token (without "Bearer ")
        val token = sessionManager.getToken().orEmpty()
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    suspend fun getOrgDetails(): Response<OrgDetailsResponse> = withContext(Dispatchers.IO) {
        apiService.getOrgDetails(authHeader())
    }

    // ----------------- Update user with multipart (text fields + optional images) -----------------
    /**
     * @param userId numeric id of the user to update
     * @param textFields Map of the text fields expected by backend. Example keys:
     *  - "userDateOfBirth", "userAddress", "userFirstName", "userLastName", "orgId", "subRole"
     *  Provide only keys you want to update (partial updates).
     * @param profileImageFile optional File for profile image
     * @param coverImageFile optional File for cover image
     *
     * NOTE: backend expects image field names: "userProfileImagePath" and "userCoverProfileImagePath"
     */


        // Call API

    }

    // ----------------- Small helper to guess image mime-type from file extension -----------------
    private fun guessImageMimeType(file: File): okhttp3.MediaType? {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".png") -> "image/png".toMediaTypeOrNull()
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg".toMediaTypeOrNull()
            name.endsWith(".webp") -> "image/webp".toMediaTypeOrNull()
            else -> null
        }
    }

    // ----------------- Optional helper to create text map from UpdateUserRequest -----------------
    fun toTextFieldMapFromRequest(req: UpdateUserRequest): Map<String, String> {
        val map = mutableMapOf<String, String>()
        req.userDateOfBirth?.let { map["userDateOfBirth"] = it }
        req.userAddress?.let { map["userAddress"] = it }
        // note: images are handled as files, don't put the image path here unless backend expects a url
        req.userFirstName?.let { map["userFirstName"] = it }
        req.userLastName?.let { map["userLastName"] = it }
        req.orgId?.let { map["orgId"] = it.toString() }
        req.subRole?.let { map["subRole"] = it }
        return map
    }

