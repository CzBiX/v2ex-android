package com.czbix.v2ex.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

import com.czbix.v2ex.model.Topic

class DebugActivity : AppCompatActivity() {
    private lateinit var mLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLayout = LinearLayout(this)
        setContentView(mLayout)

        initDebugItem()
    }

    @SuppressLint("SetTextI18n")
    private fun initDebugItem() {
        val button = Button(this)
        button.text = "Sandbox Topic"
        button.setOnClickListener {
//            val intent = Intent(this@DebugActivity, TopicActivity::class.java)
        }
        mLayout.addView(button)
    }
}
