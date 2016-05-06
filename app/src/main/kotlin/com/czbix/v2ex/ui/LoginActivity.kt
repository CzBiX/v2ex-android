package com.czbix.v2ex.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.czbix.v2ex.R
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.FatalException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.google.GoogleHelper
import com.czbix.v2ex.helper.CustomTabsHelper
import com.czbix.v2ex.model.LoginResult
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.ui.fragment.GoogleLoginDialog
import com.czbix.v2ex.util.*
import io.fabric.sdk.android.Fabric
import java.io.IOException

/**
 * A login screen that offers login via account/password.
 */
class LoginActivity : BaseActivity(), View.OnClickListener, GoogleLoginDialog.GoogleSignInListener {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null

    // UI references.
    private lateinit var mAccountView: EditText
    private lateinit var mPwdView: EditText
    private lateinit var mProgressView: View
    private lateinit var mLoginFormView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up the login form.
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

        val mSignIn = findViewById(R.id.sign_in) as Button
        mSignIn.setOnClickListener(this)

        findViewById(R.id.sign_up)!!.setOnClickListener(this)

        findViewById(R.id.google_sign_in)!!.setOnClickListener(this)

        mLoginFormView = findViewById(R.id.login_form)!!
        mProgressView = findViewById(R.id.login_progress)!!
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in -> attemptLogin()
            R.id.sign_up -> {
                val uri = Uri.parse("https://www.v2ex.com/signup?r=aliuwr")
                val builder = CustomTabsHelper.getBuilder(this, null)
                builder.build().launchUrl(this, uri)
            }
            R.id.google_sign_in -> onGoogleSignIn()
        }
    }

    private fun onGoogleSignIn() {
        showProgress(true)
        async() {
            RequestHelper.getGoogleSignInUrl()
        }.await(onNext = {
            val dialog = GoogleLoginDialog(it, this)
            supportFragmentManager.beginTransaction().add(dialog, null).commit()
        }, onError = {
            onGoogleSignInFailed(it)
        })
    }

    private fun onGoogleSignInFailed(throwable: Throwable) {
        LogUtils.w(TAG, "google login failed", throwable)
        if (throwable !is IOException) {
            Crashlytics.logException(throwable)
        }
        Toast.makeText(this, R.string.toast_sign_in_failed, Toast.LENGTH_LONG).show()
        onGoogleSignInCancelled()
    }

    override fun onGoogleSignedIn(url: String) {
        LogUtils.d(TAG, "result url: %s", url)

        async() {
            RequestHelper.loginViaGoogle(url)
        }.aforeach {
            onLogin(it)
        }.await(onNext = {
            onLoginSuccess(it.mUsername)
            showProgress(false)
        }, onError = {
            onGoogleSignInFailed(it)
        })
    }

    private fun onLogin(result: LoginResult) {
        UserState.getInstance().login(result.mUsername, result.mAvatar)
        if (PrefStore.getInstance().shouldReceiveNotifications()) {
            startService(GoogleHelper.getRegistrationIntentToStartService(this, true))
        }
    }

    override fun onGoogleSignInCancelled() {
        showProgress(false)
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

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask(email, password)
            mAuthTask!!.execute(null)
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    fun showProgress(show: Boolean) {
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

    private fun onLoginSuccess(username: String) {
        Toast.makeText(this, getString(R.string.toast_login_success, username),
                Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mAccount: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {
        private val TAG = UserLoginTask::class.java.simpleName
        private lateinit var mException: Exception

        override fun doInBackground(vararg params: Void): Boolean {
            try {
                val result = RequestHelper.login(mAccount, mPassword)
                onLogin(result)
                return true
            } catch (e: ConnectionException) {
                mException = e
            } catch (e: RemoteException) {
                mException = e
            } catch (e: RequestException) {
                mException = e
            }

            return false
        }

        override fun onPostExecute(success: Boolean) {
            mAuthTask = null
            showProgress(false)

            if (success) {
                onLoginSuccess(mAccount)
                return
            }

            LogUtils.w(TAG, "login failed", mException)

            val resId = when (mException) {
                is ConnectionException -> R.string.toast_connection_exception
                is RemoteException -> R.string.toast_remote_exception
                is RequestException -> R.string.toast_sign_in_failed
                else -> throw FatalException(mException)
            }

            Toast.makeText(this@LoginActivity, resId, Toast.LENGTH_LONG).show()
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }
}

