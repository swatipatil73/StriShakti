package com.collage.empowermentstrishakti


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.Adapter.PagePostAdapter
import com.collage.empowermentstrishakti.Common.InteractionManagerImpl
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.PostDetail
import com.collage.empowermentstrishakti.data.model.post.PostActionsVMFactory
import com.collage.empowermentstrishakti.data.network.ApiClient.apiService
import com.collage.empowermentstrishakti.data.repository.LikeRepository
import com.collage.empowermentstrishakti.data.repository.PostActionsRepository
import com.collage.empowermentstrishakti.data.repository.SavedPostsRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionEvent
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionsViewModel

import kotlinx.coroutines.launch



import kotlinx.coroutines.launch

class PostFragment : Fragment() {

    private lateinit var layoutUpload: LinearLayout
    private lateinit var etPostContent: EditText
    private lateinit var btnPickMedia: Button
    private lateinit var tvSelectedFile: TextView
    private lateinit var btnSharePost: Button
    private lateinit var tvNoData: TextView
    private lateinit var recyclerPosts: RecyclerView
    private lateinit var adapter: PagePostAdapter
    private lateinit var vm: PageDetailViewModel
    private val session: SessionManager by lazy { SessionManager(requireContext()) }

    private var selectedUri: Uri? = null
    private lateinit var pickLauncher: ActivityResultLauncher<String>

    // NOTE: set these based on current page context (pass via fragment args)
    private var pageAdminUserId: Int = -1
    private var pageId: Int = -1

    // interaction manager
    private lateinit var interactionManager: InteractionManagerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(requireActivity()).get(PageDetailViewModel::class.java)

        // init args if passed (use -1 default)
        arguments?.let { args ->
            pageAdminUserId = args.getInt("pageAdminUserId", -1)
            pageId = args.getInt("pageId", -1)
        }

        pickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedUri = uri
            if (this::tvSelectedFile.isInitialized) {
                tvSelectedFile.text = if (uri != null) "File selected" else "No file chosen"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_post, container, false)
        layoutUpload = v.findViewById(R.id.layoutUploadSection)
        etPostContent = v.findViewById(R.id.etPostContent)
        btnPickMedia = v.findViewById(R.id.btnPickMedia)
        tvSelectedFile = v.findViewById(R.id.tvSelectedFile)
        btnSharePost = v.findViewById(R.id.btnSharePost)
        tvNoData = v.findViewById(R.id.tvNoData)
        recyclerPosts = v.findViewById(R.id.recyclerPosts)

        // --- repos & interaction manager ---
        val api = apiService // your ApiClient.apiService
        val postActionsRepo = PostActionsRepository(api)
        val savedRepo = SavedPostsRepository(api)
        interactionManager = InteractionManagerImpl(
            likeRepo = LikeRepository,
            postActionsRepo = postActionsRepo,
            savedPostsRepo = savedRepo
        )

        // --- obtain PostActionsViewModel via factory (must be done before adapter) ---
        val postActionsFactory = PostActionsVMFactory(postActionsRepo)
        val postActionsVm = ViewModelProvider(requireActivity(), postActionsFactory)
            .get(PostActionsViewModel::class.java)

        // --- create adapter (PASS vm here) BEFORE assigning to RecyclerView ---
        adapter = PagePostAdapter(
            scope = viewLifecycleOwner.lifecycleScope,
            interactionManager = interactionManager,
            vm = postActionsVm,
            onItemClick = { /* open post detail if you want */ }
        )

        // --- RecyclerView setup (now safe) ---
        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        recyclerPosts.adapter = adapter

        // --- pick / share listeners ---
        btnPickMedia.setOnClickListener { pickLauncher.launch("*/*") }
        btnSharePost.setOnClickListener { doSharePost() }

        // --- fallback for page admin ---
        if (pageAdminUserId == -1) {
            pageAdminUserId = session.getUserId() ?: -1
            Log.d("PostFragment", "pageAdminUserId fallback to session: $pageAdminUserId")
        }

        // --- pageId observer (fill if unknown) ---
        if (pageId == -1) {
            vm.pageDetails.observe(viewLifecycleOwner) { resp ->
                val serverPageId = resp?.pageAbout?.pagesId ?: -1
                if (serverPageId != -1) {
                    pageId = serverPageId
                    Log.d("PostFragment", "pageId set from server (pagesId): $pageId")
                }
            }
        }

        // --- observe VM lists and update adapter ---
        vm.pageDetails.observe(viewLifecycleOwner) { resp ->
            val adminId = resp?.pageAbout?.adminId ?: -1
            layoutUpload.visibility = if (adminId == session.getUserId()) View.VISIBLE else View.GONE

            val posts = resp?.postDetails ?: emptyList()
            adapter.submitList(posts.toList())
            tvNoData.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.postDetails.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts.toList())
            tvNoData.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        // --- upload state handling ---
        vm.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PageDetailViewModel.UploadState.Uploading -> {
                    btnSharePost.isEnabled = false
                    btnSharePost.text = "Posting..."
                }
                is PageDetailViewModel.UploadState.Success -> {
                    btnSharePost.isEnabled = true
                    btnSharePost.text = "Share"
                    Toast.makeText(requireContext(), "Post uploaded", Toast.LENGTH_SHORT).show()
                    // If your UploadState contains optimisticLocalId + createdPost, update accordingly
                    // (adjust fields to your actual UploadState implementation)
                }
                is PageDetailViewModel.UploadState.Error -> {
                    btnSharePost.isEnabled = true
                    btnSharePost.text = "Share"
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_LONG).show()
                }
                else -> {
                    btnSharePost.isEnabled = true
                    btnSharePost.text = "Share"
                }
            }
        }

        // --- collect PostActionsViewModel events to update adapter ---
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            postActionsVm.events.collect { event ->
                when (event) {
                    is PostActionEvent.Deleted -> {
                        val newList = adapter.currentList.filter { it.postId != event.postId }
                        adapter.submitList(newList)
                        Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                    }
                    is PostActionEvent.Saved -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                    is PostActionEvent.Error -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return v
    }


    private fun doSharePost() {
        val text = etPostContent.text?.toString()?.trim().orEmpty()
        if (text.isBlank() && selectedUri == null) {
            Toast.makeText(requireContext(), "Enter text or choose a file", Toast.LENGTH_SHORT).show()
            return
        }

        // minimal optimistic PostDetail
        val nowIso = isoNow()
        val optimisticLocalId = (-1 * (System.currentTimeMillis() % Int.MAX_VALUE)).toInt()
        val optimistic = PostDetail(
            postId = optimisticLocalId, // temporary negative id
            userId = session.getUserId(),
            userProfileImageUrl = null,
            userName = session.getUserId()?.toString(),
            postImageURl = selectedUri?.toString(),
            postType = determinePostTypeFromUri(selectedUri),
            postCreatedAt = nowIso,
            postName = text,
            totalCountOFReact = 0,
            totalComments = 0,
            description = text
        )

        // Insert optimistic item into UI via VM
        vm.prependPostOptimistic(optimistic)

        // clear UI
        etPostContent.setText("")
        tvSelectedFile.text = "No file chosen"

        // start upload (use lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = session.getToken()
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "You are not logged in.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (pageId == -1) {
                    Toast.makeText(requireContext(),
                        "Page not ready yet. Please try again in a moment.",
                        Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // ensure uri is accessible
                if (selectedUri != null) {
                    var streamOk = true
                    try {
                        requireContext().contentResolver.openInputStream(selectedUri!!)?.close()
                    } catch (e: Exception) {
                        streamOk = false
                    }
                    if (!streamOk) {
                        Toast.makeText(requireContext(),
                            "Upload failed. Unable to access the file for the upload.",
                            Toast.LENGTH_LONG).show()
                        vm.removePostByLocalId(optimisticLocalId)
                        return@launch
                    }
                }

                // call ViewModel upload; VM will emit UploadState and replace optimistic when done
                vm.uploadPagePost(
                    context = requireContext(),
                    pageAdminUserId = pageAdminUserId.takeIf { it != -1 } ?: session.getUserId() ?: -1,
                    pageId = pageId,
                    postName = text,
                    postType = optimistic.postType ?: "",
                    selectedUri = selectedUri,
                    token = token,
                    optimisticLocalId = optimisticLocalId
                )
            } catch (e: Exception) {
                Log.d("PostUpload", "Upload failed", e)
                Toast.makeText(requireContext(), "Upload failed. Try again.", Toast.LENGTH_SHORT).show()
                vm.removePostByLocalId(optimisticLocalId)
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

    override fun onDestroyView() {
        super.onDestroyView()
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
}
