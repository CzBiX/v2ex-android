package com.czbix.v2ex.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.collection.ArraySet
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.addGlidePreloader
import com.airbnb.epoxy.glidePreloader
import com.bumptech.glide.Glide
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.R
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.FatalException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.dao.NodeDao
import com.czbix.v2ex.db.TopicRecordDao
import com.czbix.v2ex.event.BaseEvent
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.inject.Injectable
import com.czbix.v2ex.model.Node
import com.czbix.v2ex.model.Page
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.network.HttpStatus
import com.czbix.v2ex.network.V2exService
import com.czbix.v2ex.ui.MainActivity
import com.czbix.v2ex.ui.TopicActivity
import com.czbix.v2ex.ui.TopicEditActivity
import com.czbix.v2ex.ui.adapter.TopicController
import com.czbix.v2ex.ui.adapter.`TopicController$TopicModel_`
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult
import com.czbix.v2ex.ui.loader.TopicListLoader
import com.czbix.v2ex.ui.widget.AvatarView
import com.czbix.v2ex.ui.widget.DividerItemDecoration
import com.czbix.v2ex.ui.widget.TopicView.OnTopicActionListener
import com.czbix.v2ex.util.ExceptionUtils
import com.czbix.v2ex.util.ExecutorUtils
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.dispose
import com.google.common.net.HttpHeaders
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TopicListFragment : androidx.fragment.app.Fragment(), LoaderCallbacks<LoaderResult<TopicListLoader.TopicList>>, SwipeRefreshLayout.OnRefreshListener, OnTopicActionListener,  Injectable {
    private lateinit var mPage: Page

    private lateinit var controller: TopicController
    private lateinit var mLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: EpoxyRecyclerView
    private lateinit var mFavIcon: MenuItem

    private val disposables: MutableList<Disposable> = mutableListOf()
    private var mFavored: Boolean = false
    private var mOnceToken: String? = null

    @Inject
    lateinit var topicDao: TopicRecordDao

    @Inject
    lateinit var v2exService: V2exService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments
        if (arguments != null) {
            arguments.getParcelable<Page>(ARG_PAGE).let {
                if (it == null) {
                    throw FatalException("node can't be null")
                }

                mPage = it
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mLayout = inflater.inflate(R.layout.fragment_topic_list,
                container, false) as SwipeRefreshLayout
        mRecyclerView = mLayout.findViewById(R.id.recycle_view)

        mLayout.setColorSchemeResources(R.color.material_blue_grey_500, R.color.material_blue_grey_700, R.color.material_blue_grey_900)
        mLayout.setOnRefreshListener(this)

        mRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
        mRecyclerView.addGlidePreloader(
                Glide.with(this), 5,
                preloader = glidePreloader(viewMetadata = { view ->
                    AvatarView.Metadata(view as AvatarView)
                }) { requestManager, epoxyModel: `TopicController$TopicModel_`, viewData ->
                    epoxyModel.getImgRequest(requestManager, viewData.metadata.avatarView)
                })

        controller = TopicController(this)
        mRecyclerView.setController(controller)

        mLayout.isRefreshing = true
        return mLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = activity as MainActivity

        val shouldSetTitle = if (mPage is Node) {
            val node = mPage as Node
            if (node.hasInfo()) {
                true
            } else {
                val dbNode = NodeDao.get(node.name)
                if (dbNode == null) {
                    false
                } else {
                    mPage = dbNode
                    true
                }
            }
        } else if (mPage === Page.PAGE_FAV_TOPIC) {
            activity.setNavSelected(R.id.drawer_favorite)
            true
        } else {
            false
        }

        if (shouldSetTitle) {
            activity.title = mPage.title
        }
    }

    override fun onStart() {
        super.onStart()

        val loaderManager = loaderManager
        if (loaderManager.getLoader<Any>(0) != null) {
            // already loaded
            return
        }
        loaderManager.initLoader(0, null, this)
    }

    override fun onStop() {
        super.onStop()

        AppCtx.eventBus.unregister(this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<LoaderResult<TopicListLoader.TopicList>> {
        val log = String.format("load list: %s", mPage.title)
        FirebaseCrashlytics.getInstance().log(log)
        LogUtils.d(TAG, log)

        return TopicListLoader(requireActivity(), mPage, topicDao)
    }

    override fun onLoadFinished(loader: Loader<LoaderResult<TopicListLoader.TopicList>>, result: LoaderResult<TopicListLoader.TopicList>) {
        mLayout.isRefreshing = false
        if (result.hasException()) {
            handleLoadException(result.mException)
            return
        }

        result.mResult.let {
            mFavored = it.isFavorited
            mOnceToken = it.onceToken
            controller.setData(it, it.readed)
        }

        requireActivity().invalidateOptionsMenu()
    }

    private fun handleLoadException(ex: Exception) {
        var handled = false

        if (ex is RequestException) {
            @StringRes
            var strId = 0
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

    override fun onLoaderReset(loader: Loader<LoaderResult<TopicListLoader.TopicList>>) {
        controller.setData(emptyList(), emptySet())
    }

    override fun onRefresh() {
        val loader = loaderManager.getLoader<Any>(0) ?: return
        loader.forceLoad()

        mRecyclerView.smoothScrollToPosition(0)
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
        if (mPage !is Node || mOnceToken == null) {
            mFavIcon.isVisible = false
            return
        }

        val icon = if (mFavored)
            R.drawable.ic_favorite_white_24dp else R.drawable.ic_favorite_border_white_24dp

        mFavIcon.setIcon(icon)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                mLayout.isRefreshing = true
                onRefresh()
                return true
            }
            R.id.action_fav -> {
                onFavNode()
                return true
            }
            R.id.action_new_topic -> {
                val intent = Intent(activity, TopicEditActivity::class.java)
                if (mPage is Node) {
                    intent.putExtra(TopicEditActivity.KEY_NODE, mPage)
                }
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun onFavNode() {
        assert(mPage is Node)

        mFavored = !mFavored
        updateFavIcon()

        RxBus.subscribe<BaseEvent.NodeEvent> {
            updateFavIcon()
        }.let {
            disposables += it
        }

        ExecutorUtils.execute {
            try {
                val node = mPage as Node
                runBlocking {
                    v2exService.favor(node, mFavored, mOnceToken!!)
                }
            } catch (e: Exception) {
                when (e) {
                    is ConnectionException, is RemoteException -> {
                        LogUtils.w(TAG, "favorite node failed", e)
                        mFavored = !mFavored
                    }
                    else -> throw e
                }
            }

            RxBus.post(BaseEvent.NodeEvent())
        }
    }

    override fun onTopicOpen(view: View, topic: Topic) {
        val intent = Intent(context, TopicActivity::class.java)
        intent.putExtra(TopicActivity.KEY_TOPIC, topic)

        startActivity(intent)

        (controller.readedSet as ArraySet).add(topic.id)
        controller.setData(controller.data, controller.readedSet)
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
    }

    companion object {
        private val TAG = TopicListFragment::class.java.simpleName
        private val ARG_PAGE = "page"

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
