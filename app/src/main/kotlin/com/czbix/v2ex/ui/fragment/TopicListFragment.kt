package com.czbix.v2ex.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.czbix.v2ex.R
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.model.Node
import com.czbix.v2ex.model.Page
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.network.HttpStatus
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.ui.MainActivity
import com.czbix.v2ex.ui.TopicActivity
import com.czbix.v2ex.ui.TopicEditActivity
import com.czbix.v2ex.ui.adapter.TopicAdapter
import com.czbix.v2ex.ui.model.NetworkState
import com.czbix.v2ex.ui.model.TopicListViewModel
import com.czbix.v2ex.ui.widget.DividerItemDecoration
import com.czbix.v2ex.ui.widget.TopicView.OnTopicActionListener
import com.czbix.v2ex.util.ExceptionUtils
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.await
import com.czbix.v2ex.util.dispose
import com.google.common.net.HttpHeaders
import io.reactivex.disposables.Disposable

class TopicListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, OnTopicActionListener {
    private lateinit var model: TopicListViewModel

    private lateinit var mAdapter: TopicAdapter
    private lateinit var mLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mFavIcon: MenuItem

    private val disposables: MutableList<Disposable> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = checkNotNull(arguments?.getParcelable<Page>(ARG_PAGE)) {
            "Page can't be null."
        }

        model = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TopicListViewModel(page) as T
            }
        }).get(TopicListViewModel::class.java)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mLayout = inflater.inflate(R.layout.fragment_topic_list,
                container, false) as SwipeRefreshLayout
        mRecyclerView = mLayout.findViewById(R.id.recycle_view)

        mLayout.setColorSchemeResources(R.color.material_blue_grey_500,
                R.color.material_blue_grey_700,
                R.color.material_blue_grey_900)
        mLayout.setOnRefreshListener(this)
        val layoutManager = LinearLayoutManager(mLayout.context)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))

        mAdapter = TopicAdapter(this) {
            model.retry()
        }
        mRecyclerView.adapter = mAdapter

        model.topics.observe(this, Observer { data ->
            mAdapter.submitList(data)
            activity!!.invalidateOptionsMenu()
        })
        model.networkState.observe(this, Observer {
            mAdapter.setNetworkState(it)
        })

        model.refreshState.observe(this, Observer {
            mLayout.isRefreshing = it == NetworkState.LOADING
        })

        return mLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = activity as MainActivity

        val page = model.page
        val shouldSetTitle = when {
            page is Node -> page.hasInfo()
            page === Page.PAGE_FAV_TOPIC -> {
                activity.setNavSelected(R.id.drawer_favorite)
                true
            }
            else -> false
        }

        if (shouldSetTitle) {
            activity.title = model.page.title
        }
    }

    private fun handleLoadException(ex: Exception) {
        var handled = false

        if (ex is RequestException) {
            @StringRes
            var strId: Int = 0
            when (ex.code) {
                HttpStatus.SC_MOVED_TEMPORARILY -> {
                    val location = ex.response.header(HttpHeaders.LOCATION)
                    if (location == "/") {
                        // it's blocked for new user
                        strId = R.string.toast_node_not_found
                    }
                }
            }

            if (strId != 0) {
                if (userVisibleHint) {
                    Toast.makeText(activity, strId, Toast.LENGTH_SHORT).show()
                }
                handled = true
            }
        }

        if (!handled) {
            ExceptionUtils.handleExceptionNoCatch(this, ex)
        }
    }

    override fun onRefresh() {
        mLayout.isRefreshing = true
        model.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_topic_list, menu)

        if (UserState.isLoggedIn()) {
            mFavIcon = menu.findItem(R.id.action_fav)

            updateFavIcon()
        } else {
            menu.findItem(R.id.action_new_topic).isVisible = false
            menu.findItem(R.id.action_fav).isVisible = false
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun updateFavIcon() {
        if (model.page !is Node || model.onceToken.value == null) {
            mFavIcon.isVisible = false
            return
        }

        val icon = if (model.favorite.value!!) {
            R.drawable.ic_favorite_white_24dp
        } else {
            R.drawable.ic_favorite_border_white_24dp
        }

        mFavIcon.setIcon(icon)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                onRefresh()
                return true
            }
            R.id.action_fav -> {
                onFavNode()
                return true
            }
            R.id.action_new_topic -> {
                val intent = Intent(activity, TopicEditActivity::class.java)
                if (model.page is Node) {
                    intent.putExtra(TopicEditActivity.KEY_NODE, model.page)
                }
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onFavNode() {
        check(model.page is Node) {
            "Page should be Node."
        }

        model.toggleFavorite(!model.favorite.value!!)
        updateFavIcon()

        val node = model.page as Node

        disposables += RequestHelper.favor(node, model.favorite.value!!, model.onceToken.value!!)
                .await({ updateFavIcon() }, { e ->
                    when (e) {
                        is ConnectionException, is RemoteException -> {
                            LogUtils.w(TAG, "favorite node failed", e)
                            model.toggleFavorite(!model.favorite.value!!)
                        }
                        else -> throw e
                    }
                })
    }

    override fun onTopicOpen(view: View, topic: Topic) {
        val intent = Intent(context, TopicActivity::class.java)
        intent.putExtra(TopicActivity.KEY_TOPIC, topic)

        startActivity(intent)

        topic.setHasRead()
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
    }

    companion object {
        private val TAG = TopicListFragment::class.java.simpleName
        private const val ARG_PAGE = "page"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment TopicListFragment.
         */
        fun newInstance(page: Page): TopicListFragment {
            val fragment = TopicListFragment()
            val args = Bundle()
            args.putParcelable(ARG_PAGE, page)
            fragment.arguments = args
            return fragment
        }
    }
}
