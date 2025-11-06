package com.collage.new_strishakti.Paging




import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.model.Reel.ReelResponse
import com.collage.new_strishakti.data.network.ApiService

class ReelPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, Reel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Reel> {
        return try {
            val currentPage = params.key ?: 0
            val pageSize = params.loadSize

            // Call API
            val response: ReelResponse = apiService.getReels(
                token = "Bearer $token",
                page = currentPage,
                size = pageSize
            )

            // Map nullable fields safely
            val reels: List<Reel> = response.postsData.map { reel ->
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

