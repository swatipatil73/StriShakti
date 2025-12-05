package com.collage.empowermentstrishakti.data.model.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.PostActionsRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionsViewModel

class PostActionsVMFactory(
    private val repo: PostActionsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostActionsViewModel::class.java)) {
            return PostActionsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
