package com.collage.new_strishakti

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.Adapter.CommentAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.data.model.Comment.CommentModel
import com.collage.new_strishakti.data.model.Comment.CommentResponse
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.network.ApiService
import com.collage.new_strishakti.data.repository.CommentRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response



class CommentActivity : AppCompatActivity(), CommentAdapter.CommentClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etComment: EditText
    private lateinit var btnSend: ImageView
    private lateinit var adapter: CommentAdapter
    private lateinit var session: SessionManager

    private val allComments = mutableListOf<CommentModel>()
    private var postId = 0
    private var replyToParentId = 0

    private var selectedCommentIdForDelete: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comment)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        session = SessionManager(this)
        postId = intent.getIntExtra("postId", -1)
        val postOwnerUsername = intent.getStringExtra("postOwnerUsername") ?: ""  // 👈 fetch from Intent

        if (postId == -1) finish()

        recyclerView = findViewById(R.id.recyclerComments)
        etComment = findViewById(R.id.etComment)
        btnSend = findViewById(R.id.btnSendComment)

        // ✅ Corrected adapter initialization
        adapter = CommentAdapter(
            context = this,
            comments = allComments,
            postOwnerUsername = postOwnerUsername,
            listener = this
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                if (replyToParentId == 0) postComment(text)
                else sendReply(replyToParentId, text)
            }
        }

        fetchComments()
    }

    /**
     * Fetch all comments and nested replies
     */
//    private fun fetchComments() {
//        val token = session.getToken() ?: ""
//        lifecycleScope.launch {
//            try {
//                val response = CommentRepository.getComments(token, postId)
//                if (response.isSuccessful && response.body() != null) {
//                    allComments.clear()
//                    response.body()?.commentsAndReacts?.forEach { parent ->
//                        allComments.add(parent)
//                        parent.children?.let { children -> allComments.addAll(children) }
//                    }
//                    adapter.updateList(allComments)
//                } else {
//                    Toast.makeText(
//                        this@CommentActivity,
//                        "Failed to load comments: ${response.message()}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            } catch (e: Exception) {
//                Toast.makeText(this@CommentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }



    private fun fetchComments() {
        val token = session.getToken() ?: ""
        lifecycleScope.launch {
            try {
                val response = CommentRepository.getComments(token, postId)
                if (response.isSuccessful && response.body() != null) {
                    allComments.clear()
                    response.body()?.commentsAndReacts?.forEach { parent ->
                        allComments.add(parent)
                        parent.children?.let { children -> allComments.addAll(children) }
                    }

                    if (allComments.isEmpty()) {
                        // ✅ No comments → show empty video state
                        ViewUtils.showEmptyState(findViewById(R.id.main), true, "No comments yet!")
                        recyclerView.visibility = View.GONE
                    } else {
                        // ✅ Comments found → hide empty state
                        ViewUtils.showEmptyState(findViewById(R.id.main), false, "")
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateList(allComments)
                    }
                } else {
                    Toast.makeText(
                        this@CommentActivity,
                        "Failed to load comments: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // ✅ Also show empty state on failure
                    ViewUtils.showEmptyState(findViewById(R.id.main), true, "Unable to load comments")
                }
            } catch (e: Exception) {
                Toast.makeText(this@CommentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                ViewUtils.showEmptyState(findViewById(R.id.main), true, "Error loading comments")
            }
        }
    }


    /**
     * Post a new top-level comment
     */
    private fun postComment(text: String) {
        val token = session.getToken() ?: ""
        val userId = session.getUserId()

        lifecycleScope.launch {
            try {
                val response = CommentRepository.addComment(token, userId, postId, text)
                if (response.isSuccessful && response.body() != null) {
                    val newComment = response.body()!!
                    allComments.add(0, newComment)
                    adapter.updateList(allComments)
                    etComment.setText("")
                    replyToParentId = 0

                    // ✅ Return updated comment count to Reel screen
                    val updatedCount = allComments.size
                    val resultIntent = Intent().apply {
                        putExtra("updatedCommentCount", updatedCount)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                } else {
                    Toast.makeText(this@CommentActivity, "Failed to post comment: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CommentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * Reply to an existing comment
     */
    private fun sendReply(parentCommentId: Int, replyText: String) {
        val token = session.getToken() ?: ""
        val userId = session.getUserId()

        lifecycleScope.launch {
            try {
                val response = CommentRepository.addReply(token, userId, postId, parentCommentId, replyText)
                if (response.isSuccessful) {
                    etComment.setText("")
                    replyToParentId = 0
                    fetchComments()
                } else {
                    Toast.makeText(
                        this@CommentActivity,
                        "Failed to send reply: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CommentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onReplyClicked(commentId: Int) {
        replyToParentId = commentId
        etComment.requestFocus()
        etComment.hint = "Replying..."
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT)
    }

//    override fun onDeleteClicked(comment: CommentModel) {
//        val token = session.getToken() ?: ""
//        val userId = session.getUserId()
//
//        // ✅ auto-select correct comment ID
//        selectedCommentIdForDelete = if (comment.childCommentId != 0)
//            comment.childCommentId
//        else
//            comment.childCommentId
//
//        Log.d("DELETE_COMMENT", "UserID: $userId, CommentID: ${comment.childCommentId}")
//
//        AlertDialog.Builder(this)
//            .setTitle("Delete Comment")
//            .setMessage("Are you sure you want to delete this comment?")
//            .setPositiveButton("Yes") { _, _ ->
//                lifecycleScope.launch {
//                    try {
//                        // ✅ include "Bearer " prefix here
//                        val response = CommentRepository.deleteComment(
//                            " Bearer $token",
//                            userId,
//                            comment.childCommentId
//                        )
//
//                        if (response.isSuccessful) {
//                            Toast.makeText(this@CommentActivity, "Comment deleted", Toast.LENGTH_SHORT).show()
//                            fetchComments()
//                        } else {
//                            Toast.makeText(this@CommentActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
//                        }
//                    } catch (e: Exception) {
//                        Toast.makeText(this@CommentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//            .setNegativeButton("No", null)
//            .show()
//    }


    override fun onDeleteClicked(comment: CommentModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = CommentRepository.deleteComment(
                            "Bearer ${session.getToken() ?: ""}",
                            session.getUserId(),
                            if (comment.childCommentId != 0) comment.childCommentId else comment.parentCommentId
                        )

                        if (response.isSuccessful) {
                            allComments.remove(comment)
                            adapter.updateList(allComments)

                            // ✅ Return updated count to Reel screen
                            val updatedCount = allComments.size
                            val resultIntent = Intent().apply {
                                putExtra("updatedCommentCount", updatedCount)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)

                            Toast.makeText(this@CommentActivity, "Comment deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CommentActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@CommentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }





    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
