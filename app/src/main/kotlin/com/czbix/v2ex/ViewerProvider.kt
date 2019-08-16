package com.czbix.v2ex

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.czbix.v2ex.network.GlideRequests
import com.czbix.v2ex.util.MiscUtils
import java.io.File
import java.io.IOException

class ViewerProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?,
                       sortOrder: String?): Cursor? {
        @Suppress("NAME_SHADOWING")
        var projection = projection
        // ContentProvider has already checked granted permissions
        val file = getFileForUri(uri)

        if (projection == null) {
            projection = COLUMNS
        }

        var cols = arrayOfNulls<String>(projection.size)
        var values = arrayOfNulls<Any>(projection.size)
        var i = 0
        for (col in projection) {
            when (col) {
                OpenableColumns.DISPLAY_NAME -> {
                    cols[i] = OpenableColumns.DISPLAY_NAME
                    values[i++] = file.name
                }
                OpenableColumns.SIZE -> {
                    cols[i] = OpenableColumns.SIZE
                    values[i++] = file.length()
                }
            }
        }

        cols = cols.sliceArray(0 until i)
        values = values.sliceArray(0 until i)

        val cursor = MatrixCursor(cols, 1)
        cursor.addRow(values)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return "application/octet-stream"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("No external inserts")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("No external updates")
    }

    override fun delete(uri: Uri, selection: String?,
                        selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("No external deletes")
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // ContentProvider has already checked granted permissions
        val file = getFileForUri(uri)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun getFileForUri(uri: Uri): File {
        val path = uri.encodedPath

        if (path != "/image") {
            throw IllegalArgumentException("Invalid file path")
        }

        var file = File(tempPath)
        try {
            file = file.canonicalFile
        } catch (e: IOException) {
            throw IllegalArgumentException("Failed to resolve canonical path for $file")
        }

        return file
    }

    companion object {
        private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".viewer"
        var tempPath: String? = null

        private val COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)

        fun getUriForFile(): Uri {
            return Uri.Builder().scheme("content")
                    .authority(AUTHORITY).encodedPath("image").build()
        }

        fun viewImage(context: Context, glide: GlideRequests, url: String) {
            @Suppress("NAME_SHADOWING")
            val url = MiscUtils.formatUrl(url)
            glide.downloadOnly().load(url).into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    tempPath = resource.canonicalPath

                    val contentUri = getUriForFile()

                    val intent = MiscUtils.getViewImageIntent(context, contentUri)
                    context.startActivity(intent)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        }
    }
}
