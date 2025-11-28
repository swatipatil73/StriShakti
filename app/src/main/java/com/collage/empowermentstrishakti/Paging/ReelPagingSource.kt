package com.collage.empowermentstrishakti.Paging




import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.collage.empowermentstrishakti.data.model.Reel.Reel
import com.collage.empowermentstrishakti.data.model.Reel.ReelResponse
import com.collage.empowermentstrishakti.data.network.ApiService


class ReelPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, Reel>() {

    companion object {
        private const val PAGE_SIZE = 5 // 👈 fixed size per API call
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Reel> {
        return try {
            val currentPage = params.key ?: 0

            // ✅ Always request 5 reels (ignore loadSize)
            val response: ReelResponse = apiService.getReels(
                token = "Bearer $token",
                page = currentPage,
                size = PAGE_SIZE
            )

            val reels = response.postsData.map { reel ->
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

            LoadResult.Page(
                data = reels,
                prevKey = if (currentPage == 0) null else currentPage - 1,
                nextKey = if (response.hasNextPage) currentPage + 1 else null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Reel>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
