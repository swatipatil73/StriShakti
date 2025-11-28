package com.collage.empowermentstrishakti.ui.RegisterViewModel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.Adapter.ReportReasonAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.post.ReportReason
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.PostActionsRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ReportBottomSheet {

    fun show(context: Context, postId: Int, userId: Int) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_report_post, null)
        dialog.setContentView(view)

        // Expand Bottom Sheet Full Height
        val bottomSheet = dialog.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        val btnSubmit = view.findViewById<Button>(R.id.btnReportSubmit)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.listViewReasons)

        // Sample reasons
        // Use the same ReportReason class
        val reasons = listOf(
            ReportReason(1, "Spam", "Unsolicited promotional content"),
            ReportReason(2, "Fraud", "Unauthorized transaction attempt"),
            ReportReason(3, "Phishing", "Attempt to steal personal information"),
            ReportReason(4, "Impersonation", "Pretending to be someone else"),
            ReportReason(5, "Scam", "Fraudulent scheme to obtain money"),
            ReportReason(6, "Harassment", "Bullying or threatening behavior"),
            ReportReason(7, "Copyright Violation", "Unauthorized use of copyrighted material"),
            ReportReason(8, "Malware", "Software designed to harm or exploit")
        )


        val adapter = ReportReasonAdapter(reasons)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter




        btnSubmit.setOnClickListener {
            val selected = adapter.getSelectedItem()
            if (selected == null) {
                Toast.makeText(context, "Please select a reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call repository suspend function in Coroutine
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Initialize SessionManager and get token
                    val sessionManager = SessionManager(context)
                    val token = sessionManager.getToken()

                    if (token.isNullOrEmpty()) {
                        Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Use the singleton ApiService from ApiClient
                    val repository = PostActionsRepository(ApiClient.apiService)

                    // Call suspend function to report post
                    val result = repository.reportPost(userId, postId, selected, token)


                    // Handle success or failure
                    result.fold(
                        onSuccess = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            val displayMessage = error.message ?: "Something went wrong"
                            Toast.makeText(context, displayMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "An unexpected error occurred", Toast.LENGTH_SHORT).show()
                }
            }


            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}

