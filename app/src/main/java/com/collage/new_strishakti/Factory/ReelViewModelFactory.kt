package com.collage.new_strishakti.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.data.repository.ReelRepository
import com.collage.new_strishakti.ui.RegisterViewModel.ReelViewModel

class ReelViewModelFactory(
    private val repository: ReelRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}