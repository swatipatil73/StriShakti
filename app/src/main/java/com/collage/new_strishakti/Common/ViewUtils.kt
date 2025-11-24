package com.collage.new_strishakti.Common


import android.net.Uri
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.R




object ViewUtils {

    /**
     * Toggle an "empty state" layout inside any parent view.
     * - Looks for: emptyStateLayout (required)
     * - Text: tvEmptyParticipants OR tvEmpty (fallback)
     * - RecyclerView to hide/show: rvParticipants (optional)
     * - VideoView: emptyVideo (optional)
     */
    fun showEmptyState(parentView: View, visible: Boolean, message: String = "No items") {
        val emptyLayout = parentView.findViewById<LinearLayout?>(R.id.emptyStateLayout) ?: return

        // prefer participant-specific empty text id, otherwise use generic tvEmpty
        val emptyText = parentView.findViewById<TextView?>(R.id.tvEmptyParticipants)
            ?: parentView.findViewById(R.id.tvEmpty)

        // optional views
        val rvParticipants = parentView.findViewById<RecyclerView?>(R.id.rvParticipants)
        val emptyVideo = parentView.findViewById<VideoView?>(R.id.emptyVideo)

        if (visible) {
            emptyLayout.visibility = View.VISIBLE
            emptyText?.text = message
            rvParticipants?.visibility = View.GONE

            // try to play simple looping video if present (raw/f)
            try {
                if (emptyVideo != null) {
                    val uri = Uri.parse("android.resource://${parentView.context.packageName}/${R.raw.f}")
                    emptyVideo.setVideoURI(uri)
                    emptyVideo.setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.start()
                    }
                }
            } catch (_: Exception) {
                // ignore playback errors
            }
        } else {
            emptyLayout.visibility = View.GONE
            rvParticipants?.visibility = View.VISIBLE
            try { emptyVideo?.stopPlayback() } catch (_: Exception) { }
        }
    }
}


//ViewUtils.showEmptyState(findViewById(R.id.main), true, "Failed to fetch comments.")