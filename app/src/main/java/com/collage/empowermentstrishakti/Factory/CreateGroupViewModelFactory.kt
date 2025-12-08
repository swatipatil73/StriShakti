package com.collage.empowermentstrishakti.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.CreateGroupViewModel

class CreateGroupViewModelFactory(
    private val repository: GroupRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateGroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateGroupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
