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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.Adapter.PagePostAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.PostDetail
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



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
    private val session by lazy { SessionManager(requireContext()) }

    private var selectedUri: Uri? = null
    private lateinit var pickLauncher: ActivityResultLauncher<String>
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // NOTE: set these based on current page context (pass via fragment args)
    private var pageAdminUserId: Int = -1
    private var pageId: Int = -1

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
            view?.findViewById<TextView>(R.id.tvSelectedFile)?.text =
                if (uri != null) "File selected" else "No file chosen"
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

        adapter = PagePostAdapter(
            onItemClick = { /* open post detail if you want */ },
            onLikeClick = { post, pos -> onLikeClicked(post, pos) },
            onCommentClick = { post, pos -> onCommentClicked(post, pos) },
            onShareClick = { post, pos -> onShareClicked(post, pos) },
            onMoreClick = { post, pos, view -> onMoreClicked(post, pos, view) }
        )
        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        recyclerPosts.adapter = adapter

        btnPickMedia.setOnClickListener {
            pickLauncher.launch("*/*") // accept image or video
        }

        btnSharePost.setOnClickListener {
            doSharePost()
        }

        // if pageAdminUserId not provided, fallback to current user
        if (pageAdminUserId == -1) {
            pageAdminUserId = session.getUserId()
            Log.d("PostFragment", "pageAdminUserId fallback to session: $pageAdminUserId")
        }

        // If pageId not supplied via args, observe pageDetails and set pageId when available
        if (pageId == -1) {
            vm.pageDetails.observe(viewLifecycleOwner) { resp ->
                val serverPageId = resp?.pageAbout?.pagesId ?: -1   // <-- use pagesId
                if (serverPageId != -1) {
                    pageId = serverPageId
                    Log.d("PostFragment", "pageId set from server (pagesId): $pageId")
                } else {
                    Log.w("PostFragment", "pageId still unknown (will block upload until set)")
                }
            }
        } else {
            Log.d("PostFragment", "pageId from args: $pageId")
        }


        // Observe the pageDetails and update UI
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
                }
                is PageDetailViewModel.UploadState.Error -> {
                    btnSharePost.isEnabled = true
                    btnSharePost.text = "Share"
                    // friendly message
                    Toast.makeText(requireContext(),
                        "Upload failed. Unable to access the file for the upload.",
                        Toast.LENGTH_LONG).show()
                    Log.d("PostUpload", "Upload error detail: ${state.message}")
                }
                else -> {
                    btnSharePost.isEnabled = true
                    btnSharePost.text = "Share"
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
        val optimistic = PostDetail(
            postId = Int.MIN_VALUE, // temporary negative id to identify optimistic item
            userId = session.getUserId(),
            userProfileImageUrl = null,
            userName = session.getUserId().toString(),
            postImageURl = selectedUri?.toString(),
            postType = determinePostTypeFromUri(selectedUri),
            postCreatedAt = nowIso,
            postName = text,
            totalCountOFReact = 0,
            totalComments = 0,
            description = text
        )

        // Insert optimistic item into UI
        vm.prependPostOptimistic(optimistic)

        // clear UI
        etPostContent.setText("")
        tvSelectedFile.text = "No file chosen"
        // keep selectedUri until upload begins

        // start upload
        coroutineScope.launch {
            try {
                // validate auth token
                val token = session.getToken()
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "You are not logged in.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // ensure pageId is valid before calling API
                if (pageId == -1) {
                    Toast.makeText(requireContext(),
                        "Page not ready yet. Please try again in a moment.",
                        Toast.LENGTH_SHORT).show()
                    // (Optional) remove optimistic post if you want:
                    // vm.removePostByLocalId(optimistic.postId) // implement this in VM if desired
                    return@launch
                }

                // if a file was chosen, ensure we can access it before calling ViewModel
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
                        // (Optional) remove optimistic post if you want:
                        // vm.removePostByLocalId(optimistic.postId)
                        return@launch
                    }
                }

                // CALL ViewModel upload with context — ViewModel should copy Uri -> temp File and call repo
                vm.uploadPagePost(
                    context = requireContext(),
                    pageAdminUserId = pageAdminUserId.takeIf { it != -1 } ?: session.getUserId(),
                    pageId = pageId,
                    postName = text,
                    postType = optimistic.postType ?: "",
                    selectedUri = selectedUri,
                    token = token,
                    optimisticLocalId = optimistic.postId
                )

            } catch (e: Exception) {
                Log.d("PostUpload", "Upload failed", e)
                Toast.makeText(requireContext(), "Upload failed. Try again.", Toast.LENGTH_SHORT).show()
            } finally {
                // clear selectedUri after upload started
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

    private fun onLikeClicked(post: PostDetail, pos: Int) {
        Toast.makeText(requireContext(), "Like clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onCommentClicked(post: PostDetail, pos: Int) {
        Toast.makeText(requireContext(), "Comment clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onShareClicked(post: PostDetail, pos: Int) {
        Toast.makeText(requireContext(), "Share clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onMoreClicked(post: PostDetail, pos: Int, anchor: View) {
        Toast.makeText(requireContext(), "More clicked", Toast.LENGTH_SHORT).show()
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
        coroutineScope.cancel()
    }
}
