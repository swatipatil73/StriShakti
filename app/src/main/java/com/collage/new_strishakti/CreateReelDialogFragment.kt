package com.collage.new_strishakti

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.data.network.ApiClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.jar.Manifest

class CreateReelDialogFragment : DialogFragment() {
    var onReelCreatedListener: OnReelCreatedListener? = null

    private lateinit var etPostText: EditText
    private lateinit var etHashtags: EditText
    private lateinit var btnAddVideo: Button
    private lateinit var btnSubmit: Button

    private val selectedVideoUris = mutableListOf<Uri>()

    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris != null && uris.isNotEmpty()) {
                selectedVideoUris.clear()
                selectedVideoUris.addAll(uris)
                Toast.makeText(requireContext(), "${uris.size} video(s) selected", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickVideos()
            else Toast.makeText(requireContext(), "Permission denied to access videos", Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_post_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etPostText = view.findViewById(R.id.etPostText)
        etHashtags = view.findViewById(R.id.etHashtags)
        btnAddVideo = view.findViewById(R.id.btnAddVideo)
        btnSubmit = view.findViewById(R.id.btnSubmitPost)

        // Hide Image Button
        view.findViewById<Button>(R.id.btnAddImage).visibility = View.GONE

        btnAddVideo.setOnClickListener { checkPermissionAndPickVideos() }
        btnSubmit.setOnClickListener { submitReel() }
    }

    private fun checkPermissionAndPickVideos() {
        val permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                android.Manifest.permission.READ_MEDIA_VIDEO
            else
                android.Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> pickVideos()
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(requireContext(), "Permission needed to pick videos", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun pickVideos() {
        pickVideoLauncher.launch("video/*")
    }
    private fun submitReel() {
        val tooBig = selectedVideoUris.any { getFileSizeInMB(it) > 50 } // 🔸 Limit: 50MB
        if (tooBig) {
            showSnackBar("Video file too large. Please select a smaller one.", isError = true)
            return
        }

        val caption = etPostText.text.toString().trim()
        if (selectedVideoUris.isEmpty()) {
            showSnackBar("Please select a video", isError = true)
            return
        }

        // Optional: show progress bar
        view?.findViewById<ProgressBar>(R.id.progress)?.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                val sessionManager = SessionManager(requireContext())
                val userId = sessionManager.getUserId()
                val token = sessionManager.getToken()

                val postNamePart = caption.toRequestBody("text/plain".toMediaTypeOrNull())
                val postTypePart = "video".toRequestBody("text/plain".toMediaTypeOrNull())
                val videoThumbnailUrlPart = "".toRequestBody("text/plain".toMediaTypeOrNull())

                val videoParts = selectedVideoUris.map { uri ->
                    prepareFilePart("postImage", uri, requireContext())
                }

                val response = ApiClient.apiService.addReel(
                    userId = userId,
                    token = "Bearer $token",
                    postName = postNamePart,
                    postType = postTypePart,
                    videoThumbnailUrl = videoThumbnailUrlPart,
                    postImage = videoParts
                )

                if (response.isSuccessful && response.body()?.status == "Success") {
                    showSnackBar("Reel uploaded successfully")
                    onReelCreatedListener?.onReelCreated()
                    dismiss()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val code = response.code()

                    when {
                        code == 413 || errorBody.contains("Request Entity Too Large", true) -> {
                            showSnackBar("Video size too large. Please select a smaller video.", isError = true)
                        }
                        errorBody.contains("<html>", true) -> {
                            showSnackBar("Server error. Please try again with a smaller file.", isError = true)
                        }
                        else -> {
                            showSnackBar("Upload failed: ${response.message()}", isError = true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackBar("Error: ${e.message}", isError = true)
            } finally {
                // Hide progress and re-enable button
                view?.findViewById<ProgressBar>(R.id.progress)?.visibility = View.GONE
                btnSubmit.isEnabled = true
            }
        }
    }


    private fun showSnackBar(message: String, isError: Boolean = false) {
        val rootView = dialog?.window?.decorView?.findViewById<View>(android.R.id.content)
        rootView?.let {
            val snack = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            if (isError) {
                snack.setBackgroundTint(Color.parseColor("#E53935")) // 🔴 red for errors
            } else {
                snack.setBackgroundTint(Color.parseColor("#43A047")) // 🟢 green for success
            }
            snack.setTextColor(Color.WHITE)
            snack.show()
        }
    }



    private fun prepareFilePart(partName: String, fileUri: Uri, context: Context): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
        val fileName = getFileName(fileUri, context)
        val requestFile = InputStreamRequestBody(contentResolver, fileUri, mimeType)
        return MultipartBody.Part.createFormData(partName, fileName, requestFile)
    }

    private fun getFileName(uri: Uri, context: Context): String {
        var name = "video_${System.currentTimeMillis()}"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) name = it.getString(index)
        }
        return name
    }


    private fun getFileSizeInMB(uri: Uri): Double {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        var size: Long = 0
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && it.moveToFirst()) {
                size = it.getLong(sizeIndex)
            }
        }
        return size / (1024.0 * 1024.0) // Convert bytes → MB
    }

}
interface OnReelCreatedListener {
    fun onReelCreated()
}
