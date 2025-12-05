package com.collage.empowermentstrishakti.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.GroupListViewModel

class GroupListViewModelFactory(
    private val repository: GroupRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}