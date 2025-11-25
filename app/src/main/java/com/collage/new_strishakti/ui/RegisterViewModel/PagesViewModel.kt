package com.collage.new_strishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.PageDetail
import com.collage.new_strishakti.data.repository.PagesRepository
import kotlinx.coroutines.launch


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

    private var currentPageIndex = 0
    private var pageSize = 10
    private var isRequestInFlight = false

    /**
     * Call to load the first page (resets state).
     */
    fun loadInitial(userId: Int, token: String, size: Int = 5) {
        if (isRequestInFlight) return
        currentPageIndex = 0
        pageSize = size
        _isInitialLoading.value = true
        _error.value = null
        _items.value = emptyList()
        fetchPage(userId, token, isInitial = true)
    }

    /**
     * Call to load the next page (if available).
     */
    fun loadNext(userId: Int, token: String) {
        if (isRequestInFlight) return
        if (hasNextPage.value != true && currentPageIndex != 0) return // nothing to load
        fetchPage(userId, token, isInitial = false)
    }

    private fun fetchPage(userId: Int, token: String, isInitial: Boolean) {
        viewModelScope.launch {
            try {
                isRequestInFlight = true
                _isLoading.value = !isInitial
                if (isInitial) _isInitialLoading.value = true

                val response = repo.fetchPages(userId = userId, page = currentPageIndex, size = pageSize, token = token)
                if (response.isSuccessful) {
                    val body = response.body()
                    val newPages = body?.pagesDetail ?: emptyList()

                    // merge lists
                    val current = _items.value?.toMutableList() ?: mutableListOf()
                    current.addAll(newPages)
                    _items.value = current

                    // pagination meta
                    _hasNextPage.value = body?.hasNextPage ?: false
                    // nextPageNo in API is explicit; prefer that if provided
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

    /**
     * Helper to update single item (e.g., follow state) after API success.
     * Replace item with same pagesId/puuid.
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
}