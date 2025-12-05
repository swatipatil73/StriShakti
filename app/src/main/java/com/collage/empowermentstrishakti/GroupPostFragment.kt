package com.collage.empowermentstrishakti.ui.group

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.GroupPostAdapter
import com.collage.empowermentstrishakti.Adapter.PagePostAdapter
import com.collage.empowermentstrishakti.Common.InteractionManagerImpl
import com.collage.empowermentstrishakti.Common.SessionManager

import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.data.model.post.PostActionsVMFactory
import com.collage.empowermentstrishakti.data.network.ApiClient.apiService
import com.collage.empowermentstrishakti.data.repository.LikeRepository
import com.collage.empowermentstrishakti.data.repository.PostActionsRepository
import com.collage.empowermentstrishakti.data.repository.SavedPostsRepository
import com.collage.empowermentstrishakti.databinding.FragmentGroupPostBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionEvent

import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionsViewModel
import kotlinx.coroutines.launch
import com.google.gson.Gson


class GroupPostFragment : Fragment() {

    private var _binding: FragmentGroupPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private var responseData: GroupDetailsResponse? = null

    private lateinit var interactionManager: InteractionManagerImpl
    private lateinit var postActionsVm: PostActionsViewModel
    private lateinit var pickLauncher: ActivityResultLauncher<String>
    private var selectedUri: Uri? = null

    companion object {
        private const val ARG_JSON = "data"
        fun newInstance(data: GroupDetailsResponse): GroupPostFragment {
            val f = GroupPostFragment()
            val b = Bundle()
            b.putString(ARG_JSON, Gson().toJson(data))
            f.arguments = b
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(requireContext())

        arguments?.getString(ARG_JSON)?.let { json ->
            responseData = try { Gson().fromJson(json, GroupDetailsResponse::class.java) } catch (t: Throwable) { null }
        }

        pickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedUri = uri
            // update UI text if view exists
            if (_binding != null) binding.tvSelectedFile.text = if (uri != null) "File selected" else "No file chosen"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroupPostBinding.inflate(inflater, container, false)

        // repos / managers
        val api = apiService
        val postActionsRepo = PostActionsRepository(api)
        val savedRepo = SavedPostsRepository(api)
        interactionManager = InteractionManagerImpl(
            likeRepo = LikeRepository,
            postActionsRepo = postActionsRepo,
            savedPostsRepo = savedRepo
        )

        // PostActions VM factory (for delete/save events)
        val postActionsFactory = PostActionsVMFactory(postActionsRepo)
        postActionsVm = ViewModelProvider(requireActivity(), postActionsFactory).get(PostActionsViewModel::class.java)
        val savedPostIds = mutableSetOf<Int>()

        val adapter = GroupPostAdapter(
            savedIdsProvider = { savedPostIds.map { it.toString() } },  // FIXED
            scope = viewLifecycleOwner.lifecycleScope,
            interactionManager = interactionManager,
            vm = postActionsVm,
            onItemClick = { }
        )





        binding.recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPosts.adapter = adapter

        // submit initial posts (group model list)
        val posts = responseData?.postDetails ?: emptyList()
        adapter.submitList(posts.toList())

        // show/hide upload
        val adminId = responseData?.groupAbout?.adminId
        binding.layoutUploadSection.visibility = if (adminId == session.getUserId()) View.VISIBLE else View.GONE

        // pick/share
        binding.btnPickMedia.setOnClickListener { pickLauncher.launch("*/*") }
        //binding.btnSharePost.setOnClickListener { doSharePost(adapter) }

        // collect PostActions events to update UI (delete/save)
        viewLifecycleOwner.lifecycleScope.launch {
            postActionsVm.events.collect { ev ->
                when (ev) {
                    is PostActionEvent.Deleted -> {
                        val newList = adapter.currentList.filter { it.postId != ev.postId }.toList()
                        adapter.submitList(newList)
                        Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                    }
                    is PostActionEvent.Saved -> {
                        Toast.makeText(requireContext(), ev.message, Toast.LENGTH_SHORT).show()
                        val newList = adapter.currentList.map {
                            if (it.postId == ev.postId) it.copy(postSaved = true) else it
                        }.toList()
                        adapter.submitList(newList)

                        adapter.submitList(newList) // submit a new list instance
                    }
                    is PostActionEvent.Error -> {
                        Toast.makeText(requireContext(), ev.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        return binding.root
    }
    private fun doSharePost(adapter: PagePostAdapter) {
        val text = binding.etPostContent.text?.toString()?.trim().orEmpty()
        if (text.isBlank() && selectedUri == null) {
            Toast.makeText(requireContext(), "Enter text or choose a file", Toast.LENGTH_SHORT).show()
            return
        }

        // create optimistic PostDetail (minimal fields) — reuse your PostDetail model
        val optimisticId = (-1 * (System.currentTimeMillis() % Int.MAX_VALUE)).toInt()
        val optimistic = com.collage.empowermentstrishakti.data.model.PostDetail(
            postId = optimisticId,
            userId = session.getUserId() ?: 0,
            userProfileImageUrl = null,
            userName = session.getUserId()?.toString(),
            postImageURl = selectedUri?.toString(),
            postType = determinePostTypeFromUri(selectedUri),
            postCreatedAt = isoNow(),
            postName = text,
            totalCountOFReact = 0,
            totalComments = 0,
            description = text
        )

        // prepend locally via adapter's backing list (adapter uses submitList)
        val newList = listOf(optimistic) + adapter.currentList
        adapter.submitList(newList)

        // clear UI
        binding.etPostContent.setText("")
        binding.tvSelectedFile.text = "No file chosen"

        // call your group upload flow: here we assume GroupDetailViewModel exists with uploadGroupPost
        // If you don't have that implemented yet, call your repo directly or follow Page->Group pattern.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = session.getToken() ?: ""
                if (token.isBlank()) {
                    Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // TODO: call viewModel.uploadGroupPost(...) as you implemented for Page
                // After server success replace the optimistic item (find by postId and replace with server object).
                // For now we keep optimistic and rely on later refresh to update server data.

            } catch (e: Exception) {
                Log.e("GroupPost", "upload failed", e)
                // remove optimistic item on failure
                val filtered = adapter.currentList.filter { it.postId != optimisticId }
                adapter.submitList(filtered)
            } finally {
                selectedUri = null
            }
        }
    }

    private fun determinePostTypeFromUri(uri: Uri?): String? {
        if (uri == null) return null
        val type = requireContext().contentResolver.getType(uri) ?: return null
        return when {
            type.startsWith("image") -> "image"
            type.startsWith("video") -> "video"
            else -> "file"
        }
    }

    private fun isoNow(): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.format(java.util.Date())
        } catch (e: Exception) {
            System.currentTimeMillis().toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
