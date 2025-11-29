package com.collage.empowermentstrishakti

import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel



import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.PageAbout
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class AboutFragment : Fragment() {

    private lateinit var tvPageName: TextView
    private lateinit var tvPageDescription: TextView
    private lateinit var tvAdminName: TextView
    private lateinit var imgAdminProfile: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvUUID: TextView
    private lateinit var tvLink: TextView
    private lateinit var btnEditPage: Button
    private lateinit var btnDeletePage: Button

    private lateinit var vm: PageDetailViewModel
    private val session by lazy { SessionManager(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(requireActivity()).get(PageDetailViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_about, container, false)
        tvPageName = v.findViewById(R.id.tvPageName)
        tvPageDescription = v.findViewById(R.id.tvPageDescription)
        tvAdminName = v.findViewById(R.id.tvAdminName)
        imgAdminProfile = v.findViewById(R.id.imgAdminProfile)
        tvCreatedAt = v.findViewById(R.id.tvCreatedAt)
        tvUUID = v.findViewById(R.id.tvUUID)
        tvLink = v.findViewById(R.id.tvLink)
        btnEditPage = v.findViewById(R.id.btnEditPage)
        btnDeletePage = v.findViewById(R.id.btnDeletePage)

        btnEditPage.setOnClickListener { showEditDialog() }
        btnDeletePage.setOnClickListener { confirmDelete() }

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
        tvAdminName.text = "${about.adminUserFirstName ?: ""} ${about.adminUserLastName ?: ""}".trim()
        tvUUID.text = about.puuid ?: ""
        tvUUID.visibility = if (!about.puuid.isNullOrBlank()) View.VISIBLE else View.GONE

        if (!about.linkUrl.isNullOrBlank()) {
            tvLink.text = about.linkUrl
            tvLink.visibility = View.VISIBLE
        } else {
            tvLink.visibility = View.GONE
        }

        // created date formatting (try ISO -> friendly)
        tvCreatedAt.text = try {
            about.pageCreatedAt?.let { iso ->
                val odt = OffsetDateTime.parse(iso)
                val formatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
                "Created: ${odt.format(formatted)}"
            } ?: ""
        } catch (e: Exception) {
            "Created: ${about.pageCreatedAt ?: ""}"
        }

        // admin image
        Glide.with(requireContext())
            .load(about.adminUserProfileImagePath)
            .placeholder(R.drawable.user)
            .into(imgAdminProfile)

        // show edit/delete only if current user is admin
        val isAdmin = (about.adminId == session.getUserId())
        btnEditPage.visibility = if (isAdmin) View.VISIBLE else View.GONE
        btnDeletePage.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun showEditDialog() {
        // Load current values
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
        val inputLink = EditText(ctx).apply {
            hint = "Link (optional)"
            setText(about.linkUrl ?: "")
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        layout.addView(inputName)
        layout.addView(inputDesc)
        layout.addView(inputLink)

        AlertDialog.Builder(ctx)
            .setTitle("Edit Page")
            .setView(layout)
            .setPositiveButton("Save") { dialog, _ ->
                val newName = inputName.text?.toString()?.trim().orEmpty()
                val newDesc = inputDesc.text?.toString()?.trim().orEmpty().ifBlank { null }
                val newLink = inputLink.text?.toString()?.trim().orEmpty().ifBlank { null }

                if (newName.isBlank()) {
                    Toast.makeText(ctx, "Name required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = about.copy(
                    pageName = newName,
                    pageDescription = newDesc,
                    linkUrl = newLink
                )

                // Immediately update UI locally
                vm.updatePageAboutLocally(updated)

                // TODO: call backend API to persist updates.
                // Example:
                // viewModelScope.launch { repo.updatePage(...); on success call vm.loadPageDetails(...) or vm.setPageDetailsForTest(response) }

                dialog.dismiss()
                Toast.makeText(ctx, "Saved (local). Server call TODO", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete() {
        val about = vm.pageAbout.value ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Page")
            .setMessage("Are you sure you want to delete this page? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // TODO: call backend delete endpoint here; after success finish the activity or navigate back
                // Example:
                // pagesRepository.deletePage(about.adminId, about.pagesId, token)
                Toast.makeText(requireContext(), "Delete API TODO", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
