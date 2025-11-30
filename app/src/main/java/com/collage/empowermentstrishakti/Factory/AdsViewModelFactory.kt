package com.collage.empowermentstrishakti.Factory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.AdsRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.AdsViewModel

class AdsViewModelFactory(
    private val repo: AdsRepository = AdsRepository()
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}