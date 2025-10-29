package com.collage.new_strishakti.data.model.post

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.Adapter.AdsAdapter
import com.collage.new_strishakti.R


class AnnouncementAdsPopupDialog : DialogFragment() {

    private var announcement: Announcement? = null
    private var adsSuperAdmin: AdsResponse? = null
    private var adsAdmin: AdsResponse? = null

    private var dismissListener: (() -> Unit)? = null

    companion object {
        private const val ARG_ANNOUNCEMENT = "arg_announcement"
        private const val ARG_ADS_SUPER_ADMIN = "arg_ads_super_admin"
        private const val ARG_ADS_ADMIN = "arg_ads_admin"

        fun newInstance(
            announcement: Announcement,
            adsSuperAdmin: AdsResponse?,
            adsAdmin: AdsResponse?
        ): AnnouncementAdsPopupDialog {
            return AnnouncementAdsPopupDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ANNOUNCEMENT, announcement)
                    putParcelable(ARG_ADS_SUPER_ADMIN, adsSuperAdmin)
                    putParcelable(ARG_ADS_ADMIN, adsAdmin)
                }
            }
        }
    }

    fun setOnDismissListener(listener: () -> Unit) {
        dismissListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            announcement = it.getParcelable(ARG_ANNOUNCEMENT)
            adsSuperAdmin = it.getParcelable(ARG_ADS_SUPER_ADMIN)
            adsAdmin = it.getParcelable(ARG_ADS_ADMIN)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_announcement_ads, container, false)

        val announcementImageView = view.findViewById<ImageView>(R.id.announcementImage)
        val adsSuperAdminRecycler = view.findViewById<RecyclerView>(R.id.adsSuperAdminRecycler)
        val adsAdminRecycler = view.findViewById<RecyclerView>(R.id.adsAdminRecycler)
        val closeButton = view.findViewById<ImageView>(R.id.closeBtn)

        // Load announcement image
        announcement?.postImageUrl?.let {
            Glide.with(this).load(it).into(announcementImageView)
        }

        // Show SuperAdmin ads
        adsSuperAdmin?.postData?.let {
            adsSuperAdminRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adsSuperAdminRecycler.adapter = AdsAdapter(it)
        }

        // Show Admin ads
        adsAdmin?.postData?.let {
            adsAdminRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adsAdminRecycler.adapter = AdsAdapter(it)
        }

        closeButton.setOnClickListener { dismiss() }

        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // Transparent bg
            setDimAmount(0.5f)  // Dim behind the dialog (0f = no dim, 1f = full black)
        }
    }

}
