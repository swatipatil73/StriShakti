package com.collage.new_strishakti.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.data.repository.FriendRepository
import com.collage.new_strishakti.ui.RegisterViewModel.FriendListViewModel

class FriendListViewModelFactory(
    private val repository: FriendRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


