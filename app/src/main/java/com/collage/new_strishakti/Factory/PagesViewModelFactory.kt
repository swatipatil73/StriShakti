package com.collage.new_strishakti.Factory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.data.repository.PagesRepository
import com.collage.new_strishakti.ui.RegisterViewModel.PagesViewModel


class PagesViewModelFactory(private val repo: PagesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PagesViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
