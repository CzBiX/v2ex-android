package com.czbix.v2ex.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.czbix.v2ex.R
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.*
import com.czbix.v2ex.google.GoogleHelper
import com.czbix.v2ex.helper.CustomTabsHelper
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.model.LoginResult
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.parser.Parser
import com.czbix.v2ex.ui.fragment.TwoFactorAuthDialog
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.await
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.toObservable

/**
 * A login screen that offers login via account/password.
 */
class LoginActivity : BaseActivity(), View.OnClickListener {
    private var mAuthTask: Subscription? = null
    private var mCaptchaTask: Subscription? = null

    // UI references.
    private lateinit var mAccountView: EditText
    private lateinit var mPwdView: EditText
    private lateinit var mCaptchaImageView: ImageView
    private lateinit var mCaptchaView: EditText
    private lateinit var mProgressView: View
    private lateinit var mLoginFormView: View

    private var mSignInFormData: Parser.SignInFormData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAccountView = findViewById(R.id.account) as EditText
        mPwdView = findViewById(R.id.password) as EditText
        mPwdView.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            private val mActionIdSignIn = resources.getInteger(R.integer.id_action_sign)

            override fun onEditorAction(textView: TextView, id: Int, keyEvent: KeyEvent?): Boolean {
                if (id == mActionIdSignIn || id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin()
                    return true
                }
                return false
            }
        })
        mCaptchaImageView = findViewById(R.id.image_captcha) as ImageView
        mCaptchaView = findViewById(R.id.captcha) as EditText

        arrayOf(R.id.sign_in, R.id.sign_up, R.id.reset_password, R.id.image_captcha).forEach {
            findViewById(it).setOnClickListener(this)
        }

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)

        // remove left auth code dialog
        supportFragmentManager.findFragmentByTag(TAG_AUTH_CODE)?.let {
            (it as TwoFactorAuthDialog).dismiss()
        }

        loadCaptcha()
    }

    private fun loadCaptcha() {
        mCaptchaTask = RequestHelper.getSignInForm()
                .await({ signInFormData ->
                    mSignInFormData = signInFormData
                    mCaptchaView.visibility = View.VISIBLE
                    Glide.with(this).load(RequestHelper.getCaptchaImageUrl(signInFormData.once))
                            .asBitmap()
                            .into(mCaptchaImageView)
                }, {
                    LogUtils.e(TAG, "Get signn in form failed.", it)
                })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in -> attemptLogin()
            R.id.sign_up -> {
                val uri = Uri.parse("https://www.v2ex.com/signup?r=aliuwr")
                CustomTabsHelper.getBuilder(this, null).build().launchUrl(this, uri)
            }
            R.id.reset_password -> {
                val uri = Uri.parse("https://www.v2ex.com/forgot")
                CustomTabsHelper.getBuilder(this, null).build().launchUrl(this, uri)
            }
            R.id.image_captcha -> loadCaptcha()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mAuthTask?.unsubscribe()
        mCaptchaTask?.unsubscribe()
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        mAccountView.error = null
        mPwdView.error = null

        // Store values at the time of the login attempt.
        val email = mAccountView.text.toString()
        val password = mPwdView.text.toString()
        val captcha = mCaptchaView.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPwdView.error = getString(R.string.error_field_required)
            focusView = mPwdView
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mAccountView.error = getString(R.string.error_field_required)
            focusView = mAccountView
            cancel = true
        }

        if (TextUtils.isEmpty(captcha)) {
            mCaptchaView.error = getString(R.string.error_field_required)
            focusView = mCaptchaView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = loginUser(email, password, captcha)
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        mLoginFormView.visibility = if (show) View.GONE else View.VISIBLE
        mLoginFormView.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mLoginFormView.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        mProgressView.visibility = if (show) View.VISIBLE else View.GONE
        mProgressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProgressView.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

    private fun onLoginSuccess(result: LoginResult) {
        UserState.login(result.mUsername, result.mAvatar)
        if (PrefStore.getInstance().shouldReceiveNotifications()) {
            startService(GoogleHelper.getRegistrationIntentToStartService(this, true))
        }

        Toast.makeText(this, getString(R.string.toast_login_success, result.mUsername),
                Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onLoginFailed(error: Throwable) {
        LogUtils.w(TAG, "login failed", error)

        val resId = when (error) {
            is ConnectionException -> R.string.toast_connection_exception
            is RemoteException -> R.string.toast_remote_exception
            is RequestException -> R.string.toast_sign_in_failed
            else -> throw FatalException(error)
        }

        mCaptchaView.text.clear()
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

        loadCaptcha()
    }

    private fun onLoginCancel() {
        mAuthTask = null
        showProgress(false)

        if (!UserState.isLoggedIn()) {
            // force clean login state to avoid 2fa page
            RequestHelper.cleanCookies()
        }
    }

    private fun loginUser(account: String, password: String, captcha: String): Subscription? {
        mSignInFormData ?: return null
        return RequestHelper.login(account, password, captcha, mSignInFormData!!).onErrorResumeNext { error ->
            if (error is TwoFactorAuthException) {
                showTwoFactorAuthDialog()
            } else {
                error.toObservable()
            }
        }.observeOn(AndroidSchedulers.mainThread()).doOnUnsubscribe {
            onLoginCancel()
        }.await(object : Observer<LoginResult> {
            override fun onCompleted() {
            }

            override fun onError(error: Throwable) {
                onLoginFailed(error)
            }

            override fun onNext(result: LoginResult) {
                onLoginSuccess(result)
            }
        })
    }

    private fun showTwoFactorAuthDialog(): Observable<LoginResult> {
        val fragment = TwoFactorAuthDialog()
        fragment.show(supportFragmentManager, TAG_AUTH_CODE)

        return RxBus.toObservable<TwoFactorAuthDialog.TwoFactorAuthEvent>().first().flatMap { event ->
            if (event.code == null) {
                Observable.empty<LoginResult>()
            } else {
                RequestHelper.twoFactorAuth(event.code)
            }
        }
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName

        private val TAG_AUTH_CODE = "auth_code"
    }
}

