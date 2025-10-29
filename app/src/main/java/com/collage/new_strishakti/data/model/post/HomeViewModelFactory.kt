package com.collage.new_strishakti.data.model.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.data.repository.HomeRepository
import com.collage.new_strishakti.ui.RegisterViewModel.HomeViewModel

class HomeViewModelFactory(private val repository: HomeRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
