package com.czbix.v2ex.ui.adapter

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.czbix.v2ex.R

import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.ui.model.NetworkState
import com.czbix.v2ex.ui.model.Status
import com.czbix.v2ex.ui.widget.TopicView

class TopicAdapter(
        private val mListener: TopicView.OnTopicActionListener,
        private val retryCallback: () -> Unit
) : PagedListAdapter<Topic, RecyclerView.ViewHolder>(TOPIC_COMPARATOR) {
    private var networkState: NetworkState? = null

    private fun hasExtraRow(): Boolean {
        return networkState != null && networkState != NetworkState.LOADED
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            TYPE_NETWORK
        } else {
            TYPE_TOPIC
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TOPIC -> ViewHolder(TopicView(parent.context), mListener)
            TYPE_NETWORK -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> error("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_TOPIC -> (holder as ViewHolder).bind(getItem(position)!!)
            TYPE_NETWORK -> (holder as NetworkStateItemViewHolder).bind(networkState)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    class ViewHolder(
            private val mView: TopicView,
            listener: TopicView.OnTopicActionListener
    ) : RecyclerView.ViewHolder(mView) {
        init {
            mView.setListener(listener)
        }

        fun bind(topic: Topic) {
            mView.fillData(topic)
        }
    }

    /**
     * A View Holder that can display a loading or have click action.
     * It is used to show the network state of paging.
     */
    class NetworkStateItemViewHolder(
            view: View,
            private val retryCallback: () -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        private val retry = view.findViewById<Button>(R.id.retry_button)
        private val errorMsg = view.findViewById<TextView>(R.id.error_msg)
        init {
            retry.setOnClickListener {
                retryCallback()
            }
        }
        fun bind(networkState: NetworkState?) {
            progressBar.visibility = toVisibility(networkState?.status == Status.RUNNING)
            retry.visibility = toVisibility(networkState?.status == Status.FAILED)
            errorMsg.visibility = toVisibility(networkState?.msg != null)
            errorMsg.text = networkState?.msg
        }

        companion object {
            fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateItemViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_network_state_item, parent, false)
                return NetworkStateItemViewHolder(view, retryCallback)
            }

            fun toVisibility(constraint : Boolean): Int {
                return if (constraint) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    companion object {
        const val TYPE_TOPIC = 1
        const val TYPE_NETWORK = 2

        val TOPIC_COMPARATOR = object : DiffUtil.ItemCallback<Topic>() {
            override fun areItemsTheSame(oldTopic: Topic, newTopic: Topic): Boolean {
                return oldTopic.id == newTopic.id
            }

            override fun areContentsTheSame(oldTopic: Topic, newTopic: Topic): Boolean {
                return oldTopic == newTopic
            }
        }
    }
}
