package com.collage.empowermentstrishakti

import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel



import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.PageDetailViewModelFactory
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.PageAbout
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


import okhttp3.MultipartBody


import androidx.lifecycle.Observer
import com.collage.empowermentstrishakti.Common.FileUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.Locale

class AboutFragment : Fragment() {

    private lateinit var tvPageName: TextView
    private lateinit var tvPageDescription: TextView
    private lateinit var tvAdminName: TextView
    private lateinit var imgAdminProfile: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var tvCreatedAt: TextView

    private lateinit var tvLink: TextView
    private lateinit var btnEditPage: Button

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var pickedImageUri: Uri? = null
    private lateinit var vm: PageDetailViewModel
    private lateinit var factory: PageDetailViewModelFactory
    private val session by lazy { SessionManager(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Create SessionManager
        val sessionManager = SessionManager(requireContext())

        // 2) Create ApiService (same style as apiService in your UserProfile)
        val apiService = ApiClient.apiService     // <-- IMPORTANT: same as your UserProfile code

        // 3) Create Repository (DI style)
        val repository = PagesRepository(apiService)
        // If your PagesRepository only accepts apiService, remove sessionManager argument

        // 4) Create Factory
        val factory = PageDetailViewModelFactory(repository, sessionManager)

        // 5) Create shared ViewModel
        vm = ViewModelProvider(requireActivity(), factory)
            .get(PageDetailViewModel::class.java)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                pickedImageUri = uri
                // You can update UI (e.g., change button text) if needed
            }
        }
    }

    private fun uriToImagePart(ctx: Context, fieldName: String, uri: Uri): MultipartBody.Part? {
        val tmpFile = FileUtil.copyUriToTempFile(ctx, uri) ?: return null
        val mime = FileUtil.getMimeType(ctx, uri) ?: "image/*"
        val reqFile = tmpFile.asRequestBody(mime.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, tmpFile.name, reqFile)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_about, container, false)
        tvPageName = v.findViewById(R.id.tvPageName)
        tvPageDescription = v.findViewById(R.id.tvPageDescription)
        tvAdminName = v.findViewById(R.id.tvAdminName)
        imgAdminProfile = v.findViewById(R.id.imgAdminProfile)
        tvCreatedAt = v.findViewById(R.id.tvCreatedAt)

        tvLink = v.findViewById(R.id.tvLink)
        btnEditPage = v.findViewById(R.id.btnEditPage)
        //  btnDeletePage = v.findViewById(R.id.btnDeletePage)

        btnEditPage.setOnClickListener { showEditDialog() }
        // btnDeletePage.setOnClickListener { confirmDelete() }

        // Observe pageAbout from shared VM
        vm.pageAbout.observe(viewLifecycleOwner) { about ->
            bindAbout(about)
        }

        // If pageDetails is used instead (complete object), observe it and read pageAbout
        vm.pageDetails.observe(viewLifecycleOwner) { resp ->
            bindAbout(resp?.pageAbout)
        }

        return v
    }

    private fun bindAbout(about: PageAbout?) {
        if (about == null) return

        tvPageName.text = about.pageName ?: ""
        tvPageDescription.text = about.pageDescription ?: ""
        tvAdminName.text =
            "${about.adminUserFirstName ?: ""} ${about.adminUserLastName ?: ""}".trim()

      //
        //
        //
        //
        //
        //
        //
        //
        //  tvUUID.visibility = if (!about.puuid.isNullOrBlank()) View.VISIBLE else View.GONE

        if (!about.linkUrl.isNullOrBlank()) {
            tvLink.text = about.linkUrl
            tvLink.visibility = View.VISIBLE
        } else {
            tvLink.visibility = View.GONE
        }

        tvCreatedAt.text = try {
            about.pageCreatedAt?.let { iso ->
                Log.d("PageCreatedAt", "Original ISO: $iso")

                // 1) Try OffsetDateTime (works if string has offset or 'Z')
                val odt: OffsetDateTime = try {
                    OffsetDateTime.parse(iso)
                } catch (e: DateTimeParseException) {
                    // 2) Fallback: parse as LocalDateTime with fractional seconds then convert to OffsetDateTime
                    try {
                        val ldt = LocalDateTime.parse(
                            iso,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                        )
                        ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime()
                    } catch (e2: DateTimeParseException) {
                        // 3) Another fallback: try ISO_LOCAL_DATE_TIME (handles variable fraction lengths)
                        val ldt2 = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        ldt2.atZone(ZoneId.systemDefault()).toOffsetDateTime()
                    }
                }

                // Format to "12.00am" style (dot, lowercase, no space)
                val outFmt = DateTimeFormatter.ofPattern("hh.mm a", Locale.getDefault())
                val formatted = odt.format(outFmt).replace(" ", "").lowercase(Locale.getDefault())

                Log.d("PageCreatedAt", "Formatted Time: $formatted")
                formatted
            } ?: ""
        } catch (e: Exception) {
            Log.e("PageCreatedAt", "Error formatting date: ${e.message}")
            // fallback display: raw ISO (or empty)
            about.pageCreatedAt ?: ""
        }






        // admin image
        Glide.with(requireContext())
            .load(about.adminUserProfileImagePath)
            .placeholder(R.drawable.user)
            .into(imgAdminProfile)

        // show edit/delete only if current user is admin
        val isAdmin = (about.adminId == session.getUserId())
        btnEditPage.visibility = if (isAdmin) View.VISIBLE else View.GONE

    }

    private fun showEditDialog() {
        val about = vm.pageAbout.value ?: return
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 14, 28, 0)
        }

        val inputName = EditText(ctx).apply {
            hint = "Page name"
            setText(about.pageName ?: "")
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val inputDesc = EditText(ctx).apply {
            hint = "Description"
            setText(about.pageDescription ?: "")
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }
        val inputLinkName = EditText(ctx).apply {
            hint = "Link URL Name (optional)"
            setText("") // set if you have stored value
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val inputLink = EditText(ctx).apply {
            hint = "Link (optional)"
            setText(about.linkUrl ?: "")
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        val btnSelectImage = Button(ctx).apply {
            text = "Select Cover Image (optional)"
            setOnClickListener {
                // open picker for images
                pickImageLauncher.launch("image/*")
            }
        }

        layout.addView(inputName)
        layout.addView(inputDesc)
        layout.addView(inputLinkName)
        layout.addView(inputLink)
        layout.addView(btnSelectImage)

        val dialog = AlertDialog.Builder(ctx)
            .setTitle("Edit Page")
            .setView(layout)
            .setCancelable(false)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            "Save"
        ) { _, _ -> /* will be overridden below */ }
        dialog.show()

        val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveBtn.setOnClickListener {
            val newName = inputName.text?.toString()?.trim().orEmpty()
            val newDesc = inputDesc.text?.toString()?.trim().orEmpty().ifBlank { null }
            val newLinkName = inputLinkName.text?.toString()?.trim().orEmpty().ifBlank { null }
            val newLink = inputLink.text?.toString()?.trim().orEmpty().ifBlank { null }

            if (newName.isBlank()) {
                Toast.makeText(ctx, "Name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = about.copy(
                pageName = newName,
                pageDescription = newDesc,
                linkUrl = newLink
            )

            // optimistic UI
            vm.updatePageAboutLocally(updated)

            // prepare coverImagePart if user picked an image
//            val coverPart: MultipartBody.Part? = pickedImageUri?.let { uri ->
//                try {
//                    uriToImagePart(ctx, "coverImage", uri)
//                } catch (e: Exception) {
//                    Log.e("UPDATE_PAGE_ERROR", "Failed to prepare image part", e)
//                    null
//                }
//            }


            val coverPart = pickedImageUri?.let { uriToImagePart(requireContext(), "coverImage", it) }
            vm.updatePageOnServer(updated, coverPart)
            // disable inputs
            positiveBtn.isEnabled = false
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
            inputName.isEnabled = false
            inputDesc.isEnabled = false
            inputLink.isEnabled = false
            btnSelectImage.isEnabled = false

            // observe one-shot
            vm.updateState.observe(
                viewLifecycleOwner,
                object : Observer<PageDetailViewModel.UpdateResult> {
                    override fun onChanged(state: PageDetailViewModel.UpdateResult) {
                        when (state) {
                            is PageDetailViewModel.UpdateResult.Loading -> positiveBtn.text =
                                "Saving..."

                            is PageDetailViewModel.UpdateResult.Success -> {
                                Toast.makeText(
                                    ctx,
                                    state.response?.message ?: "Saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                                vm.updateState.removeObserver(this)
                                dialog.dismiss()
                                // cleanup temp file if you created one in FileUtil (it deletes after upload in repo or you can delete here)
                            }

                            is PageDetailViewModel.UpdateResult.Error -> {
                                Log.d(
                                    "UPDATE_PAGE_ERROR",
                                    "Failed to update page: ${state.message}"
                                )
                                positiveBtn.isEnabled = true
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
                                inputName.isEnabled = true
                                inputDesc.isEnabled = true
                                inputLink.isEnabled = true
                                btnSelectImage.isEnabled = true
                                positiveBtn.text = "Save"
                                Toast.makeText(
                                    ctx,
                                    "Update failed: ${state.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                vm.updateState.removeObserver(this)
                            }
                        }
                    }
                })

            // call VM with cover part (fragment created the MultipartBody.Part)
            vm.updatePageOnServer(updated, coverPart)
        }


    }
}
