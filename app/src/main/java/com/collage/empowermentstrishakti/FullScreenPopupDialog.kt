package com.collage.empowermentstrishakti



import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.collage.empowermentstrishakti.data.model.SavedPost.SavedPostData
import de.hdodenhof.circleimageview.CircleImageView
class FullScreenPopupDialog(
    private val post: SavedPostData,
    private val listener: PopupListener
) : DialogFragment() {

    interface PopupListener {
        fun onDeleteClicked(post: SavedPostData)
    }

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_MaterialComponents_Dialog)
        val view = requireActivity().layoutInflater.inflate(R.layout.popup_fullscreen, null)

        val ivFullImage = view.findViewById<ImageView>(R.id.ivFullImage)
        val tvCenterText = view.findViewById<TextView>(R.id.tvCenterText)
        val playerView = view.findViewById<PlayerView>(R.id.playerView)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val ivUser = view.findViewById<CircleImageView>(R.id.ivUser)
        val ivDelete = view.findViewById<ImageView>(R.id.ivDelete)
        val tvPostName = view.findViewById<TextView>(R.id.tvPostName)
        val bottomBar = view.findViewById<View>(R.id.bottomBar)

        // Top data
        tvUserName.text = post.userName ?: ""
        Glide.with(requireContext()).load(post.userProfileImageUrl).placeholder(R.drawable.user).into(ivUser)
        tvPostName.text = post.description ?: post.postName ?: ""

        fun isValid(u: String?) =
            !u.isNullOrBlank() && !u.contains("emptyPostImageUrl") && !u.contains("emptyVideoThumbnailUrl")

        val img = post.postImageUrl
        val thumb = post.videoThumbnailUrl
        val type = post.postType ?: ""

        val isVideo = type.contains("video", true) ||
                (isValid(img) && img!!.endsWith(".mp4", true))
        val isImage = type.contains("image", true)
        val isText = !isVideo && !isImage

        when {
            isVideo -> {
                tvCenterText.visibility = View.GONE
                bottomBar.visibility = View.VISIBLE

                playerView.visibility = View.VISIBLE
                ivFullImage.visibility = View.GONE

                val url = if (isValid(img) && img!!.endsWith(".mp4", true)) img else thumb
                if (url != null) initPlayer(Uri.parse(url), playerView)
            }

            isImage -> {
                tvCenterText.visibility = View.GONE
                bottomBar.visibility = View.VISIBLE

                playerView.visibility = View.GONE
                ivFullImage.visibility = View.VISIBLE

                if (isValid(img)) Glide.with(requireContext()).load(img).into(ivFullImage)
            }

            else -> {
                // TEXT POST
                playerView.visibility = View.GONE
                ivFullImage.visibility = View.GONE
                bottomBar.visibility = View.GONE   // hide duplicate bottom text

                tvCenterText.visibility = View.VISIBLE
                tvCenterText.text = post.description
                    ?: post.postName
                            ?: "Text Post"
            }
        }

        ivDelete.setOnClickListener {
            listener.onDeleteClicked(post)
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    private fun initPlayer(uri: Uri, playerView: PlayerView) {
        player = ExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        val item = MediaItem.fromUri(uri)
        player?.setMediaItem(item)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }
}
