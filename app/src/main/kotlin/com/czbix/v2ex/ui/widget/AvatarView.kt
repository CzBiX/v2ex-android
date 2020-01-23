package com.czbix.v2ex.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.airbnb.epoxy.preload.ViewMetadata
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.czbix.v2ex.R
import com.czbix.v2ex.db.Member
import com.czbix.v2ex.model.Avatar
import com.czbix.v2ex.util.withCrossFade

class AvatarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val realSize by lazy {
        layoutParams.width - paddingTop * 2
    }

    fun setAvatar(glide: RequestManager, avatar: Avatar?) {
        if (avatar == null) {
            glide.clear(this)
            return
        }

        getImgRequest(glide, avatar).into(this)
    }

    fun getImgRequest(glide: RequestManager, avatar: Avatar): RequestBuilder<Drawable> {
        val size = realSize
        val url = avatar.getUrlByPx(size)

        return glide.load(url).placeholder(R.drawable.avatar_default)
                .override(size, size).fitCenter()
                .withCrossFade()
    }

    interface OnAvatarActionListener {
        fun onMemberClick(member: Member)
    }

    class Metadata(
            val avatarView: AvatarView
    ) : ViewMetadata
}
