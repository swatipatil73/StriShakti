package com.collage.empowermentstrishakti.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.data.repository.EventRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.EventViewModel


@Suppress("UNCHECKED_CAST")
class EventViewModelFactory(
    private val repository: EventRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

