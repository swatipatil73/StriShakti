package com.collage.empowermentstrishakti.data.model.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.HomeRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.HomeViewModel

class HomeViewModelFactory(private val repository: HomeRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
