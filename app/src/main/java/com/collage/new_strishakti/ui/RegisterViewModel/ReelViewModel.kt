package com.collage.new_strishakti.ui.RegisterViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.repository.ReelRepository
import kotlinx.coroutines.flow.Flow

class ReelViewModel(
    private val repository: ReelRepository
) : ViewModel() {

    fun getReels(): Flow<PagingData<Reel>> {
        return repository.getAllReels()
            .cachedIn(viewModelScope) // keeps data cached during lifecycle
    }
}