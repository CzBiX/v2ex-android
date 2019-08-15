package com.czbix.v2ex.ui.adapter

import android.view.View
import com.airbnb.epoxy.*
import com.czbix.v2ex.R
import com.czbix.v2ex.model.Topic
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
            holder.view.fillData(topic)
        }

        inner class Holder : EpoxyHolder() {
            lateinit var view: TopicView

            override fun bindView(itemView: View) {
                view = itemView as TopicView

                view.setListener(listener)
            }
        }
    }
}
