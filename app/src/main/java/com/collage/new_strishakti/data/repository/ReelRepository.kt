package com.collage.new_strishakti.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.collage.new_strishakti.Paging.ReelPagingSource
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.network.ApiService
import kotlinx.coroutines.flow.Flow
class ReelRepository(
    private val apiService: ApiService,
    private val token: String
) {

    fun getAllReels(): Flow<PagingData<Reel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,       // API size parameter
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ReelPagingSource(apiService, token)
            }
        ).flow
    }

    suspend fun deletePost(postId: Int, tokenOverride: String? = null): Result<Unit> {
        // Use passed token if provided, else default to repository token
        val authToken = tokenOverride ?: token
        return try {
            val res = apiService.deletePost(postId, authToken)
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(res.errorBody()?.string() ?: "Delete failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
