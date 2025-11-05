package com.collage.new_strishakti.Common


import android.net.Uri
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import com.collage.new_strishakti.R

object ViewUtils {

    fun showEmptyState(parentView: View, visible: Boolean, message: String) {
        val emptyLayout = parentView.findViewById<LinearLayout>(R.id.emptyStateLayout)
        val emptyText = parentView.findViewById<TextView>(R.id.emptyText)
        val emptyVideo = parentView.findViewById<VideoView>(R.id.emptyVideo)

        if (visible) {
            emptyLayout.visibility = View.VISIBLE
            emptyText.text = message

            val uri = Uri.parse("android.resource://${parentView.context.packageName}/${R.raw.f}")
            emptyVideo.setVideoURI(uri)
            emptyVideo.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.start()
            }
        } else {
            emptyLayout.visibility = View.GONE
            emptyVideo.stopPlayback()
        }
    }
}


//ViewUtils.showEmptyState(findViewById(R.id.main), true, "Failed to fetch comments.")