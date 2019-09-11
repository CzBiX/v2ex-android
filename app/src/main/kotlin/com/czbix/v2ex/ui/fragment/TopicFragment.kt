package com.czbix.v2ex.ui.fragment


import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.epoxy.EpoxyRecyclerView
import com.bumptech.glide.Glide
import com.czbix.v2ex.CommentPlaceholderBindingModel_
import com.czbix.v2ex.R
import com.czbix.v2ex.ViewerProvider
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.db.Comment
import com.czbix.v2ex.db.Member
import com.czbix.v2ex.db.TopicRecord
import com.czbix.v2ex.inject.Injectable
import com.czbix.v2ex.model.Node
import com.czbix.v2ex.model.Resource
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.model.TopicResponse
import com.czbix.v2ex.network.GlideApp
import com.czbix.v2ex.network.HttpStatus
import com.czbix.v2ex.ui.*
import com.czbix.v2ex.ui.adapter.CommentController
import com.czbix.v2ex.ui.common.RetryCallback
import com.czbix.v2ex.ui.helper.ReplyFormHelper
import com.czbix.v2ex.ui.model.TopicViewModel
import com.czbix.v2ex.ui.widget.AvatarView
import com.czbix.v2ex.ui.widget.CommentView
import com.czbix.v2ex.ui.widget.DividerItemDecoration
import com.czbix.v2ex.ui.widget.HtmlMovementMethod
import com.czbix.v2ex.util.ExceptionUtils
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.MiscUtils
import com.czbix.v2ex.util.TrackerUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.common.base.Preconditions
import com.google.common.net.HttpHeaders
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TopicFragment : Fragment(),
        SwipeRefreshLayout.OnRefreshListener,
        ReplyFormHelper.OnReplyListener,
        CommentView.OnCommentActionListener,
        HtmlMovementMethod.OnHtmlActionListener,
        NodeListFragment.OnNodeActionListener,
        AvatarView.OnAvatarActionListener,
        Injectable
{
    private var layout: SwipeRefreshLayout by autoCleared()
    private var commentsView: EpoxyRecyclerView by autoCleared()
    private var jumpBackBtn: ImageButton by autoCleared()
    private var appBarLayout: AppBarLayout by autoCleared()
    private var layoutManager: LinearLayoutManager by autoCleared()
    private var favIcon: MenuItem by autoCleared()

    private var replyForm: ReplyFormHelper? = null
    private var snackbar: Snackbar? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var commentController: CommentController by autoCleared()
    private val topicViewModel: TopicViewModel by viewModels {
        viewModelFactory
    }
    private var lastPosStack: Stack<CommentPos> by autoCleared()

    private var mIsLoaded: Boolean = false
    private var mIsLoading: Boolean = false
    private var mLastIsFailed: Boolean = false
    private lateinit var baseTopic: Topic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        baseTopic = arguments?.getParcelable(ARG_TOPIC)!!

        lastPosStack = Stack()
        mIsLoaded = false

        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_topic, container, false)
        initJumpBackButton(rootView)

        layout = rootView.findViewById(R.id.comments_layout)
        layout.setColorSchemeResources(R.color.material_blue_grey_500, R.color.material_blue_grey_700, R.color.material_blue_grey_900)
        layout.setOnRefreshListener(this)

        commentsView = layout.findViewById(R.id.comments)
        commentsView.itemAnimator = object : DefaultItemAnimator() {
            override fun animateMove(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
                dispatchMoveFinished(holder)
                return false
            }
        }
        CommentController.addGlidePreloader(commentsView, Glide.with(this))
        initCommentsView()

        topicViewModel.result.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    setIsLoading(true)
                    commentController.setLoading(true)
                }
                is Resource.Failed -> handleLoadException(result.exception)
                is Resource.Success -> onLoadFinished(result.data)
            }
        }
        topicViewModel.comments.observe(this) {
            commentController.submitList(it)
        }
        topicViewModel.userActionResult.observe(this) { (action, resource) ->
            handleUserActionResult(action, resource)
        }

        if (!baseTopic.hasInfo) {
            commentsView.setVisible(false)
        }

        commentController.retryCallback = object : RetryCallback {
            override fun retry() {
                topicViewModel.refresh()
            }
        }
        commentController.setData(baseTopic)
        topicViewModel.setTopic(baseTopic)

        return rootView
    }

    private fun initCommentsView() {
        layoutManager = commentsView.layoutManager as LinearLayoutManager
        commentsView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST))

        commentController = CommentController(this, this, this, this)
        commentsView.setController(commentController)
        commentsView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                fetchPageIfNeed()
            }
        })
    }

    private fun initJumpBackButton(rootView: View) {
        jumpBackBtn = rootView.findViewById(R.id.btn_jump_back)
        jumpBackBtn.setOnClickListener {
            Preconditions.checkState(!lastPosStack.isEmpty(), "Why jump button showed without dest")

            this@TopicFragment.scrollToPos(null, lastPosStack.pop())
        }
        jumpBackBtn.setOnLongClickListener {
            lastPosStack.clear()
            true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = activity as TopicActivity
        activity.title = null

        val actionBar = checkNotNull(activity.supportActionBar)
        actionBar.setDisplayHomeAsUpEnabled(true)

        appBarLayout = activity.appBarLayout
        appBarLayout.findViewById<View>(R.id.toolbar).setOnClickListener {
            scrollToPos(null, CommentPos(true, 0))
        }
    }

    private fun setIsLoading(isLoading: Boolean) {
        mIsLoading = isLoading
        layout.isRefreshing = isLoading
    }

    override fun onRefresh() {
        topicViewModel.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!mIsLoaded) {
            return
        }

        inflater.inflate(R.menu.menu_topic, menu)

        if (UserState.isLoggedIn()) {
            favIcon = menu.findItem(R.id.action_fav)

            topicViewModel.favored.observe(this) {
                updateFavIcon(false, it)
            }

            if (PrefStore.getInstance().isAlwaysShowReplyForm) {
                menu.findItem(R.id.action_reply).isVisible = false
            }
        } else {
            for (i in MENU_REQUIRED_LOGGED_IN) {
                menu.findItem(i).isVisible = false
            }
        }

        setupShareActionMenu(menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun updateFavIcon(loading: Boolean, bool: Boolean? = null) {
        require(loading || bool != null)

        val icon = when {
            loading -> R.drawable.ic_sync_white_24dp
            bool!! -> R.drawable.ic_star_black_24dp
            else -> R.drawable.ic_star_border_black_24dp
        }
        favIcon.setIcon(icon)
    }

    private fun setupShareActionMenu(menu: Menu) {
        val itemShare = menu.findItem(R.id.action_share)

        val topic = topicViewModel.lastTopic

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                String.format("%s\n%s", topic.getTitle(), topic.url))

        itemShare.setOnMenuItemClickListener {
            this@TopicFragment.startActivity(Intent.createChooser(shareIntent, null))
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val topic = topicViewModel.lastTopic

        when (item.itemId) {
            R.id.action_copy_link -> {
                MiscUtils.setClipboard(activity!!, getString(R.string.desc_topic_link),
                        String.format("%s\n%s", topic.getTitle(), topic.url))
                return true
            }
            R.id.action_refresh -> {
                if (!mIsLoading) {
                    setIsLoading(true)
                    onRefresh()
                }
                return true
            }
            R.id.action_fav -> {
                onFavTopic()
                return true
            }
            R.id.action_reply -> {
                toggleReplyForm()
                return true
            }
            R.id.action_thank -> {
                onThankTopic(topic)
                return true
            }
            R.id.action_ignore -> {
                onIgnoreTopic(topic)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun toggleReplyForm() {
        val isShow: Boolean
        if (replyForm == null) {
            val rootView = view
            Preconditions.checkNotNull(rootView!!)
            val viewStub = rootView.findViewById<ViewStub>(R.id.reply_form)
            replyForm = ReplyFormHelper(activity, viewStub, this)

            isShow = true
        } else {
            replyForm!!.toggle()
            isShow = replyForm!!.visibility
        }

        if (isShow) {
            appBarLayout.setExpanded(false)
        }
        TrackerUtils.onTopicSwitchReply(isShow)
    }

    private val lastReadRecordObserver: Observer<in TopicRecord?> = Observer {
        if (it != null && it.lastReadComment > 0 && lastPosStack.isEmpty()) {
            val pos = minOf(it.lastReadComment, topicViewModel.comments.value!!.size)
            lastPosStack.push(CommentPos(false, pos))
            updateJumpBackButton()
        }
    }

    private fun onLoadFinished(data: TopicResponse) {
        if (!mIsLoaded) {
            if (!baseTopic.hasInfo) {
                commentsView.setVisible(true)
            }
            topicViewModel.lastReadRecord.observe(this, lastReadRecordObserver)
        }
        mIsLoaded = true
        mLastIsFailed = false

        commentController.setData(data.topic)
        commentController.setLoading(false)

        activity!!.invalidateOptionsMenu()

        if (replyForm == null &&
                UserState.isLoggedIn() &&
                PrefStore.getInstance().isAlwaysShowReplyForm) {
            toggleReplyForm()
        }

        setIsLoading(false)
    }

    private fun handleLoadException(exception: Exception) {
        Timber.w(exception)

        mLastIsFailed = true
        setIsLoading(false)

        commentController.setFailed()

        var finishActivity = false
        var handled = false

        if (exception is RequestException) {
            @StringRes
            var strId = 0
            when (exception.code) {
                HttpStatus.SC_NOT_FOUND -> strId = R.string.toast_topic_not_found
                HttpStatus.SC_MOVED_TEMPORARILY -> {
                    val location = exception.response.header(HttpHeaders.LOCATION)
                    if (location == "/") {
                        // it's blocked for new user
                        strId = R.string.toast_topic_not_found
                    }
                }
            }

            if (strId != 0) {
                Toast.makeText(activity, strId, Toast.LENGTH_SHORT).show()
                finishActivity = true
                handled = true
            }
        }

        if (!handled) {
            finishActivity = ExceptionUtils.handleException(this, exception)
        }

        if (finishActivity) {
            activity!!.finish()
        }
    }

    override fun onStop() {
        super.onStop()

        if (mIsLoaded) {
            topicViewModel.markReadPosition()
        }
    }

    override fun onReply(content: CharSequence) {
        TrackerUtils.onTopicReply()

        val form = replyForm!!

        form.visibility = false
        doActionRequest(TopicViewModel.Action.PostComment(content.toString())) {
            if (!form.visibility) {
                form.toggle()
            }
        }
    }

    private fun handleUserActionResult(action: TopicViewModel.Action, resource: Resource<*>) {
        if (resource.status == Resource.Status.LOADING) {
            if (action is TopicViewModel.Action.FavTopic) {
                updateFavIcon(true)
            }
            return
        }

        if (resource is Resource.Failed) {
            ExceptionUtils.handleException(this, resource.exception)
            when (action) {
                is TopicViewModel.Action.PostComment -> {
                    replyForm!!.let {
                        if (!it.visibility) {
                            it.toggle()
                        }
                    }
                }
            }
        } else {
            when (action) {
                is TopicViewModel.Action.FavTopic -> updateFavIcon(false, action.bool)
                is TopicViewModel.Action.IgnoreTopic -> {
                    Toast.makeText(activity, R.string.toast_topic_ignored, Toast.LENGTH_LONG).show()
                    activity!!.finish()
                    return
                }
                is TopicViewModel.Action.PostComment -> {
                    replyForm!!.setContent(null)
                }
            }
        }

        snackbar?.let {
            it.dismiss()
            snackbar = null
        }
        topicViewModel.refresh(action.itemPage)
    }

    private fun doActionRequest(action: TopicViewModel.Action, cancelCallback: (() -> Unit)? = null) {
        layout.isRefreshing = true

        val snackbar = Snackbar.make(layout, R.string.toast_sending, Snackbar.LENGTH_INDEFINITE)
        this.snackbar = snackbar

        val job = topicViewModel.doAction(action)
        if (job != null) {
            snackbar.setAction(R.string.action_cancel) {
                this.snackbar = null
                job.cancel()
                cancelCallback?.invoke()
                layout.isRefreshing = false
            }
        }
        snackbar.show()
    }

    override fun onCommentIgnore(comment: Comment) {
        doActionRequest(TopicViewModel.Action.IgnoreComment(comment))
    }

    private fun onIgnoreTopic(topic: Topic) {
        doActionRequest(TopicViewModel.Action.IgnoreTopic(topic))
    }

    override fun onCommentThank(comment: Comment) {
        doActionRequest(TopicViewModel.Action.ThankComment(comment))
    }

    private fun onThankTopic(topic: Topic) {
        doActionRequest(TopicViewModel.Action.ThankTopic(topic))
    }

    override fun onCommentReply(comment: Comment, member: Member) {
        if (replyForm == null || !replyForm!!.visibility) {
            toggleReplyForm()
        }

        replyForm!!.content.append("@").append(member.username).append(" ")
        replyForm!!.requestFocus()
    }

    override fun onCommentCopy(comment: Comment, content: String) {
        val context = activity
        MiscUtils.setClipboard(context!!, ClipData.newHtmlText(null, content, comment.content))
    }

    override fun onCommentUrlClick(url: String, pos: Int) {
        if (url.startsWith(MiscUtils.PREFIX_MEMBER)) {
            findComment(Member.getNameFromUrl(url), pos)
            return
        }

        onUrlClick(url)
    }

    private fun findComment(member: String, pos: Int) {
        val oldPos = CommentPos(false, pos)

        val basePos = commentController.commentBasePos
        val models = commentController.adapter.copyOfModels
        for (model in models.subList(basePos, basePos + pos - 1).asReversed()) {
            if (model is CommentPlaceholderBindingModel_) {
                continue
            }

            check(model is CommentController.CommentModel)

            if (model.comment.member.username == member) {
                scrollToPos(oldPos, CommentPos(false, model.position))
                return
            }
        }

        if (topicViewModel.lastTopic.member!!.username == member) {
            scrollToPos(oldPos, CommentPos(true, 0))
            return
        }

        Toast.makeText(activity, getString(R.string.toast_can_not_found_comments_of_the_author,
                member), Toast.LENGTH_SHORT).show()
    }

    private fun scrollToPos(curPos: CommentPos?, destPos: CommentPos) {
        if (curPos != null) {
            lastPosStack.push(curPos)
        }
        updateJumpBackButton()

        commentsView.scrollToPosition(destPos.getPos())
        commentsView.postDelayed({
            val view = layoutManager.findViewByPosition(destPos.getPos())
            if (view == null) {
                return@postDelayed
            }
            highlightRow(view)
        }, 200)
    }

    override fun onUrlClick(url: String) {
        try {
            MiscUtils.openUrl(activity!!, url)
        } catch (e: ActivityNotFoundException) {
            LogUtils.i(TAG, "can't start activity for: %s", e, url)
            Toast.makeText(activity, R.string.toast_activity_not_found,
                    Toast.LENGTH_SHORT).show()
        }

    }

    override fun onImageClick(source: String) {
        ViewerProvider.viewImage(context!!, GlideApp.with(this), source)
    }

    override fun onNodeOpen(node: Node) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra(MainActivity.BUNDLE_NODE, node)
        startActivity(intent)
    }

    override fun onMemberClick(member: Member) {
        onUrlClick(member.url)
    }

    private fun onFavTopic() {
        val favored = topicViewModel.favored.value!!
        doActionRequest(TopicViewModel.Action.FavTopic(!favored))
    }

    private fun fetchPageIfNeed() {
        if (mIsLoading || mLastIsFailed) {
            return
        }

        val itemPosition = layoutManager.findLastVisibleItemPosition()
        val commentPos = itemPosition + PRELOAD_DISTANCE - commentController.commentBasePos
        val pageToFetch = (commentPos / 100) + 1
        topicViewModel.fetchPageIfNeed(pageToFetch)
    }

    private fun highlightRow(view: View) {
        val width = view.width * 0.1f
        val animator = ObjectAnimator.ofFloat(
                view, "translationX", 0f, -width, width, 0f
        )
        animator.interpolator = null
        animator.start()
    }

    private fun updateJumpBackButton() {
        jumpBackBtn.setVisibility(lastPosStack.isNotEmpty())
    }

    internal inner class CommentPos(
            private val isAbsPos: Boolean,
            private val pos: Int
    ) {
        fun getPos(): Int {
            return if (isAbsPos) {
                pos
            } else commentController.commentBasePos + pos
        }
    }

    companion object {
        private val TAG = TopicFragment::class.java.simpleName
        private const val ARG_TOPIC = "topic"
        private val MENU_REQUIRED_LOGGED_IN = intArrayOf(R.id.action_ignore, R.id.action_reply, R.id.action_thank, R.id.action_fav)
        private const val PRELOAD_DISTANCE = 20

        @JvmStatic
        fun newInstance(topic: Topic): TopicFragment {
            val fragment = TopicFragment()
            val args = Bundle().apply {
                putParcelable(ARG_TOPIC, topic)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
