package com.czbix.v2ex.ui.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.czbix.v2ex.R

class GoogleLoginDialog : DialogFragment() {
    private var webView: WebView? = null
    private lateinit var signInUrl: String
    private lateinit var listener: GoogleSignInListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments;
        signInUrl = args.getString(KEY_SIGN_IN_URL)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = context as GoogleSignInListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle(R.string.desc_google_sign_in)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        webView = WebView(context)
        return webView
    }

    override fun onDestroyView() {
        super.onDestroyView()

        webView?.destroy()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initWebView(webView!!)
    }

    private fun initWebView(webView: WebView) {
        webView.settings.let {
            it.javaScriptEnabled = true
        }

        webView.loadUrl(signInUrl)
        webView.setWebViewClient(client)
    }

    private val client: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (!url.startsWith(CALLBACK_URL)) {
                return false
            }

            listener.onGoogleSignedIn(url)
            // dismiss in next message cycle
            webView!!.post {
                dismiss()
            }
            return true
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        listener.onGoogleSignInCancelled()
    }

    interface GoogleSignInListener {
        fun onGoogleSignedIn(url: String)
        fun onGoogleSignInCancelled()
    }

    companion object {
        const val CALLBACK_URL = "https://www.v2ex.com/auth/google"

        private const val KEY_SIGN_IN_URL = "sign_in_url";

        fun newInstance(url: String): GoogleLoginDialog {
            val instance = GoogleLoginDialog()
            val args = Bundle().apply {
                putString(KEY_SIGN_IN_URL, url)
            }

            instance.arguments = args

            return instance
        }
    }
}
