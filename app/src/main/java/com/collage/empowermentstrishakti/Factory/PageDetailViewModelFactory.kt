package com.collage.empowermentstrishakti.Factory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel


class PageDetailViewModelFactory(
    private val repo: PagesRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PageDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PageDetailViewModel(repo, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
