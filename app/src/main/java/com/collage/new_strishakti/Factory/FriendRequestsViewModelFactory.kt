package com.collage.new_strishakti.Factory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.collage.new_strishakti.data.repository.FriendRepository


import com.collage.new_strishakti.ui.RegisterViewModel.FriendRequestsViewModel  // <-- IMPORTANT

class FriendRequestsViewModelFactory(
    private val repo: FriendRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendRequestsViewModel::class.java)) {
            return FriendRequestsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    // For Lifecycle 2.5+ (optional but nice to have)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return create(modelClass)
    }
}



