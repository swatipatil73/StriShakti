package com.collage.new_strishakti.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.collage.new_strishakti.Paging.ReelPagingSource
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.model.post.CommonResponse
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

    suspend fun deleteReel(reelId: Int, tokenOverride: String? = null): Result<CommonResponse> {
        val authToken = tokenOverride ?: token
        return try {
            val res = apiService.deleteReel(reelId, "Bearer $authToken")
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("Failed: ${res.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

