package com.collage.empowermentstrishakti

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.FileUtil
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.GroupViewModelFactory
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import com.collage.empowermentstrishakti.databinding.FragmentGroupAboutBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.GroupViewModel

import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class GroupAboutFragment : Fragment() {

    private var groupId: Int = 0
    private var groupUUID: String = ""
    private var groupData: GroupDetailsResponse? = null

    private var _binding: FragmentGroupAboutBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager
    private var selectedImageUri: Uri? = null
    private lateinit var viewModel: GroupViewModel

    // Image picker launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                Log.d("GROUP_ABOUT", "Image picked: $it")
            }
        }

    companion object {
        private const val ARG_GROUP_DATA = "arg_group_data"

        fun newInstance(data: GroupDetailsResponse): GroupAboutFragment {
            val fragment = GroupAboutFragment()
            val args = Bundle()
            args.putParcelable(ARG_GROUP_DATA, data)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("GROUP_ABOUT", "onCreate")

        arguments?.let {
            groupData = it.getParcelable(ARG_GROUP_DATA)
            Log.d("GROUP_ABOUT", "Received parcelable: $groupData")
            groupData?.groupAbout?.let { about ->
                groupId = about.groupId
                groupUUID = about.groupUUID ?: ""
            }
        }
        Log.d("GROUP_ABOUT", "groupId=$groupId, groupUUID=$groupUUID")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGroupAboutBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("GROUP_ABOUT", "onViewCreated")

        // If we have parcelable passed, show immediately
        groupData?.let {
            Log.d("GROUP_ABOUT", "setupUI from initial parcelable")
            setupUI(it)
        }

        val repo = GroupRepository(ApiClient.apiService, session.getToken() ?: "")
        viewModel = ViewModelProvider(this, GroupViewModelFactory(repo))[GroupViewModel::class.java]

        viewModel.groupDetails.observe(viewLifecycleOwner) { data ->
            Log.d("GROUP_ABOUT", "LiveData observed -> $data")
            data?.let { setupUI(it) }
        }

        val userId = try {
            session.getUserId().toString().toInt()
        } catch (e: Exception) {
            Log.e("GROUP_ABOUT", "UserId parse error: ${e.message}")
            0
        }

        Log.d("GROUP_ABOUT", "UserId=$userId token=${session.getToken()}")

        if (groupUUID.isNotBlank() && userId != 0) {
            Log.d("GROUP_ABOUT", "Loading group details from API for uuid=$groupUUID")
            viewModel.loadGroupDetails(
                userId,
                groupUUID,
                0,
                5,
                "Bearer ${session.getToken()}"
            )
        } else {
            Log.e("GROUP_ABOUT", "Not calling API: groupUUID='$groupUUID' userId=$userId")
        }
    }

    private fun setupUI(data: GroupDetailsResponse) {
        Log.d("GROUP_ABOUT", "setupUI() called with: $data")

        val about = data.groupAbout
        if (about == null) {
            Log.e("GROUP_ABOUT", "groupAbout null in response")
            binding.tvGroupName.text = ""
            binding.tvGroupDesc.text = ""
            binding.tvAdminName.text = ""
            binding.imgEditGroup.visibility = View.GONE
            return
        }

        // update stored ids
        groupId = about.groupId
        groupUUID = about.groupUUID ?: groupUUID

        // Set texts & images
        binding.tvGroupName.text = about.groupName ?: ""
        binding.tvGroupDesc.text = about.groupDescription ?: ""
        binding.tvAdminName.text = "${about.adminUserFirstName ?: ""} ${about.adminUserLastName ?: ""}"

        Glide.with(this)
            .load(about.adminUserProfileImagePath)
            .placeholder(R.drawable.user)
            .into(binding.imgAdmin)

        Glide.with(this)
            .load(about.groupCoverProfileImagePath)
            .placeholder(R.drawable.strishaktilogo)
            .into(binding.imgGroupCover)

        // admin check and attach click listener
        val sessionUid = try {
            session.getUserId().toString().toInt()
        } catch (e: Exception) {
            Log.e("GROUP_ABOUT", "session user id parse error: ${e.message}")
            -1
        }

        Log.d("GROUP_ABOUT", "about.adminId=${about.adminId} sessionUid=$sessionUid")

        if (sessionUid == about.adminId) {
            binding.imgEditGroup.visibility = View.VISIBLE
            binding.imgEditGroup.isClickable = true
            binding.imgEditGroup.setOnClickListener {
                Log.d("GROUP_ABOUT", "imgEditGroup clicked")
                openEditDialog(data)
            }
        } else {
            binding.imgEditGroup.visibility = View.GONE
            binding.imgEditGroup.setOnClickListener(null)
        }
    }

    /**
     * inflate dialog_edit_group.xml (you posted it) and handle pick/save actions
     */
    private fun openEditDialog(data: GroupDetailsResponse) {
        Log.d("GROUP_ABOUT", "openEditDialog()")
        val about = data.groupAbout
        if (about == null) {
            Toast.makeText(requireContext(), "Group info missing", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_group, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etGroupName)
        val etDesc = dialogView.findViewById<android.widget.EditText>(R.id.etGroupDesc)
        val btnPick = dialogView.findViewById<Button>(R.id.btnPickCover)
        val tvCoverName = dialogView.findViewById<TextView>(R.id.tvCoverName)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveGroup)

        etName.setText(about.groupName ?: "")
        etDesc.setText(about.groupDescription ?: "")
        tvCoverName.text = if (selectedImageUri != null) FileUtil.getMimeType(requireContext(), selectedImageUri!!) else "No file chosen"

        // pick image action
        btnPick.setOnClickListener {
            Log.d("GROUP_ABOUT", "btnPick clicked -> launching picker")
            pickImageLauncher.launch("image/*")
        }

        // update tvCoverName whenever a new image is selected (we check launcher result async)
        // simple approach: poll selectedImageUri after small delay when dialog is shown OR update when launching returns
        // We'll update the tvCoverName when dialog is resumed by checking selectedImageUri.
        val alert = AlertDialog.Builder(requireContext())
            .setTitle("Edit Group")
            .setView(dialogView)
            .create()

        // Save: call updateGroup and dismiss
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newDesc = etDesc.text.toString().trim()
            if (newName.isEmpty()) {
                etName.error = "Name required"
                return@setOnClickListener
            }
            Log.d("GROUP_ABOUT", "Saving changes name='$newName' desc='$newDesc' adminId=${about.adminId}")
            // call update; selectedImageUri (if picked) will be used inside updateGroup
            updateGroup(about.adminId, newName, newDesc)
            alert.dismiss()
        }

        alert.show()

        // Update tvCoverName when user picks an image (launcher callback sets selectedImageUri)
        // We observe picked uri by checking it with a small lifecycle-aware loop:
        // Simpler: attach a one-time lifecycleScope check that updates when selectedImageUri is non-null.
        lifecycleScope.launch {
            // wait up to ~3 seconds for selection (user action) — short loop with small sleeps
            // This is not blocking UI thread because coroutine runs on Main by default but uses delay.
            // If you want instant update when user picks, the launcher above could call a function to update the shown dialog text.
            var tries = 0
            while (tries < 30) { // ~3 seconds
                if (selectedImageUri != null) {
                    try {
                        val name = FileUtil.getMimeType(requireContext(), selectedImageUri!!)
                        tvCoverName.text = name ?: "Selected"
                    } catch (e: Exception) {
                        tvCoverName.text = "Selected"
                    }
                    break
                }
                tries++
                kotlinx.coroutines.delay(100)
            }
        }
    }

    /** Update group via API (reuses selectedImageUri if present) */
    private fun updateGroup(adminId: Int, name: String, description: String) {
        Log.d("GROUP_ABOUT", "updateGroup(adminId=$adminId, name='$name')")
        val apiService = ApiClient.apiService
        val token = "Bearer ${session.getToken()}"

        val params = HashMap<String, RequestBody>()
        params["name"] = name.toRequestBody("text/plain".toMediaTypeOrNull())
        params["description"] = description.toRequestBody("text/plain".toMediaTypeOrNull())

        var coverImagePart: MultipartBody.Part? = null
        selectedImageUri?.let { uri ->
            val file: File? = FileUtil.copyUriToTempFile(requireContext(), uri)
            file?.let {
                Log.d("GROUP_ABOUT", "Uploading file: ${it.path}")
                val req = it.asRequestBody("image/*".toMediaTypeOrNull())
                coverImagePart = MultipartBody.Part.createFormData("coverImage", it.name, req)
            }
        }

        lifecycleScope.launch {
            try {
                if (groupId == 0) {
                    Log.e("GROUP_ABOUT", "Invalid groupId, cannot update")
                    Toast.makeText(requireContext(), "Invalid group id", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Log.d("GROUP_ABOUT", "Calling API updateGroup...")
                val res = apiService.updateGroup(adminId, groupId, token, params, coverImagePart)
                Log.d("GROUP_ABOUT", "updateGroup response: $res")

                if (res.isSuccessful && res.body()?.status == "Success") {
                    // 1) Immediate local UI update for instant feedback
                    requireActivity().runOnUiThread {
                        // update texts
                        binding.tvGroupName.text = name
                        binding.tvGroupDesc.text = description

                        // if a new cover image was picked, show it immediately
                        selectedImageUri?.let { uri ->
                            try {
                                Glide.with(this@GroupAboutFragment)
                                    .load(uri)
                                    .placeholder(R.drawable.strishaktilogo)
                                    .into(binding.imgGroupCover)
                            } catch (e: Exception) {
                                Log.e("GROUP_ABOUT", "Glide error: ${e.message}")
                            }
                        }
                    }

                    Toast.makeText(requireContext(), "Group updated", Toast.LENGTH_SHORT).show()

                    // 2) Re-fetch fresh details from server to keep local state consistent
                    val userId = try { session.getUserId().toString().toInt() } catch (_: Exception) { 0 }
                    if (userId != 0 && groupUUID.isNotBlank()) {
                        // call viewModel load ON MAIN thread (it should handle coroutine internally)
                        viewModel.loadGroupDetails(userId, groupUUID, 0, 5, token)
                    }
                } else {
                    val errBody = res.errorBody()?.string()
                    Log.e("GROUP_ABOUT", "Update failed: $errBody")
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("GROUP_ABOUT", "updateGroup exception: ${e.message}")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
