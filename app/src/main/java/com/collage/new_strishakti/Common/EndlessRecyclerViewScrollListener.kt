package com.collage.new_strishakti.Common



import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Simple endless scroll listener that triggers when user scrolls near the end.
 *
 * - threshold: how many items before the end to trigger loading next page (default 3)
 * - onLoadMore: called when we need to load next page
 *
 * Attach to RecyclerView: recyclerView.addOnScrollListener(listener)
 */
class EndlessRecyclerViewScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val threshold: Int = 3,
    private val onLoadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    private var previousTotalItemCount = 0
    private var loading = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy <= 0) return // only handle scroll down

        val totalItemCount = layoutManager.itemCount
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        // If the total item count has changed and we were loading, reset loading flag
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        // If not currently loading and within threshold, trigger load
        if (!loading && (lastVisibleItemPosition + threshold) >= totalItemCount) {
            onLoadMore()
            loading = true
        }
    }

    /**
     * Reset state (call when you refresh or load initial)
     */
    fun resetState() {
        previousTotalItemCount = 0
        loading = true
    }
}
