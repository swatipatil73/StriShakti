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
}