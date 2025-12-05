package com.collage.empowermentstrishakti

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.GroupPostAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.databinding.FragmentGroupPostBinding
import com.google.gson.Gson

class GroupPostFragment : Fragment() {

    private lateinit var binding: FragmentGroupPostBinding
    private lateinit var session: SessionManager

    private var responseData: GroupDetailsResponse? = null

    companion object {
        fun newInstance(data: GroupDetailsResponse): GroupPostFragment {
            val fragment = GroupPostFragment()
            val bundle = Bundle()
            bundle.putString("data", Gson().toJson(data))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupPostBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())

        // Get response passed from Activity
        val json = arguments?.getString("data")
        if (!json.isNullOrEmpty()) {
            responseData = Gson().fromJson(json, GroupDetailsResponse::class.java)
        }

        setupRecycler()
        checkAdminAndShowUpload()

        return binding.root
    }

    private fun setupRecycler() {
        val posts = responseData?.postDetails ?: emptyList()

        binding.recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPosts.adapter = GroupPostAdapter(posts)
    }

    private fun checkAdminAndShowUpload() {
        val adminId = responseData?.groupAbout?.adminId
        val loginUserId = session.getUserId()

        if (adminId == loginUserId) {
            binding.layoutUploadSection.visibility = View.VISIBLE
        } else {
            binding.layoutUploadSection.visibility = View.GONE
        }
    }
}
