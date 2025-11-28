package com.collage.empowermentstrishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.PageDetail
import com.collage.empowermentstrishakti.data.model.SavedPost.CreatePageRequest
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class PagesViewModel(private val repo: PagesRepository) : ViewModel() {

    private val _items = MutableLiveData<List<PageDetail>>(emptyList())
    val items: LiveData<List<PageDetail>> = _items

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isInitialLoading = MutableLiveData(false)
    val isInitialLoading: LiveData<Boolean> = _isInitialLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _hasNextPage = MutableLiveData<Boolean>(false)
    val hasNextPage: LiveData<Boolean> = _hasNextPage

    private val _actionStatus = MutableLiveData<Pair<Boolean, String?>>(Pair(false, null))
    val actionStatus: LiveData<Pair<Boolean, String?>> = _actionStatus

    private val _isActionLoading = MutableLiveData(false)
    val isActionLoading: LiveData<Boolean> = _isActionLoading

    // mode LiveData (false = all pages, true = own pages)
    private val _showOwn = MutableLiveData<Boolean>(false)
    val showOwn: LiveData<Boolean> = _showOwn

    private val _deleteStatus = MutableLiveData<Pair<Boolean, String?>>(Pair(false, null))
    val deleteStatus: LiveData<Pair<Boolean, String?>> = _deleteStatus

    private val _isDeleteLoading = MutableLiveData(false)
    val isDeleteLoading: LiveData<Boolean> = _isDeleteLoading
    private val _createStatus = MutableLiveData<Pair<Boolean, String?>>(Pair(false, null))
    val createStatus: LiveData<Pair<Boolean, String?>> = _createStatus

    private val _isCreating = MutableLiveData(false)
    val isCreating: LiveData<Boolean> = _isCreating
    private var currentPageIndex = 0
    private var pageSize = 5
    private var isRequestInFlight = false

    // ⭐ newly created page detail
    private val _createdPage = MutableLiveData<PageDetail?>(null)
    val createdPage: LiveData<PageDetail?> = _createdPage


    fun toggleMode(userId: Int, token: String, size: Int = 5) {
        val newMode = !(_showOwn.value ?: false)
        _showOwn.value = newMode
        loadInitial(userId = userId, token = token, size = size, showOwn = newMode)
    }

    fun setMode(showOwnMode: Boolean, userId: Int, token: String, size: Int = 5) {
        _showOwn.value = showOwnMode
        loadInitial(userId = userId, token = token, size = size, showOwn = showOwnMode)
    }


    fun loadInitial(
        userId: Int,
        token: String,
        size: Int = 5,
        showOwn: Boolean = _showOwn.value ?: false
    ) {
        if (isRequestInFlight) return
        currentPageIndex = 0
        pageSize = size
        _isInitialLoading.value = true
        _error.value = null
        _items.value = emptyList()
        fetchPage(userId, token, isInitial = true, showOwn = showOwn)
    }

    fun loadNext(userId: Int, token: String) {
        if (isRequestInFlight) return
        if (_hasNextPage.value != true && currentPageIndex != 0) return // nothing to load
        fetchPage(userId, token, isInitial = false, showOwn = _showOwn.value ?: false)
    }

    private fun fetchPage(userId: Int, token: String, isInitial: Boolean, showOwn: Boolean) {
        viewModelScope.launch {
            try {
                isRequestInFlight = true
                _isLoading.value = !isInitial
                if (isInitial) _isInitialLoading.value = true

                val response =
                    repo.fetchPagesByMode(userId, currentPageIndex, pageSize, token, showOwn)
                if (response.isSuccessful) {
                    val body = response.body()
                    val newPages = body?.pagesDetail ?: emptyList()

                    // merge lists
                    val current = _items.value?.toMutableList() ?: mutableListOf()

                    current.addAll(newPages)
                    _items.value = current

                    // pagination meta
                    _hasNextPage.value = body?.hasNextPage ?: false
                    // prefer server nextPageNo if provided
                    currentPageIndex = body?.nextPageNo ?: (currentPageIndex + 1)
                } else {
                    _error.value = "Server error: ${response.code()}"
                }
            } catch (t: Throwable) {
                _error.value = t.message ?: "Unknown error"
            } finally {
                isRequestInFlight = false
                _isLoading.value = false
                _isInitialLoading.value = false
            }
        }
    }

    fun clearCreatedPage() {
        _createdPage.value = null
    }

    fun createPage(adminUserId: Int, body: CreatePageRequest, token: String) {
        if (isRequestInFlight) return

        viewModelScope.launch {
            try {
                isRequestInFlight = true
                _isCreating.value = true
                _createStatus.value = Pair(false, null)

                val resp = repo.createPage(adminUserId, body, token)

                if (resp.isSuccessful && resp.body()?.status.equals("Success", ignoreCase = true)) {
                    _createStatus.value = Pair(true, resp.body()?.message ?: "Page created")

                    // ----------------------------
                    // Extract created PageDetail
                    // ----------------------------
                    // TODO: replace the extraction below with the actual field names
                    // from your Retrofit response model. Common patterns:
                    //   resp.body()?.data?.createdPage
                    //   resp.body()?.pageDetail
                    //   resp.body()?.data?.page
                    //
                    // If your response class is strongly typed (recommended), use that:
                    //   val created = resp.body()?.data?.createdPage
                    //
                    // The block below tries a few likely fields safely; update it to match your model.
                    val created: PageDetail? = try {
                        resp.body()?.let { bodyResp ->
                            // Try common field names (edit to match your model)
                            // 1) response.data.createdPage
                            val viaDataCreated = runCatching {
                                // replace `data` and `createdPage` with your actual fields if present
                                val dataField =
                                    bodyResp::class.members.firstOrNull { it.name == "data" }
                                        ?.call(bodyResp)
                                dataField?.let { df ->
                                    df::class.members.firstOrNull { it.name == "createdPage" }
                                        ?.call(df) as? PageDetail
                                        ?: df::class.members.firstOrNull { it.name == "page" }
                                            ?.call(df) as? PageDetail
                                } as? PageDetail
                            }.getOrNull()
                            if (viaDataCreated != null) return@let viaDataCreated

                            // 2) response.pageDetail or response.createdPage directly on root
                            val direct1 =
                                bodyResp::class.members.firstOrNull { it.name == "pageDetail" }
                                    ?.call(bodyResp) as? PageDetail
                            if (direct1 != null) return@let direct1

                            val direct2 =
                                bodyResp::class.members.firstOrNull { it.name == "createdPage" }
                                    ?.call(bodyResp) as? PageDetail
                            if (direct2 != null) return@let direct2

                            // 3) If your response type already contains PageDetail as a typed field use it here
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }

                    // ----------------------------
                    // Update LiveData immediately so Activity shows the new page
                    // ----------------------------
                    if (created != null) {
                        // 1) prepend into current list so observers see it immediately
                        val cur = _items.value?.toMutableList() ?: mutableListOf()
                        cur.add(0, created)
                        _items.value = cur

                        // 2) emit single-event created page for any UI listeners
                        _createdPage.value = created
                    } else {
                        // If you can't extract full PageDetail, you can create a minimal PageDetail
                        // from the request (not ideal). Example (uncomment & adapt if needed):
                        // val minimal = PageDetail(puuid = generateTempUuid(), pageName = body.pageName, ...)
                        // prepend minimal to _items and set _createdPage = minimal
                    }

                    // OPTIONAL: If you still want to refresh from server to reconcile IDs/metadata,
                    // schedule a refresh AFTER a short delay so the immediate insert is visible.
                    // Example (uncomment if desired):
                    //
                    // delay(800) // small delay to let UI animate
                    // loadInitial(adminUserId, token, size = pageSize)

                } else {
                    _createStatus.value =
                        Pair(false, resp.body()?.message ?: "Create failed: ${resp.code()}")
                }

            } catch (t: Throwable) {
                _createStatus.value = Pair(false, t.message ?: "Unknown error")
            } finally {
                isRequestInFlight = false
                _isCreating.value = false
            }
        }
    }





    /**
     * Update a single item (e.g., follow/unfollow) — replace matching item in list.
     */
    fun updateItem(updated: PageDetail) {
        val current = _items.value?.toMutableList() ?: return
        val idx = current.indexOfFirst {
            (it.pagesId != 0 && it.pagesId == updated.pagesId) ||
                    (!it.puuid.isNullOrBlank() && it.puuid == updated.puuid)
        }
        if (idx >= 0) {
            current[idx] = updated
            _items.value = current
        }
    }




    /**
     * Follow a page.
     * - Calls repository.followPage(...)
     * - On success updates the item in _items (isPageFollowed = true)
     * - Emits actionStatus for UI feedback
     */
    fun followPage(userId: Int, pagesId: Int, token: String) {
        if (isRequestInFlight) return
        viewModelScope.launch {
            try {
                isRequestInFlight = true
                _isActionLoading.value = true
                _actionStatus.value = Pair(false, null)

                val resp = repo.followPage(userId, pagesId, token)
                if (resp.isSuccessful && resp.body()?.status.equals("Success", ignoreCase = true)) {
                    // update local list item
                    val current = _items.value?.toMutableList() ?: mutableListOf()
                    val idx = current.indexOfFirst { it.pagesId == pagesId }

                    if (idx >= 0) {
                        val old = current[idx]
                        val updated = old.copy(isPageFollowed = true)
                        current[idx] = updated
                        _items.value = current
                    }
                    _actionStatus.value = Pair(true, resp.body()?.message ?: "Followed")
                } else {
                    _actionStatus.value = Pair(false, resp.body()?.message ?: "Follow failed")
                }
            } catch (t: Throwable) {
                _actionStatus.value = Pair(false, t.message ?: "Unknown error")
            } finally {
                isRequestInFlight = false
                _isActionLoading.value = false
            }
        }
    }


    fun unfollowPage(userId: Int, pagesId: Int, pageAdminUserId: Int, token: String) {
        if (isRequestInFlight) return
        viewModelScope.launch {
            try {
                isRequestInFlight = true
                _isActionLoading.value = true
                _actionStatus.value = Pair(false, null)

                val resp = repo.unfollowPage(userId, pagesId, pageAdminUserId, token)
                if (resp.isSuccessful && resp.body()?.status.equals("Success", ignoreCase = true)) {
                    // update local list item
                    val current = _items.value?.toMutableList() ?: mutableListOf()
                    val idx = current.indexOfFirst { it.pagesId == pagesId }

                    if (idx >= 0) {
                        val old = current[idx]
                        val updated = old.copy(isPageFollowed = false)
                        current[idx] = updated
                        _items.value = current
                    }
                    _actionStatus.value = Pair(true, resp.body()?.message ?: "Unfollowed")
                } else {
                    _actionStatus.value = Pair(false, resp.body()?.message ?: "Unfollow failed")
                }
            } catch (t: Throwable) {
                _actionStatus.value = Pair(false, t.message ?: "Unknown error")
            } finally {
                isRequestInFlight = false
                _isActionLoading.value = false
            }
        }
    }

    fun deletePage(pageAdminUserId: Int, pagesId: Int, token: String) {
        if (isRequestInFlight) return
        viewModelScope.launch {
            try {
                isRequestInFlight = true
                _isDeleteLoading.value = true
                _deleteStatus.value = Pair(false, null)

                val resp: Response<CommonResponse> = repo.deletePage(pageAdminUserId, pagesId, token)
                if (resp.isSuccessful && resp.body()?.status.equals("Success", ignoreCase = true)) {
                    // remove item from list if present
                    val current = _items.value?.toMutableList() ?: mutableListOf()
                    val idx = current.indexOfFirst { it.pagesId == pagesId }
                    if (idx >= 0) {
                        current.removeAt(idx)
                        _items.value = current
                    }
                    _deleteStatus.value = Pair(true, resp.body()?.message ?: "Page deleted")
                } else {
                    // server returned non-2xx or status not success
                    _deleteStatus.value = Pair(false, resp.body()?.message ?: "Delete failed: ${resp.code()}")
                }
            } catch (t: Throwable) {
                _deleteStatus.value = Pair(false, t.message ?: "Unknown error")
            } finally {
                isRequestInFlight = false
                _isDeleteLoading.value = false
            }
        }
    }
}
