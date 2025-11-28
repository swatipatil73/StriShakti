package com.collage.empowermentstrishakti

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.PagesViewModelFactory
import com.collage.empowermentstrishakti.data.model.PageDetail
import com.collage.empowermentstrishakti.data.model.SavedPost.CreatePageRequest
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PagesViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreatePageDialogFragment : DialogFragment() {

    private lateinit var etPageName: TextInputEditText
    private lateinit var etPageDescription: TextInputEditText
    private lateinit var etLinkUrlName: TextInputEditText
    private lateinit var etLinkUrl: TextInputEditText
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var imgPreviewCard: CardView
    private lateinit var imgPreview: ImageView
    private lateinit var btnSubmitPage: MaterialButton

    private var selectedImageUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    // ⭐ callback variable (NEEDED)
    var callback: Callback? = null

    // ViewModel
    private lateinit var vm: PagesViewModel

    private val session by lazy { SessionManager(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = PagesRepository(ApiClient.apiService)
        val factory = PagesViewModelFactory(repo)

        // activity scoped ViewModel so Activity + Dialog share same data
        vm = ViewModelProvider(requireActivity(), factory)
            .get(PagesViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.dialog_create_page, container, false)

        etPageName = v.findViewById(R.id.etPageName)
        etPageDescription = v.findViewById(R.id.etPageDescription)
        etLinkUrlName = v.findViewById(R.id.etLinkUrlName)
        etLinkUrl = v.findViewById(R.id.etLinkUrl)
        btnSelectImage = v.findViewById(R.id.btnSelectImage)
        imgPreviewCard = v.findViewById(R.id.imgPreviewCard)
        imgPreview = v.findViewById(R.id.imgPreview)
        btnSubmitPage = v.findViewById(R.id.btnSubmitPage)

        imgPreviewCard.isVisible = false

        // Image picker
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                selectedImageUri = uri
                if (uri != null) {
                    imgPreviewCard.isVisible = true
                    Glide.with(requireContext()).load(uri).centerCrop().into(imgPreview)
                } else {
                    imgPreviewCard.isVisible = false
                }
            }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmitPage.setOnClickListener {
            submitCreatePage()
        }

        // ⭐ Observe createStatus (success / error)
        vm.createStatus.observe(viewLifecycleOwner) { pair ->
            val (success, message) = pair

            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }

            if (success) {
                val newlyCreatedPage = vm.createdPage.value
//
//                if (newlyCreatedPage != null) {
//                    callback?.onPageCreated(newlyCreatedPage)
//                }

                dismiss()
            }
        }

        return v
    }

    private fun submitCreatePage() {
        val name = etPageName.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) {
            etPageName.error = "Required"
            return
        }

        val description = etPageDescription.text?.toString()?.trim().orEmpty().ifBlank { null }
        val linkName = etLinkUrlName.text?.toString()?.trim().orEmpty().ifBlank { null }
        val linkUrl = etLinkUrl.text?.toString()?.trim().orEmpty().ifBlank { null }
        val coverPath = selectedImageUri?.toString()

        val req = CreatePageRequest(
            pageName = name,
            pageDescription = description,
            pageCoverProfileImagePath = coverPath,
            linkUrlName = linkName,
            linkUrl = linkUrl
        )

        val adminId = session.getUserId()
        val token = "Bearer ${session.getToken()}"

        vm.createPage(
            adminUserId = adminId,
            body = req,
            token = token
        )
    }

    interface Callback {
        fun onPageCreated(newPage: PageDetail)
    }
}
