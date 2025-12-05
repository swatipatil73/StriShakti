package com.collage.empowermentstrishakti.Common



/**
 * Central API to perform interactions on posts (like/unlike/save/unsave/report/comment/share).
 * All adapters call this. Implementation performs network calls (via repositories) and returns success/failure.
 */
interface InteractionManager {
    suspend fun likePost(userId: Int, postId: Int, token: String): Boolean
    suspend fun unlikePost(userId: Int, postId: Int, token: String): Boolean

    suspend fun savePost(userId: Int, postId: Int, token: String): Pair<Boolean, String?> // (success, message)
    suspend fun unsavePost(userId: Int, postId: Int, token: String): Pair<Boolean, String?>

    suspend fun reportPost(userId: Int, postId: Int, reason: String, token: String): Boolean

    suspend fun addComment(userId: Int, postId: Int, commentText: String, token: String): Boolean

    suspend fun notifyShare(userId: Int, postId: Int, token: String): Boolean
}
