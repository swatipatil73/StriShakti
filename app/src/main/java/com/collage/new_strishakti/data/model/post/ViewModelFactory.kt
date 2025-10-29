package com.collage.new_strishakti.data.model.post





import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.data.repository.HomeRepository
import com.collage.new_strishakti.ui.RegisterViewModel.HomeViewModel

class ViewModelFactory(private val repository: HomeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}


