package com.collage.empowermentstrishakti.Common

import android.content.Context
import android.net.Uri
import java.io.File
object FileUtil {
    fun copyUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val resolver = context.contentResolver
            val input = resolver.openInputStream(uri) ?: return null

            val mime = resolver.getType(uri) ?: "application/octet-stream"
            val ext = when {
                mime.startsWith("image/") -> ".jpg"
                mime.startsWith("video/") -> ".mp4"
                mime == "application/pdf" -> ".pdf"
                else -> ".bin"
            }

            val out = File.createTempFile("upload_", ext, context.cacheDir)
            out.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
            input.close()
            out
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
}
