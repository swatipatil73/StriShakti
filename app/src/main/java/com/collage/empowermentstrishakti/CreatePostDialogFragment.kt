package com.collage.empowermentstrishakti

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.collage.empowermentstrishakti.Common.OnPostCreatedListener
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.source

// Streams large files without loading all bytes into memory
class InputStreamRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val contentType: String
) : RequestBody() {
    override fun contentType() = contentType.toMediaTypeOrNull()

    override fun writeTo(sink: okio.BufferedSink) {
        contentResolver.openInputStream(uri)?.use { input ->
            sink.writeAll(input.source())
        }
    }
}

class CreatePostDialogFragment : DialogFragment() {
    var onPostCreatedListener: OnPostCreatedListener? = null

    private lateinit var etPostText: EditText
    private lateinit var etHashtags: EditText
    private lateinit var btnAddImage: Button
    private lateinit var btnAddVideo: Button
    private lateinit var btnSubmit: Button

    private val selectedMediaUris = mutableListOf<Uri>()
    private var isVideoSelected = false

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris != null && uris.isNotEmpty()) {
                selectedMediaUris.clear()
                selectedMediaUris.addAll(uris)
                isVideoSelected = uris.any { it.toString().endsWith(".mp4") }
                Toast.makeText(requireContext(), "${uris.size} media selected", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImages()
            else Toast.makeText(requireContext(), "Permission denied to access media", Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_post_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etPostText = view.findViewById(R.id.etPostText)
        etHashtags = view.findViewById(R.id.etHashtags)
        btnAddImage = view.findViewById(R.id.btnAddImage)
        btnAddVideo = view.findViewById(R.id.btnAddVideo)
        btnSubmit = view.findViewById(R.id.btnSubmitPost)

        btnAddImage.setOnClickListener { checkPermissionAndPickImages() }
        btnAddVideo.setOnClickListener { checkPermissionAndPickVideos() }
        btnSubmit.setOnClickListener { submitPost() }
    }

    // 🟩 Make dialog full width with margin
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)

            val params = attributes
            params.gravity = Gravity.CENTER
            attributes = params
        }

        // Optional: add margin/padding (e.g., 24dp from sides)
        val marginInPx = (24 * resources.displayMetrics.density).toInt()
        val decorView = dialog?.window?.decorView
        decorView?.setPadding(marginInPx, 0, marginInPx, 0)
    }

    private fun checkPermissionAndPickImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.READ_MEDIA_IMAGES
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> pickImages()
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(requireContext(), "Media access permission is required to pick images", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(permission)
                }
                else -> requestPermissionLauncher.launch(permission)
            }
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> pickImages()
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(requireContext(), "Storage permission needed to pick images", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(permission)
                }
                else -> requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun checkPermissionAndPickVideos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.READ_MEDIA_VIDEO
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> pickVideos()
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(requireContext(), "Media access permission is required to pick videos", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(permission)
                }
                else -> requestPermissionLauncher.launch(permission)
            }
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> pickVideos()
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(requireContext(), "Storage permission needed to pick videos", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(permission)
                }
                else -> requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun pickImages() {
        pickMediaLauncher.launch("image/*")
    }

    private fun pickVideos() {
        pickMediaLauncher.launch("video/*")
    }

    private fun submitPost() {
        val postText = etPostText.text.toString().trim()
        if (postText.isEmpty() && selectedMediaUris.isEmpty()) {
            Toast.makeText(requireContext(), "Please add text or media", Toast.LENGTH_SHORT).show()
            return
        }

        val postType = when {
            isVideoSelected -> "video"
            selectedMediaUris.isNotEmpty() -> "image"
            else -> "text"
        }

        lifecycleScope.launch {
            try {
                val sessionManager = SessionManager(requireContext())
                val userId = sessionManager.getUserId()
                val token = sessionManager.getToken()






                val postNamePart = postText.toRequestBody("text/plain".toMediaTypeOrNull())
                val postTypePart = postType.toRequestBody("text/plain".toMediaTypeOrNull())
                val videoThumbnailPart = "".toRequestBody("text/plain".toMediaTypeOrNull())

                val mediaParts = selectedMediaUris.map { uri ->
                    prepareFilePart("postImage", uri, requireContext())
                }

                val response = ApiClient.apiService.addPost(
                    userId = userId,
                    token = "Bearer $token",
                    postName = postNamePart,
                    postType = postTypePart,
                    videoThumbnailUrl = videoThumbnailPart,
                    postImage = if (mediaParts.isNotEmpty()) mediaParts else null
                )
                if (response.isSuccessful && response.body()?.status == "Success") {

                    Log.d("POST_FLOW", "Post upload success — opening home")

                    Toast.makeText(requireContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show()

                    // Open Home screen
                   val intent = Intent(requireContext(), MainActivity::class.java)
                   //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
startActivity(intent)

                    onPostCreatedListener?.onPostCreated()

                    dismiss()
                }




                else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    Toast.makeText(requireContext(), "Failed: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareFilePart(partName: String, fileUri: Uri, context: Context): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
        val fileName = getFileName(fileUri, context)
        Log.d("PrepareFilePart", "fileName=$fileName, mimeType=$mimeType")

        val requestFile = InputStreamRequestBody(contentResolver, fileUri, mimeType)
        return MultipartBody.Part.createFormData(partName, fileName, requestFile)
    }

    private fun getFileName(uri: Uri, context: Context): String {
        var name = "file_${System.currentTimeMillis()}"
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    
}
