package com.collage.new_strishakti

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Factory.UserProfileViewModelFactory
import com.collage.new_strishakti.data.model.Profile.UserProfileResponse
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.UserProfileRepository
import com.collage.new_strishakti.databinding.FragmentProfileTabBinding
import com.collage.new_strishakti.ui.RegisterViewModel.UserProfileViewModel

private const val ARG_UUID = "uuid"
private const val ARG_IS_OWN_PROFILE = "is_own_profile"

class ProfileTabFragment : Fragment() {

    private var _binding: FragmentProfileTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var uuid: String
    private var isOwnProfile: Boolean = false
    var loggedInUserId:Int=0

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

        // Initialize ViewModel (assuming you already have repository instance)
        val sessionManager = SessionManager(requireContext())

         loggedInUserId = sessionManager.getUserId()
        val repository = UserProfileRepository(ApiClient.apiService, sessionManager)
        val factory = UserProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.fetchUserProfile(uuid)

        binding.btnEditProfile.setOnClickListener {
            // TODO: Handle edit profile click
            if (isOwnProfile) {
                toggleEditMode()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let { userProfile ->
                // Bind all profile data
                bindProfileData(userProfile)

                // Set username
                binding.tvUsername.text = "${userProfile.userFirstName} ${userProfile.userLastName}"

                // Show Edit Profile button only if this is the logged-in user

                binding.btnEditProfile.isVisible = (userProfile.userId == loggedInUserId)
            }
        }


        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.scrollProfile.isVisible = !isLoading
            // Optionally show progress bar
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun bindProfileData(profile: UserProfileResponse) {
        binding.tvFullName.text = profile.fullName
        binding.tvUsername.text = profile.userFirstName
        binding.tvEmail.text = profile.userEmail
        binding.tvGender.text = profile.userGender
        binding.tvBio.text = profile.userMobileNumber
        binding.tvBirth.text = profile.userBirthDate
        binding.tvLocationDistrict.text = profile.userLocation
        binding.tvHobbies.text = profile.userOrgname

        // Load images using Glide or Picasso
        Glide.with(this).load(profile.userCoverProfileImage).into(binding.imageCover)
        Glide.with(this).load(profile.userProfileImage).into(binding.imageProfile)
    }

    private fun toggleEditMode() {
        // TODO: Switch between TextView and EditText for editing
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
