package com.collage.empowermentstrishakti.Common

import android.util.Log
import com.collage.empowermentstrishakti.data.repository.LikeRepository
import com.collage.empowermentstrishakti.data.repository.PostActionsRepository
import com.collage.empowermentstrishakti.data.repository.SavedPostsRepository
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * InteractionManagerImpl that implements both String and Int variants for convenience.
 * Replace TODO call sites if your repository method signatures differ.
 */
class InteractionManagerImpl(
    private val likeRepo: LikeRepository,
    private val postActionsRepo: PostActionsRepository,
    private val savedPostsRepo: SavedPostsRepository? = null
) : InteractionManager {

    private val tag = "InteractionManagerImpl"

    // --------------------
    // Interface methods (String signatures) - required by your interface
    // --------------------
     suspend fun likePost(userId: String, postId: String, token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val callResult = try {
                    likeRepo.likePost(userId, postId, token)
                } catch (e: Exception) {
                    // If your repo requires ints, you can adapt here:
                    throw e
                }

                when (callResult) {
                    is Response<*> -> callResult.isSuccessful
                    is Result<*> -> (callResult as Result<*>).isSuccess
                    is Boolean -> callResult
                    else -> {
                        Log.w(tag, "likePost: unknown repo return type ${callResult?.javaClass}")
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "likePost failed", e)
                false
            }
        }
    }

    suspend fun unlikePost(userId: String, postId: String, token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val callResult = try {
                    likeRepo.unlikePost(userId, postId, token)
                } catch (e: Exception) {
                    throw e
                }

                when (callResult) {
                    is Response<*> -> callResult.isSuccessful
                    is Result<*> -> (callResult as Result<*>).isSuccess
                    is Boolean -> callResult
                    else -> {
                        Log.w(tag, "unlikePost: unknown repo return type ${callResult?.javaClass}")
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "unlikePost failed", e)
                false
            }
        }
    }

    // --------------------
    // Int variants (convenience overloads used elsewhere in code)
    // --------------------
    override suspend fun likePost(userId: Int, postId: Int, token: String): Boolean {
        return likePost(userId.toString(), postId.toString(), token)
    }

     override suspend fun unlikePost(userId: Int, postId: Int, token: String): Boolean {
        return unlikePost(userId.toString(), postId.toString(), token)
    }

    // --------------------
    // Save / Unsave
    // --------------------
    override suspend fun savePost(userId: Int, postId: Int, token: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            Log.d(tag, "savePost called (user=$userId, post=$postId)")
            // TODO: call postActionsRepo.savePost(...) or savedPostsRepo.savePost(...) and return (success, message)
            Pair(true, "Saved (optimistic)")
        }
    }

    override suspend fun unsavePost(userId: Int, postId: Int, token: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            Log.d(tag, "unsavePost called (user=$userId, post=$postId)")
            // TODO: call savedPostsRepo.unsavePost(...) and return result
            Pair(true, "Removed (optimistic)")
        }
    }
    // --------------------
    // Report / Comment / Share
    // --------------------
    override suspend fun reportPost(userId: Int, postId: Int, reason: String, token: String): Boolean {
        return withContext(Dispatchers.IO) {
            Log.d(tag, "reportPost called (user=$userId, post=$postId, reason=$reason)")
            // TODO: call postActionsRepo.reportPost(...) adapting 'reason' param type if needed
            true
        }
    }

    override suspend fun addComment(userId: Int, postId: Int, commentText: String, token: String): Boolean {
        return withContext(Dispatchers.IO) {
            Log.d(tag, "addComment called (user=$userId, post=$postId) text=${commentText.take(30)}")
            // TODO: call postActionsRepo.addComment(...) and return server result
            true
        }
    }

    override suspend fun notifyShare(userId: Int, postId: Int, token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // If you have API to record share, call it here. Otherwise return true.
                true
            } catch (e: Exception) {
                Log.e(tag, "notifyShare failed", e)
                false
            }
        }
    }
}
