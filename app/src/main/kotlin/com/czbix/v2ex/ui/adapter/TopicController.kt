package com.czbix.v2ex.ui.adapter

import android.graphics.drawable.Drawable
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.airbnb.epoxy.preload.Preloadable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.czbix.v2ex.R
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.ui.ExHolder
import com.czbix.v2ex.ui.widget.AvatarView
import com.czbix.v2ex.ui.widget.TopicView

class TopicController(private val mListener: TopicView.OnTopicActionListener) : TypedEpoxyController<List<Topic>>() {
    override fun buildModels(data: List<Topic>?) {
        if (data == null) {
            return
        }

        data.forEach { topic ->
            topicControllerTopic {
                id(topic.id)
                listener(mListener)
                topic(topic)
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_topic)
    abstract class TopicModel : EpoxyModelWithHolder<TopicModel.Holder>() {
        @EpoxyAttribute
        lateinit var topic: Topic
        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var listener: TopicView.OnTopicActionListener

        override fun bind(holder: Holder) {
            holder.view.fillData(holder.glide, topic)
        }

        override fun unbind(holder: Holder) {
            holder.view.clear(holder.glide)
        }

        fun getImgRequest(glide: RequestManager, avatarView: AvatarView): RequestBuilder<Drawable> {
            return avatarView.getImgRequest(glide, topic.member!!.avatar!!)
        }

        inner class Holder : ExHolder<TopicView>(), Preloadable {
            override val viewsToPreload by lazy {
                listOf(view.mAvatar)
            }

            override fun init() {
                view.setListener(listener)
            }
        }
    }
}
