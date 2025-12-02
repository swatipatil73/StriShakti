package com.collage.empowermentstrishakti

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.UserProfileViewModelFactory
import com.collage.empowermentstrishakti.data.model.Profile.OrgDetail
import com.collage.empowermentstrishakti.data.model.Profile.UserProfileResponse
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.UserProfileRepository
import com.collage.empowermentstrishakti.databinding.FragmentProfileTabBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.UserProfileViewModel
import java.util.*

class ProfileTabFragment : Fragment() {

    private var _binding: FragmentProfileTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var uuid: String
    private var isOwnProfile: Boolean = false
    private var loggedInUserId: Int = 0

    // keep Uris for images
    private var pickedProfileImageUri: Uri? = null
    private var pickedCoverImageUri: Uri? = null

    // spinner-backed list and selectedOrgId
    private var orgList: List<OrgDetail> = emptyList()
    private var selectedOrgId: Int? = null

    // Activity result launchers (initialized in onViewCreated)
    private lateinit var pickProfileImageLauncher: ActivityResultLauncher<String>
    private lateinit var pickCoverImageLauncher: ActivityResultLauncher<String>

    companion object {
        private const val ARG_UUID = "arg_uuid"
        private const val ARG_IS_OWN = "arg_is_own"

        fun newInstance(uuid: String, isOwnProfile: Boolean): ProfileTabFragment {
            val fragment = ProfileTabFragment()
            val args = Bundle()
            args.putString(ARG_UUID, uuid)
            args.putBoolean(ARG_IS_OWN, isOwnProfile)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uuid = it.getString(ARG_UUID) ?: ""
            isOwnProfile = it.getBoolean(ARG_IS_OWN, false)
        }

        // Initialize sessionManager + viewModel repository (no binding used here)
        val sessionManager = SessionManager(requireContext())
        loggedInUserId = sessionManager.getUserId() ?: 0

        // repository expects application context if using content resolver inside it
        val repository = UserProfileRepository(
            ApiClient.apiService,
            sessionManager,
            requireContext().applicationContext
        )
        val factory = UserProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileTabBinding.inflate(inflater, container, false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize ActivityResultLaunchers now that binding exists
        pickProfileImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                pickedProfileImageUri = it
                Glide.with(this).load(it).into(binding.imageProfile)
            }
        }
        pickCoverImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                pickedCoverImageUri = it
                Glide.with(this).load(it).into(binding.imageCover)
            }
        }

        observeViewModel()

        // fetch organizations and user profile
        viewModel.fetchOrgDetails()
        // only call fetchUserProfile if uuid is present
        if (this::uuid.isInitialized && uuid.isNotBlank()) {
            viewModel.fetchUserProfile(uuid)
        } else {
            Log.w("ProfileTabFragment", "UUID missing — cannot fetch profile")
        }

        // Edit button behaviour
        binding.btnEditProfile.setOnClickListener {
            if (isOwnProfile || (loggedInUserId > 0)) {
                toggleEditMode()
            }
        }

        // clicking image -> open picker only when in edit mode
        binding.imageProfile.setOnClickListener {
            if (binding.etFullName.isVisible) {
                pickProfileImageLauncher.launch("image/*")
            }
        }
        binding.imageCover.setOnClickListener {
            if (binding.etFullName.isVisible) {
                pickCoverImageLauncher.launch("image/*")
            }
        }

        // date picker for DOB (when editable)
        binding.etDob.setOnClickListener {
            if (binding.etDob.isEnabled) showDatePicker()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressProfile.isVisible = loading
            binding.scrollProfile.isVisible = !loading
            binding.btnEditProfile.isEnabled = !loading
        }

        viewModel.orgDetails.observe(viewLifecycleOwner) { list ->
            orgList = list
            populateOrgSpinner(list)
        }

        viewModel.updateResponse.observe(viewLifecycleOwner) { resp ->
            resp?.let {
                val msg = it.message ?: "Profile updated"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.clearUpdateResponse()
                setEditMode(false)
                // re-fetch profile to refresh UI
                if (uuid.isNotBlank()) viewModel.fetchUserProfile(uuid)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
        }

        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                bindProfileData(it)
                binding.btnEditProfile.isVisible = (it.userId == loggedInUserId)
            }
        }
    }

    private fun bindProfileData(profile: UserProfileResponse) {
        // TextViews
        binding.tvFullName.text = profile.fullName ?: ""
        binding.tvUsername.text = profile.userFirstName ?: ""
        binding.tvEmail.text = profile.userEmail ?: ""
        binding.tvGender.text = profile.userGender ?: ""
        binding.tvBio.text = profile.userMobileNumber ?: ""
        binding.tvBirth.text = profile.userBirthDate ?: ""
        binding.tvLocationDistrict.text = profile.userLocation ?: ""
        binding.tvHobbies.text = profile.userOrgname ?: ""

        // EditTexts (fill with same values for edit mode)
        binding.etFullName.setText(profile.fullName ?: "")
        binding.etBioEditable.setText(profile.userMobileNumber ?: "")
        binding.etDob.setText(profile.userBirthDate ?: "")
        binding.etAddress.setText(profile.userLocation ?: "")
        binding.etHobbies.setText(profile.userOrgname ?: "")

        // Images
        Glide.with(this).load(profile.userCoverProfileImage).into(binding.imageCover)
        Glide.with(this).load(profile.userProfileImage).into(binding.imageProfile)

        // If org list already loaded set spinner selection
        if (orgList.isNotEmpty() && !profile.userOrgname.isNullOrBlank()) {
            val idx = orgList.indexOfFirst { it.orgName == profile.userOrgname }
            if (idx >= 0) {
                binding.spinnerOrg.setSelection(idx)
                selectedOrgId = orgList[idx].orgId
            }
        }
    }

    private fun toggleEditMode() {
        val nowEditing = binding.etFullName.isVisible
        setEditMode(!nowEditing)
    }

    private fun setEditMode(enable: Boolean) {
        // Full name
        binding.tvFullName.isVisible = !enable
        binding.etFullName.isVisible = enable

        // Bio
        binding.tvBio.isVisible = !enable
        binding.etBioEditable.isVisible = enable

        // DOB
        binding.tvBirth.isVisible = !enable
        binding.etDob.isVisible = enable
        binding.etDob.isEnabled = enable

        // Address/location
        binding.tvLocationDistrict.isVisible = !enable
        binding.etAddress.isVisible = enable

        // Hobbies & spinner
        binding.tvHobbies.isVisible = !enable
        binding.etHobbies.isVisible = enable
        binding.spinnerOrg.isVisible = enable
        binding.spinnerOrg.isEnabled = enable

        // Make images clickable in edit mode (pickers)
        binding.imageProfile.isClickable = enable
        binding.imageCover.isClickable = enable

        // change button text and click action
        if (enable) {
            binding.btnEditProfile.text = "Save"
            binding.btnEditProfile.setOnClickListener {
                performSave()
            }
        } else {
            binding.btnEditProfile.text = "Edit Profile"
            binding.btnEditProfile.setOnClickListener {
                toggleEditMode()
            }
        }
    }

    private fun performSave() {
        val firstNameAndLast = splitName(binding.etFullName.text.toString().trim())
        val firstName = firstNameAndLast.first
        val lastName = firstNameAndLast.second

        val dob = binding.etDob.text.toString().trim().takeIf { it.isNotEmpty() }
        val address = binding.etAddress.text.toString().trim().takeIf { it.isNotEmpty() }
        val orgIdToSend = selectedOrgId
        val subRole = "STUDENT"

        if (firstName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter name", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateUserMultipart(
            userId = loggedInUserId,
            userDateOfBirth = dob,
            userAddress = address,
            userFirstName = firstName,
            userLastName = lastName,
            orgId = orgIdToSend,
            subRole = subRole,
            profileImageUri = pickedProfileImageUri,
            coverImageUri = pickedCoverImageUri
        )
    }

    private fun splitName(full: String): Pair<String, String> {
        val parts = full.split(" ")
        return if (parts.size <= 1) {
            Pair(full, "")
        } else {
            Pair(parts.first(), parts.drop(1).joinToString(" "))
        }
    }

    private fun populateOrgSpinner(list: List<OrgDetail>) {
        val names = list.map { it.orgName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
        binding.spinnerOrg.adapter = adapter

        // set selection if we matched earlier
        val currentName = binding.tvHobbies.text?.toString() ?: ""
        val idx = names.indexOfFirst { it == currentName }.takeIf { it >= 0 } ?: 0
        binding.spinnerOrg.setSelection(idx)

        binding.spinnerOrg.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOrgId = list[position].orgId
                // optional: show chosen name in hobbies EditText for clarity
                binding.etHobbies.setText(list[position].orgName)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedOrgId = null
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), { _, y, m, d ->
            val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
            binding.etDob.setText(formatted)
        }, year, month, day)
        dpd.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
