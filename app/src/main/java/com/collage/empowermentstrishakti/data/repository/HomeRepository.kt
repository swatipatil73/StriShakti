package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.data.model.post.HomeFeedItem
import com.collage.empowermentstrishakti.data.model.post.HomeFeedResult
import com.collage.empowermentstrishakti.data.model.post.HomePostPage
import com.collage.empowermentstrishakti.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class HomeRepository(private val apiService: ApiService) {

    suspend fun loadInitialHomeFeed(
        userId: Long,
        token: String,
        postCategory: String? = null,
        postPageSize: Int = 2,
        reelPageSize: Int = 5
    ): Result<HomeFeedResult> = withContext(Dispatchers.IO) {
        try {
            // Start all network calls in parallel
            val announcementDeferred = async {
                try { apiService.getAnnouncement("SUP_ADMIN_ANNOUNCEMENT", token) }
                catch (e: Exception) { null }
            }
            val supAdminAdsDeferred = async {
                try { apiService.getAdsSuperAdmin(postCategory, token) }
                catch (e: Exception) { null }
            }
            val adminAdsDeferred = async {
                try { apiService.getAdsAdmin(postCategory, token) }
                catch (e: Exception) { null }
            }
            val postsDeferred = async { apiService.getHomePosts(userId, 0, postPageSize, token) }
            val reelsDeferred = async {
                try { apiService.getAllReels(0, 5, token) }
                catch (e: Exception) { null }
            }

            // Await responses safely
            val announcement = announcementDeferred.await()?.body()?.postData
            val supAdminAds = supAdminAdsDeferred.await()?.body()?.postData ?: emptyList()
            val adminAds = adminAdsDeferred.await()?.body()?.postData ?: emptyList()
            val postsResponse = postsDeferred.await()
            val reelsResponse = reelsDeferred.await()

            val posts = postsResponse.body()?.postsData ?: emptyList()
            val reels = reelsResponse?.body()?.postsData ?: emptyList()

            val homeFeedItems = mutableListOf<HomeFeedItem>()

            // Add announcement if available
            announcement?.let {
                homeFeedItems.add(HomeFeedItem.AnnouncementItem(it))
            }

            val allAds = supAdminAds + adminAds
            var adIndex = 0

            // Combine posts, ads, and reels
            posts.forEachIndexed { index, post ->
                homeFeedItems.add(HomeFeedItem.PostItem(post))

                // Insert reels every 5 posts
                if ((index + 1) % 5 == 0 && reels.isNotEmpty()) {
                    homeFeedItems.add(HomeFeedItem.ReelSection(reels))
                }

                // Insert ads every 3 posts
                if (adIndex < allAds.size && (index + 1) % 3 == 0) {
                    homeFeedItems.add(HomeFeedItem.AdItem(allAds[adIndex]))
                    adIndex++
                }
            }

            // Append remaining ads at the end
            while (adIndex < allAds.size) {
                homeFeedItems.add(HomeFeedItem.AdItem(allAds[adIndex]))
                adIndex++
            }

            Result.success(
                HomeFeedResult(
                    items = homeFeedItems,
                    nextCursor = postsResponse.body()?.nextCursor,
                    hasNextPage = postsResponse.body()?.hasNextPage == true,
                    announcement = announcement
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadMorePosts(
        userId: Long,
        token: String,
        cursor: Long,
        postPageSize: Int = 5
    ): Result<HomePostPage> = withContext(Dispatchers.IO) {
        try {
            val postsResponse = apiService.getHomePosts(userId, cursor, postPageSize, token)
            val posts = postsResponse.body()?.postsData ?: emptyList()
            val items = posts.map { HomeFeedItem.PostItem(it) }

            Result.success(
                HomePostPage(
                    items = items,
                    nextCursor = postsResponse.body()?.nextCursor,
                    hasNextPage = postsResponse.body()?.hasNextPage == true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
