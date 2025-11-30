package com.collage.empowermentstrishakti


import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R

class ImageDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_image_view, null)

        val imageUrl = arguments?.getString("image_url")
        val imageView = view.findViewById<ImageView>(R.id.dialogImage)

        // Load image in dialog
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.imageplacehoder)
            .into(imageView)

        builder.setView(view)

        // Close dialog when clicked
        imageView.setOnClickListener { dismiss() }

        return builder.create()
    }

    companion object {
        fun newInstance(url: String?): ImageDialog {
            val args = Bundle()
            args.putString("image_url", url)
            val fragment = ImageDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.white)
    }
}
