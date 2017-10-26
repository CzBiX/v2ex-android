package com.czbix.v2ex.ui.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.EditText
import com.czbix.v2ex.R
import com.czbix.v2ex.event.BaseEvent
import com.czbix.v2ex.helper.RxBus

class TwoFactorAuthDialog : DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var editText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity.layoutInflater
        val layout = inflater.inflate(R.layout.view_edittext, null)
        editText = layout.findViewById(R.id.edit_text)

        return AlertDialog.Builder(context).apply {
            setTitle(R.string.title_two_factor_auth)
            setView(layout)
            setNegativeButton(R.string.action_cancel, this@TwoFactorAuthDialog)
            // set click listener later to avoid auto dismiss
            setPositiveButton(R.string.action_sign_in, null)
        }.create()
    }

    override fun onStart() {
        super.onStart()

        (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            onClick(dialog, DialogInterface.BUTTON_POSITIVE)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_NEGATIVE -> dialog.cancel()
            DialogInterface.BUTTON_POSITIVE -> {
                val code = editText.text.toString()
                if (code.length != 6) {
                    editText.error = getString(R.string.error_invalid_auth_code)
                    return
                }

                // dialog will auto close after click, check code format at outside
                RxBus.post(TwoFactorAuthEvent(code))
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        RxBus.post(TwoFactorAuthEvent())
    }

    class TwoFactorAuthEvent(val code: String?) : BaseEvent() {
        constructor() : this(null)
    }
}
