package com.czbix.v2ex.ui.adapter

import android.graphics.drawable.Drawable
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed2EpoxyController
import com.airbnb.epoxy.preload.Preloadable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.czbix.v2ex.R
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.ui.ExHolder
import com.czbix.v2ex.ui.widget.AvatarView
import com.czbix.v2ex.ui.widget.TopicView

class TopicController(private val mListener: TopicView.OnTopicActionListener)
    : Typed2EpoxyController<List<Topic>, Set<Int>>() {
    var data: List<Topic> = emptyList()
    var readedSet: Set<Int> = emptySet()

    override fun buildModels(data: List<Topic>, readedSet: Set<Int>) {
        this.data = data
        this.readedSet = readedSet
        if (data.isEmpty()) {
            return
        }

        data.forEach { topic ->
            topicControllerTopic {
                id(topic.id)
                listener(this@TopicController.mListener)
                topic(topic)
                readed(topic.id in readedSet)
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_topic)
    abstract class TopicModel : EpoxyModelWithHolder<TopicModel.Holder>() {
        @EpoxyAttribute
        lateinit var topic: Topic

        @EpoxyAttribute
        var readed: Boolean = false

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var listener: TopicView.OnTopicActionListener

        override fun bind(holder: Holder) {
            holder.view.setListener(listener)
            holder.view.fillData(holder.glide, topic, readed)
        }

        override fun unbind(holder: Holder) {
            holder.view.clear(holder.glide)
        }

        fun getImgRequest(glide: RequestManager, avatarView: AvatarView): RequestBuilder<Drawable> {
            return avatarView.getImgRequest(glide, topic.member!!.avatar)
        }

        inner class Holder : ExHolder<TopicView>(), Preloadable {
            override val viewsToPreload by lazy {
                listOf(view.mAvatar)
            }
        }
    }
}
