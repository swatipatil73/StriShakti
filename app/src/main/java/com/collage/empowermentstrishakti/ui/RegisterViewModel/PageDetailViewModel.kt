package com.collage.empowermentstrishakti.ui.RegisterViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.collage.empowermentstrishakti.Common.FileUtil
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.CreatePagePostResponse
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import com.collage.empowermentstrishakti.data.model.PageDetailsResponse
import com.collage.empowermentstrishakti.data.model.PageAbout
import com.collage.empowermentstrishakti.data.model.PostDetail
import com.collage.empowermentstrishakti.data.model.Member
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
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
 * PageDetailViewModel (corrected)
 *
 * Notes:
 * - UploadState.Success now includes optimisticLocalId and createdPost so the Fragment can access both.
 */
class PageDetailViewModel(
    private val repo: PagesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

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

    // --- Posts list (exposed) ---
    private val _postDetails = MutableLiveData<List<PostDetail>>(emptyList())
    val postDetails: LiveData<List<PostDetail>> = _postDetails

    private val _followers = MutableLiveData<List<Member>>(emptyList())
    val followers: LiveData<List<Member>> = _followers

    private val _hasNextPage = MutableLiveData<Boolean>(false)
    val hasNextPage: LiveData<Boolean> = _hasNextPage

    private val _nextPage = MutableLiveData<Int>(0)
    val nextPage: LiveData<Int> = _nextPage

    // update state LiveData (used by fragment/dialog)
    private val _updateState = MutableLiveData<UpdateResult>()
    val updateState: LiveData<UpdateResult> = _updateState

    // ---------- upload state (internal sealed used by this VM) ----------
    sealed class UploadState {
        object Idle : UploadState()
        object Uploading : UploadState()
        data class Success(val optimisticLocalId: Int?, val createdPost: PostDetail?) : UploadState()
        data class Error(val optimisticLocalId: Int?, val message: String) : UploadState()
    }

    private val _uploadState = MutableLiveData<UploadState>(UploadState.Idle)
    val uploadState: LiveData<UploadState> = _uploadState

    // ---------- update state sealed class ----------
    sealed class UpdateResult {
        object Loading : UpdateResult()
        data class Success(val response: CommonResponse?) : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }

    // ---------- helpers for testing / optimistic UI ----------

    fun setPageDetailsForTest(resp: PageDetailsResponse) {
        _pageDetails.value = resp
        _pageAbout.value = resp.pageAbout
        _postDetails.value = resp.postDetails.toMutableList()
        _followers.value = resp.currentPageMembers
        _hasNextPage.value = resp.hasNextPage
        _nextPage.value = resp.nextPageNo
    }

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

    fun prependPostOptimistic(post: PostDetail) {
        val cur = _postDetails.value?.toMutableList() ?: mutableListOf()
        cur.add(0, post)
        _postDetails.value = cur

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

    fun replaceOptimisticPost(optimisticLocalId: Int, serverPost: PostDetail) {
        val current = _postDetails.value?.toMutableList() ?: return
        val idx = current.indexOfFirst { it.postId == optimisticLocalId }
        if (idx >= 0) {
            current[idx] = serverPost
            _postDetails.value = current
        } else {
            current.add(0, serverPost)
            _postDetails.value = current
        }

        _pageDetails.value = _pageDetails.value?.let { resp ->
            val posts = resp.postDetails.toMutableList()
            val i = posts.indexOfFirst { it.postId == optimisticLocalId }
            if (i >= 0) posts[i] = serverPost else posts.add(0, serverPost)
            resp.copy(postDetails = posts)
        } ?: _pageDetails.value
    }

    fun removePostByLocalId(localId: Int) {
        val current = _postDetails.value?.toMutableList() ?: return
        val removed = current.removeAll { it.postId == localId }
        if (removed) _postDetails.value = current

        _pageDetails.value = _pageDetails.value?.let { resp ->
            val posts = resp.postDetails.toMutableList().apply { removeAll { it.postId == localId } }
            resp.copy(postDetails = posts)
        } ?: _pageDetails.value
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

    // ---------- network update (page update) ----------
    fun updatePageOnServer(updated: PageAbout, coverImagePart: MultipartBody.Part? = null) {
        val current = _pageAbout.value ?: return
        val token = sessionManager.getToken() ?: run {
            _updateState.postValue(UpdateResult.Error("Token missing"))
            return
        }

        viewModelScope.launch {
            _updateState.postValue(UpdateResult.Loading)

            val pageAdminId = current.adminId ?: sessionManager.getUserId()
            val pageId = current.pagesId ?: run {
                _updateState.postValue(UpdateResult.Error("Page id missing"))
                return@launch
            }

            val jwt = sessionManager.getToken() ?: ""
            val raw = sessionManager.getToken() ?: ""

            val res = repo.updatePageOnServer(
                jwtToken = jwt,
                rawToken = raw,
                pageAdminUserId = pageAdminId,
                pageId = pageId,
                pageName = updated.pageName ?: "",
                pageDescription = updated.pageDescription,
                linkUrlName = updated.linkUrlName ?: "",
                linkUrl = updated.linkUrl,
                coverImagePart = coverImagePart
            )

            if (res.isSuccess) {
                val body = res.getOrNull()
                _pageAbout.postValue(updated)
                _updateState.postValue(UpdateResult.Success(body))
            } else {
                _updateState.postValue(UpdateResult.Error(res.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }

    // ---------- network load ----------
    fun loadPageDetails(puuid: String, userId: Int, page: Int = 0, size: Int = 20, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp: Response<PageDetailsResponse> = repo.getPageDetails(
                    puidOrNullToString(puuid),
                    userId = userId,
                    page = page,
                    size = size,
                    token = token
                )

                if (resp.isSuccessful) {
                    val body = resp.body()
                    _pageDetails.value = body
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

    private fun puidOrNullToString(puuid: String?): String = puuid ?: ""

    // ---------- Upload method (multipart) ----------
    fun uploadPagePost(
        context: Context,
        pageAdminUserId: Int,
        pageId: Int,
        postName: String,
        postType: String,
        selectedUri: Uri?,
        token: String,
        optimisticLocalId: Int?
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            var tmpFile: File? = null
            try {
                if (token.isBlank()) {
                    val m = "Missing auth token"
                    _error.value = m
                    _uploadState.value = UploadState.Error(optimisticLocalId, m)
                    return@launch
                }

                val postNameBody = postName.toRequestBody("text/plain".toMediaTypeOrNull())
                val postTypeBody = postType.toRequestBody("text/plain".toMediaTypeOrNull())
                val videoThumbBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                val hashtagBody: RequestBody? = null
                val mentionBody: RequestBody? = null

                val parts: List<MultipartBody.Part>? = selectedUri?.let { uri ->
                    tmpFile = FileUtil.copyUriToTempFile(context, uri)
                    if (tmpFile == null) {
                        val m = "Unable to access file for upload"
                        _error.value = m
                        _uploadState.value = UploadState.Error(optimisticLocalId, m)
                        return@launch
                    }
                    val mime = FileUtil.getMimeType(context, uri) ?: "application/octet-stream"
                    val reqFile = tmpFile!!.asRequestBody(mime.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("postImage", tmpFile!!.name, reqFile)
                    listOf(part)
                }

                val authHeader = if (token.startsWith("Bearer", ignoreCase = true)) token else "Bearer $token"

                val resp: Response<CreatePagePostResponse> = withContext(Dispatchers.IO) {
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

                    // let type be inferred — avoids conflicts between similarly named classes
                    val home = body.homePostData
                    if (home != null) {
                        val serverPost = home.toPostDetail()

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

                        _pageDetails.value = _pageDetails.value?.let { resp0 ->
                            val posts = resp0.postDetails.toMutableList()
                            val i = posts.indexOfFirst { it.postId == optimisticLocalId }
                            if (i >= 0) posts[i] = serverPost else posts.add(0, serverPost)
                            resp0.copy(postDetails = posts)
                        } ?: _pageDetails.value

                        _uploadState.value = UploadState.Success(optimisticLocalId, serverPost)
                    } else {
                        val msg = body.message ?: "Empty response body"
                        _error.value = msg
                        _uploadState.value = UploadState.Error(optimisticLocalId, msg)
                    }
                } else {
                    val msg = resp.errorBody()?.string() ?: resp.message()
                    _error.value = msg
                    _uploadState.value = UploadState.Error(optimisticLocalId, msg ?: "Upload failed")
                }

            } catch (e: Exception) {
                val m = e.message ?: "Upload exception"
                _error.value = m
                _uploadState.value = UploadState.Error(optimisticLocalId, m)
            } finally {
                try { tmpFile?.let { if (it.exists()) it.delete() } } catch (_: Exception) {}
                if (_uploadState.value is UploadState.Uploading) _uploadState.value = UploadState.Idle
            }
        }
    }

    // ---------- mapper helper (if you don't already have it) ----------
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
            tempUuid = null,
            postSaved = false
        )
    }
}
