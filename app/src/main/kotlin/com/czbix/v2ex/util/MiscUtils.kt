package com.czbix.v2ex.util


import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.core.os.ConfigurationCompat
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.R
import com.czbix.v2ex.helper.CustomTabsHelper
import com.czbix.v2ex.network.RequestHelper

object MiscUtils {
    private val HOST_MASTER: String
    private val HOST_WWW: String
    private val PREFIX_TOPIC: String
    private val PREFIX_NODE: String
    @JvmField
    val PREFIX_MEMBER: String

    @JvmField
    val HAS_M: Boolean

    init {
        val context = AppCtx.instance
        HOST_MASTER = context.getString(R.string.master_host)
        HOST_WWW = context.getString(R.string.www_host)

        PREFIX_TOPIC = context.getString(R.string.topic_url_prefix)
        PREFIX_NODE = context.getString(R.string.node_url_prefix)
        PREFIX_MEMBER = context.getString(R.string.member_url_prefix)

        val sdkInt = VERSION.SDK_INT
        HAS_M = sdkInt >= VERSION_CODES.M
    }

    @JvmStatic
    val appUpdateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}"))

    @JvmStatic
    fun setClipboard(context: Context, data: ClipData) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.primaryClip = data

        Toast.makeText(context, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun setClipboard(context: Context, title: String?, str: String) {
        setClipboard(context, ClipData.newPlainText(title, str))
    }

    fun isEmailLink(url: String): Boolean {
        return url.startsWith("mailto:")
    }

    fun getEmailIntent(url: String): Intent {
        val uri = Uri.parse(url)
        return Intent(Intent.ACTION_SENDTO, uri)
    }

    @JvmStatic
    fun formatUrl(url: String): String {
        return when {
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> RequestHelper.BASE_URL + url
            else -> url
        }
    }

    @JvmStatic
    @JvmOverloads
    fun openUrl(activity: Activity, url: String, findMyself: Boolean = true) {
        if (isEmailLink(url)) {
            val emailIntent = getEmailIntent(url)
            activity.startActivity(emailIntent)
            return
        }

        val uri = Uri.parse(formatUrl(url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val packageManager = activity.packageManager
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        if (findMyself) {
            val foundMyself = resolveInfos.any {
                it.activityInfo.packageName == BuildConfig.APPLICATION_ID
            }

            if (foundMyself) {
                intent.`package` = BuildConfig.APPLICATION_ID
                activity.startActivity(intent)
                return
            }
        }


        val builder = CustomTabsHelper.getBuilder(activity, null)
        builder.build().launchUrl(activity, uri)
    }

    @JvmStatic
    fun getViewImageIntent(context: Context, uri: Uri): Intent {
        val googlePhotoPackage = "com.google.android.apps.photos"

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndTypeAndNormalize(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val packageManager = context.packageManager
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        val foundMyself = resolveInfos.any {
            it.activityInfo.packageName == googlePhotoPackage
        }

        if (foundMyself) {
            intent.`package` = googlePhotoPackage
        }

        return intent
    }

    fun isChineseLang(resources: Resources): Boolean {
        val lang = ConfigurationCompat.getLocales(resources.configuration)[0].language
        return lang == "zh"
    }
}

inline fun <reified T : Any> getLogTag(): String {
    return T::class.java.simpleName
}