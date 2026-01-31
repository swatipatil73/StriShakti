package com.collage.empowermentstrishakti.data.model.Profile


import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.DialogFragment

class VideoPopupFragment : DialogFragment() {

    companion object {
        private const val ARG_VIDEO_URL = "video_url"

        fun newInstance(videoUrl: String): VideoPopupFragment {
            val fragment = VideoPopupFragment()
            val args = Bundle()
            args.putString(ARG_VIDEO_URL, videoUrl)
            fragment.arguments = args
            return fragment
        }
    }

    private var videoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoUrl = arguments?.getString(ARG_VIDEO_URL)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val videoView = VideoView(requireContext())
        videoView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return videoView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoView = view as VideoView

        videoUrl?.let { url ->
            val uri = Uri.parse(url)
            videoView.setVideoURI(uri)
            val mediaController = MediaController(requireContext())
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            videoView.setOnPreparedListener { mp: MediaPlayer ->
                mp.isLooping = false
                videoView.start()
            }
        }

        view.setOnClickListener {
            dismiss() // Tap anywhere to close
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}
