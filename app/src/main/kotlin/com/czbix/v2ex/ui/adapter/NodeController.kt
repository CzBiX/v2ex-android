package com.czbix.v2ex.ui.adapter

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.czbix.v2ex.R
import com.czbix.v2ex.model.Node
import com.czbix.v2ex.ui.ExHolder
import com.czbix.v2ex.ui.fragment.NodeListFragment
import com.czbix.v2ex.util.ViewUtils

class NodeController(private val listener: NodeListFragment.OnNodeActionListener) : EpoxyController() {
    private var allData: List<Node>? = null
    private var currentData: List<Node>? = null
    private var lastFilter: String? = null

    fun setData(data: List<Node>?) {
        allData = data
        currentData = data

        if (lastFilter == null) {
            requestModelBuild()
        } else {
            filterText(lastFilter!!)
        }
    }

    override fun buildModels() {
        currentData?.forEach { node ->
            nodeControllerNode {
                id(node.id)
                node(node)
                listener(listener)
            }
        }
    }

    fun filterText(query: String) {
        currentData = if (TextUtils.isEmpty(query)) {
            allData
        } else {
            allData?.filter { node ->
                node.name.contains(query) ||
                        node.title!!.contains(query) ||
                        node.titleAlternative != null && node.titleAlternative.contains(query)
            }
        }

        requestDelayedModelBuild(100)
    }

    @EpoxyModelClass(layout = R.layout.view_node)
    abstract class NodeModel : EpoxyModelWithHolder<NodeModel.Holder>(), View.OnClickListener {
        @EpoxyAttribute
        lateinit var node: Node

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var listener: NodeListFragment.OnNodeActionListener

        override fun bind(holder: Holder) {
            holder.apply {
                view.setOnClickListener(this@NodeModel)

                title.text = node.title

                val alternative = node.titleAlternative
                if (alternative.isNullOrEmpty()) {
                    alertTitle.text = null
                    alertTitle.visibility = View.INVISIBLE
                } else {
                    alertTitle.text = alternative
                    alertTitle.visibility = View.VISIBLE
                }

                setAvatarImg(this, node)
            }
        }

        override fun unbind(holder: Holder) {
            holder.glide.clear(holder.avatar)
        }

        fun setAvatarImg(holder: Holder, node: Node) {
            val avatar = node.avatar ?: return

            val pixel = ViewUtils.getDimensionPixelSize(R.dimen.node_avatar_size)
            holder.glide.load(avatar.getUrlByPx(pixel))
                    .transition(DrawableTransitionOptions.withCrossFade()).into(holder.avatar)
        }

        override fun onClick(v: View) {
            ViewUtils.hideInputMethod(v)
            listener.onNodeOpen(node)
        }

        class Holder : ExHolder<View>() {
            val title by bind<TextView>(R.id.title)
            val alertTitle by bind<TextView>(R.id.alertTitle)
            val avatar by bind<ImageView>(R.id.avatar_img)
        }
    }
}
