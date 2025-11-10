package com.collage.new_strishakti.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.data.repository.UserProfileRepository
import com.collage.new_strishakti.ui.RegisterViewModel.UserProfileViewModel


class UserProfileViewModelFactory(
    private val repository: UserProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            return UserProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}