package com.collage.empowermentstrishakti.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.GroupViewModel

class GroupViewModelFactory(private val repository: GroupRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupViewModel(repository) as T
    }
}
