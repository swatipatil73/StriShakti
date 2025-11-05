package com.collage.new_strishakti.ui.RegisterViewModel

// PostActionsViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.repository.PostActionsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class PostActionEvent {
    data class Deleted(val postId: Int) : PostActionEvent()
    data class Error(val postId: Int, val message: String) : PostActionEvent()
    data class Saved(val postId: Int, val message: String) : PostActionEvent() //
}

class PostActionsViewModel(
    private val repo: PostActionsRepository
) : ViewModel() {

    private val _events = Channel<PostActionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Existing delete function...
    fun delete(postId: Int, token: String) {
        viewModelScope.launch {
            val result = repo.deletePost(postId, token)
            result.fold(
                onSuccess = { _events.send(PostActionEvent.Deleted(postId)) },
                onFailure = { _events.send(PostActionEvent.Error(postId, it.message ?: "Delete failed")) }
            )
        }
    }

    // ✅ New save function
    fun save(userId: Int, postId: Int, token: String) {
        viewModelScope.launch {
            val result = repo.savePost(userId, postId, token)
            result.fold(
                onSuccess = { message ->
                    _events.send(PostActionEvent.Saved(postId, message))
                },
                onFailure = { throwable ->
                    _events.send(PostActionEvent.Error(postId, throwable.message ?: "Save failed"))
                }
            )
        }
    }



}

