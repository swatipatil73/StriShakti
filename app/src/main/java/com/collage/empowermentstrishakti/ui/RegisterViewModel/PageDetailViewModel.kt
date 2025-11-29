package com.collage.empowermentstrishakti.ui.RegisterViewModel



import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.collage.empowermentstrishakti.Common.FileUtil
import com.collage.empowermentstrishakti.data.model.CreatePagePostResponse
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import com.collage.empowermentstrishakti.data.model.PageDetailsResponse
import com.collage.empowermentstrishakti.data.model.PageAbout
import com.collage.empowermentstrishakti.data.model.PostDetail
import com.collage.empowermentstrishakti.data.model.Member
import com.collage.empowermentstrishakti.data.model.post.HomePostData
import com.collage.empowermentstrishakti.data.model.toPostDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File


/**
 * PageDetailViewModel with upload support (multipart).
 *
 * Important:
 * - This implementation expects PagesRepository to expose a method
 *   `suspend fun addPagePostMultipart(...): Response<CreatePagePostResponse>`
 *   and (optionally) a `val context: Context` used by FileUtil.copyUriToTempFile().
 *   If your repository does not expose context, pass a Context into the upload function
 *   or let the repository provide a helper to convert Uri -> File.
 */
class PageDetailViewModel(private val repo: PagesRepository) : ViewModel() {

    // full response
    private val _pageDetails = MutableLiveData<PageDetailsResponse?>()
    val pageDetails: LiveData<PageDetailsResponse?> = _pageDetails

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // convenience separated pieces
    private val _pageAbout = MutableLiveData<PageAbout?>()
    val pageAbout: LiveData<PageAbout?> = _pageAbout

    private val _postDetails = MutableLiveData<List<PostDetail>>(emptyList())
    val postDetails: LiveData<List<PostDetail>> = _postDetails

    private val _followers = MutableLiveData<List<Member>>(emptyList())
    val followers: LiveData<List<Member>> = _followers

    private val _hasNextPage = MutableLiveData<Boolean>(false)
    val hasNextPage: LiveData<Boolean> = _hasNextPage

    private val _nextPage = MutableLiveData<Int>(0)
    val nextPage: LiveData<Int> = _nextPage

    // ---------- upload state (new) ----------
    sealed class UploadState {
        object Idle : UploadState()
        object Uploading : UploadState()
        data class Success(val post: PostDetail) : UploadState()
        data class Error(val message: String) : UploadState()
    }

    private val _uploadState = MutableLiveData<UploadState>(UploadState.Idle)
    val uploadState: LiveData<UploadState> = _uploadState

    // ---------- network load ----------
    fun loadPageDetails(puuid: String, userId: Int, page: Int = 0, size: Int = 20, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp: Response<PageDetailsResponse> = repo.getPageDetails(puuid = puuid, userId = userId, page = page, size = size, token = token)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    _pageDetails.value = body
                    // sync convenience fields
                    _pageAbout.value = body?.pageAbout
                    _postDetails.value = body?.postDetails ?: emptyList()
                    _followers.value = body?.currentPageMembers ?: emptyList()
                    _hasNextPage.value = body?.hasNextPage ?: false
                    _nextPage.value = body?.nextPageNo ?: 0
                } else {
                    _error.value = "Server error: ${resp.code()}"
                }
            } catch (t: Throwable) {
                _error.value = t.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------- helpers for testing / optimistic UI ----------

    /** Push a full PageDetailsResponse into LiveData (useful for UI testing) */
    fun setPageDetailsForTest(resp: PageDetailsResponse) {
        _pageDetails.value = resp
        _pageAbout.value = resp.pageAbout
        _postDetails.value = resp.postDetails.toMutableList()
        _followers.value = resp.currentPageMembers
        _hasNextPage.value = resp.hasNextPage
        _nextPage.value = resp.nextPageNo
    }

    /** Prepend a new PostDetail locally (optimistic UI) */
    fun prependPostOptimistic(post: PostDetail) {
        // update posts list
        val cur = _postDetails.value?.toMutableList() ?: mutableListOf()
        cur.add(0, post)
        _postDetails.value = cur

        // update full response
        val currentResp = _pageDetails.value
        val newResp = if (currentResp != null) {
            currentResp.copy(postDetails = currentResp.postDetails.toMutableList().apply { add(0, post) })
        } else {
            PageDetailsResponse(
                currentPageMembers = emptyList(),
                hasNextPage = false,
                totalPages = 1,
                pageSize = 0,
                nextPageNo = 0,
                currentPage = 0,
                pageAbout = null,
                postDetails = listOf(post),
                totalElements = 1
            )
        }
        _pageDetails.value = newResp
    }

    /** Clear / reset */
    fun clearPageDetails() {
        _pageDetails.value = null
        _pageAbout.value = null
        _postDetails.value = emptyList()
        _followers.value = emptyList()
        _hasNextPage.value = false
        _nextPage.value = 0
    }

    /**
     * Update pageAbout locally and keep combined pageDetails consistent.
     * Use this for optimistic UI and call server API separately.
     */
    fun updatePageAboutLocally(updated: PageAbout) {
        _pageAbout.value = updated

        // update the full response if present
        val cur = _pageDetails.value
        _pageDetails.value = if (cur != null) {
            cur.copy(pageAbout = updated)
        } else {
            PageDetailsResponse(
                currentPageMembers = emptyList(),
                hasNextPage = false,
                totalPages = 1,
                pageSize = 0,
                nextPageNo = 0,
                currentPage = 0,
                pageAbout = updated,
                postDetails = emptyList(),
                totalElements = 0
            )
        }
    }

    /** Remove a follower locally (optimistic). Call server separately. */
    fun removeFollowerLocally(memberId: Int) {
        // update followers LiveData
        val cur = _followers.value?.toMutableList() ?: mutableListOf()
        val idx = cur.indexOfFirst { it.userId == memberId }
        if (idx >= 0) {
            cur.removeAt(idx)
            _followers.value = cur
        }

        // also update combined pageDetails
        val curResp = _pageDetails.value
        if (curResp != null) {
            val newMembers = curResp.currentPageMembers.toMutableList().apply {
                val j = indexOfFirst { it.userId == memberId }
                if (j >= 0) removeAt(j)
            }
            _pageDetails.value = curResp.copy(currentPageMembers = newMembers)
        }
    }

    fun replacePostByTempId(tempId: String, real: PostDetail) {
        val list = _postDetails.value?.toMutableList() ?: return
        val index = list.indexOfFirst { it.tempUuid == tempId }
        if (index >= 0) {
            list[index] = real
            _postDetails.value = list
        }
    }

    fun removePostByTempId(tempId: String) {
        val list = _postDetails.value?.toMutableList() ?: return
        val newList = list.filterNot { it.tempUuid == tempId }
        _postDetails.value = newList
    }

    // ---------- Upload method (new) ----------
    /**
     * Uploads a page post (optionally with a file Uri). This method:
     *  - posts Uploading -> Success/Error to uploadState LiveData,
     *  - replaces optimistic post (matched by optimisticTempUuid) with server result.
     *
     * @param pageAdminUserId page admin user id (server path param)
     * @param pageId page id (server path param)
     * @param postName text/title of post
     * @param postType "image" | "video" | "file" (string)
     * @param selectedUri optional Uri of chosen file (from picker)
     * @param token auth token (string) — repository may expect "Bearer <token>" or raw token
     * @param optimisticTempUuid optional temp uuid used to match optimistic item in list
     */
    /**
     * Updated uploadPagePost: accepts nullable token and optimisticLocalId (Int?)
     * Callers can pass session.getToken() (nullable) and the temp int id (e.g. Int.MIN_VALUE or optimistic.postId).
     */

    fun uploadPagePost(
        context: Context,
        pageAdminUserId: Int,
        pageId: Int,
        postName: String,
        postType: String,
        selectedUri: Uri?,
        token: String,
        optimisticLocalId: Int? // match your optimistic postId (Int.MIN_VALUE etc)
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            var tmpFile: File? = null
            try {
                if (token.isBlank()) {
                    val m = "Missing auth token"
                    _error.value = m
                    _uploadState.value = UploadState.Error(m)
                    return@launch
                }

                // Prepare text parts
                val postNameBody = postName.toRequestBody("text/plain".toMediaTypeOrNull())
                val postTypeBody = postType.toRequestBody("text/plain".toMediaTypeOrNull())
                val videoThumbBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                val hashtagBody: RequestBody? = null
                val mentionBody: RequestBody? = null

                // Prepare file parts if selectedUri provided
                val parts: List<MultipartBody.Part>? = selectedUri?.let { uri ->
                    tmpFile = FileUtil.copyUriToTempFile(context, uri)
                    if (tmpFile == null) {
                        val m = "Unable to access file for upload"
                        _error.value = m
                        _uploadState.value = UploadState.Error(m)
                        return@launch
                    }
                    val mime = FileUtil.getMimeType(context, uri) ?: "application/octet-stream"
                    val reqFile = tmpFile!!.asRequestBody(mime.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("postImage", tmpFile!!.name, reqFile)
                    listOf(part)
                }

                // Ensure Authorization header format
                val authHeader = if (token.startsWith("Bearer", ignoreCase = true)) token else "Bearer $token"

                val resp = withContext(Dispatchers.IO) {
                    repo.addPagePostMultipart(
                        pageAdminUserId = pageAdminUserId,
                        pageId = pageId,
                        postName = postNameBody,
                        postType = postTypeBody,
                        videoThumbnailUrl = videoThumbBody,
                        hashtags = hashtagBody,
                        mentionIds = mentionBody,
                        postImage = parts,
                        authorization = authHeader
                    )
                }

                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    val home = body.homePostData
                    if (home != null) {
                        val serverPost = home.toPostDetail()
                        // Replace optimistic local post (match by postId)
                        if (optimisticLocalId != null) {
                            val list = _postDetails.value?.toMutableList() ?: mutableListOf()
                            val idx = list.indexOfFirst { it.postId == optimisticLocalId }
                            if (idx >= 0) {
                                list[idx] = serverPost
                                _postDetails.value = list
                            } else {
                                val cur = _postDetails.value?.toMutableList() ?: mutableListOf()
                                cur.add(0, serverPost)
                                _postDetails.value = cur
                            }
                        } else {
                            val cur = _postDetails.value?.toMutableList() ?: mutableListOf()
                            cur.add(0, serverPost)
                            _postDetails.value = cur
                        }
                        _uploadState.value = UploadState.Success(serverPost)
                    } else {
                        val msg = body.message ?: "Empty response body"
                        _error.value = msg
                        _uploadState.value = UploadState.Error(msg)
                    }
                } else {
                    val msg = resp.errorBody()?.string() ?: resp.message()
                    _error.value = msg
                    _uploadState.value = UploadState.Error(msg ?: "Upload failed")
                }

            } catch (e: Exception) {
                val m = e.message ?: "Upload exception"
                _error.value = m
                _uploadState.value = UploadState.Error(m)
            } finally {
                try { tmpFile?.let { if (it.exists()) it.delete() } } catch (_: Exception) {}
                if (_uploadState.value is UploadState.Uploading) _uploadState.value = UploadState.Idle
            }
        }
    }

    // ---------- mapper helper (if you don't already have it) ----------
    // Converts server HomePostData -> your PostDetail model. Adjust fields as needed.
    private fun HomePostData.toPostDetail(): PostDetail {
        return PostDetail(
            postId = this.postId,
            userId = this.userId,
            userProfileImageUrl = this.userProfileImageUrl,
            userName = this.userName,
            postImageURl = this.postImageURl,
            postType = this.postType,
            videoThumbnailUrl = this.videoThumbnailUrl,
            postCreatedAt = this.postCreatedAt,
            postName = this.postName,
            totalCountOFReact = this.totalCountOFReact,
            totalComments = this.totalCountOfComments,
            description = this.description,
            // preserve tempUuid as null for real server object
            tempUuid = null,
            postSaved = false
        )
    }
}
