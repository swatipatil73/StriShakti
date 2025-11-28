package com.collage.empowermentstrishakti.Factory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.SavedPostsRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.BookmarkViewModel


class BookmarkViewModelFactory(private val repository: SavedPostsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarkViewModel::class.java)) {
            return BookmarkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
