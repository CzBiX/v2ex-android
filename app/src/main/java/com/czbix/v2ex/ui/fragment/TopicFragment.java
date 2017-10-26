package com.czbix.v2ex.ui.fragment;


import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.PrefStore;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.dao.DraftDao;
import com.czbix.v2ex.dao.TopicDao;
import com.czbix.v2ex.eventbus.TopicEvent;
import com.czbix.v2ex.helper.MultiList;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Ignorable;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Thankable;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.czbix.v2ex.model.db.Draft;
import com.czbix.v2ex.network.HttpStatus;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.ui.MainActivity;
import com.czbix.v2ex.ui.TopicActivity;
import com.czbix.v2ex.ui.adapter.CommentAdapter;
import com.czbix.v2ex.ui.helper.ReplyFormHelper;
import com.czbix.v2ex.ui.loader.AsyncTaskLoader.LoaderResult;
import com.czbix.v2ex.ui.loader.TopicLoader;
import com.czbix.v2ex.ui.util.Html;
import com.czbix.v2ex.ui.widget.AvatarView;
import com.czbix.v2ex.ui.widget.CommentView;
import com.czbix.v2ex.ui.widget.DividerItemDecoration;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod;
import com.czbix.v2ex.util.ExceptionUtils;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.MiscUtils;
import com.czbix.v2ex.util.TrackerUtils;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HttpHeaders;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TopicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<LoaderResult<TopicWithComments>>,
        ReplyFormHelper.OnReplyListener, CommentView.OnCommentActionListener,
        HtmlMovementMethod.OnHtmlActionListener, NodeListFragment.OnNodeActionListener,
        AvatarView.OnAvatarActionListener {
    private static final String TAG = TopicFragment.class.getSimpleName();
    private static final String ARG_TOPIC = "topic";
    private static final int[] MENU_REQUIRED_LOGGED_IN = {R.id.action_ignore, R.id.action_reply,
            R.id.action_thank, R.id.action_fav};

    private Topic mTopic;
    private SwipeRefreshLayout mLayout;
    private RecyclerView mCommentsView;
    private CommentAdapter mCommentAdapter;
    private ReplyFormHelper mReplyForm;
    private String mCsrfToken;
    private String mOnceToken;
    private Draft mDraft;
    private ImageButton mJumpBack;

    private MultiList<Comment> mComments;
    private boolean mIsLoaded;
    private int mCurPage;
    private int mMaxPage;
    private boolean mIsLoading;
    private boolean mLastIsFailed;
    private boolean mFavored;
    private MenuItem mFavIcon;
    private int mLastFocusPos;
    private LinearLayoutManager mCommentsLayoutManager;
    private AppBarLayout mAppBarLayout;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TopicFragment.
     */
    public static TopicFragment newInstance(Topic topic) {
        TopicFragment fragment = new TopicFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TOPIC, topic);
        fragment.setArguments(args);
        return fragment;
    }

    public TopicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTopic = getArguments().getParcelable(ARG_TOPIC);
        }

        mComments = new MultiList<>();
        mMaxPage = 1;
        mCurPage = 1;
        mIsLoaded = false;
        mLastFocusPos = NO_POSITION;

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_topic, container, false);
        initJumpBackButton(rootView);

        mLayout = rootView.findViewById(R.id.comments_layout);
        mLayout.setColorSchemeResources(R.color.material_blue_grey_500, R.color.material_blue_grey_700, R.color.material_blue_grey_900);
        mLayout.setOnRefreshListener(this);

        mCommentsView = mLayout.findViewById(R.id.comments);

        if (!mTopic.hasInfo()) {
            mCommentsView.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    private void initCommentsView(TopicActivity activity) {
        mCommentsLayoutManager = new LinearLayoutManager(activity);
        mCommentsView.setLayoutManager(mCommentsLayoutManager);
        mCommentsView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST));

        mCommentAdapter = new CommentAdapter(this, this, this, this);
        mCommentAdapter.setTopic(mTopic);
        mCommentAdapter.setDataSource(mComments);
        mCommentsView.setAdapter(mCommentAdapter);
        mCommentsView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                loadNextPageIfNeed(mCommentAdapter.getItemCount(), mCommentsView.getChildAdapterPosition(view));
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {

            }
        });
    }

    private void initJumpBackButton(View rootView) {
        mJumpBack = ((ImageButton) rootView.findViewById(R.id.btn_jump_back));
        mJumpBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preconditions.checkState(mLastFocusPos != NO_POSITION, "why jump button show without dest");

                TopicFragment.this.scrollToPos(NO_POSITION, mLastFocusPos);
            }
        });
        mJumpBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(TopicFragment.this.getActivity(), R.string.toast_jump_to_last_read_pos, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final TopicActivity activity = (TopicActivity) getActivity();
        activity.setTitle(null);

        final ActionBar actionBar = activity.getSupportActionBar();
        Preconditions.checkNotNull(actionBar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAppBarLayout = activity.getAppBarLayout();

        initCommentsView(activity);

        setIsLoading(true);
        getLoaderManager().initLoader(0, null, this);
    }

    private void setIsLoading(boolean isLoading) {
        mIsLoading = isLoading;
        mLayout.setRefreshing(isLoading);
    }

    @Override
    public void onStart() {
        super.onStart();

        mDraft = null;
        final Draft draft = DraftDao.get(mTopic.getId());
        if (draft == null) {
            return;
        }

        if (draft.isExpired()) {
            DraftDao.delete(draft.mId);
            return;
        }

        mDraft = draft;
    }

    @Override
    public void onRefresh() {
        final TopicLoader loader = getLoader();
        if (loader == null) {
            return;
        }
        loader.forceLoad();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mIsLoaded) {
            return;
        }

        inflater.inflate(R.menu.menu_topic, menu);

        if (UserState.INSTANCE.isLoggedIn()) {
            mFavIcon = menu.findItem(R.id.action_fav);
            updateFavIcon();

            if (PrefStore.getInstance().isAlwaysShowReplyForm()) {
                menu.findItem(R.id.action_reply).setVisible(false);
            }
        } else {
            for (int i : MENU_REQUIRED_LOGGED_IN) {
                menu.findItem(i).setVisible(false);
            }
        }

        setupShareActionMenu(menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateFavIcon() {
        mFavIcon.setIcon(mFavored ? R.drawable.ic_favorite_white_24dp
                : R.drawable.ic_favorite_border_white_24dp);
    }

    private void setupShareActionMenu(Menu menu) {
        final MenuItem itemShare = menu.findItem(R.id.action_share);

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                String.format("%s\n%s", mTopic.getTitle(), mTopic.getUrl()));

        if (MiscUtils.HAS_L) {
            itemShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    TopicFragment.this.startActivity(Intent.createChooser(shareIntent, null));
                    return true;
                }
            });
        } else {
            final ShareActionProvider actionProvider = new ShareActionProvider(getContext());
            MenuItemCompat.setActionProvider(itemShare, actionProvider);
            actionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_link:
                MiscUtils.setClipboard(getActivity(), getString(R.string.desc_topic_link),
                        String.format("%s\n%s", Html.fromHtml(mTopic.getTitle()).toString(), mTopic.getUrl()));
                return true;
            case R.id.action_copy:
                final String title = Html.fromHtml(mTopic.getTitle()).toString();
                MiscUtils.setClipboard(getActivity(),
                        ClipData.newHtmlText(title,
                                String.format("%s\n%s", title,
                                        Html.fromHtml(mTopic.getContent()).toString()),
                                String.format("<p>%s</p>%s", mTopic.getTitle(),
                                        mTopic.getContent())));
                return true;
            case R.id.action_refresh:
                setIsLoading(true);
                onRefresh();
                return true;
            case R.id.action_fav:
                onFavTopic();
                return true;
            case R.id.action_reply:
                toggleReplyForm();
                return true;
            case R.id.action_thank:
                onThank(mTopic);
                return true;
            case R.id.action_ignore:
                onIgnore(mTopic, true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleReplyForm() {
        boolean isShow;
        if (mReplyForm == null) {
            final View rootView = getView();
            Preconditions.checkNotNull(rootView);
            final ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.reply_form);
            mReplyForm = new ReplyFormHelper(getActivity(), viewStub, this);

            isShow = true;
        } else {
            mReplyForm.toggle();
            isShow = mReplyForm.getVisibility();
        }

        if (isShow) {
            mAppBarLayout.setExpanded(false);
        }
        TrackerUtils.onTopicSwitchReply(isShow);
    }

    @Override
    public Loader<LoaderResult<TopicWithComments>> onCreateLoader(int id, Bundle args) {
        String log = String.format("load topic, id: %d, title: %s", mTopic.getId(), mTopic.getTitle());
        Crashlytics.log(log);
        LogUtils.d(TAG, log);
        return new TopicLoader(getActivity(), mTopic);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<TopicWithComments>> loader, LoaderResult<TopicWithComments> result) {
        if (result.hasException()) {
            handleLoadException(result.mException);
            return;
        }

        final TopicWithComments data = result.mResult;
        if (!mIsLoaded) {
            if (!mTopic.hasInfo()) {
                mCommentsView.setVisibility(View.VISIBLE);
            }
            if (data.mLastReadPos > 0) {
                // add one for topic in header
                mLastFocusPos = data.mLastReadPos + 1;
                updateJumpBackButton();
            }
        }
        mIsLoaded = true;
        mLastIsFailed = false;

        mCommentAdapter.setTopic(data.mTopic);
        mTopic = data.mTopic;

        mCurPage = data.mCurPage;
        mMaxPage = data.mMaxPage;
        final int oldSize = mComments.listSize();
        if (mCurPage > oldSize) {
            // new page
            mComments.addList(data.mComments);
        } else {
            mComments.setList(mCurPage - 1, data.mComments);
        }

        mCommentAdapter.notifyDataSetChanged();

        mFavored = mTopic.isFavored();
        mCsrfToken = data.mCsrfToken;
        mOnceToken = data.mOnceToken;

        getActivity().invalidateOptionsMenu();

        if (mReplyForm == null &&
                UserState.INSTANCE.isLoggedIn() &&
                PrefStore.getInstance().isAlwaysShowReplyForm()) {
            toggleReplyForm();
        }
        if (mDraft != null) {
            if (mReplyForm == null || !mReplyForm.getVisibility()) {
                toggleReplyForm();
            }
            mReplyForm.setContent(mDraft.mContent);

            DraftDao.delete(mDraft.mId);
            mDraft = null;
        }

        setIsLoading(false);
    }

    private void handleLoadException(Exception exception) {
        mLastIsFailed = true;
        setIsLoading(false);
        mCurPage = Math.max(mComments.listSize(), 1);
        boolean finishActivity = false;
        boolean handled = false;

        if (exception instanceof RequestException) {
            final RequestException ex = (RequestException) exception;

            @StringRes
            int strId = 0;
            switch (ex.getCode()) {
                case HttpStatus.SC_NOT_FOUND:
                    strId = R.string.toast_topic_not_found;
                    break;
                case HttpStatus.SC_MOVED_TEMPORARILY:
                    final String location = ex.getResponse().header(HttpHeaders.LOCATION);
                    if (location.equals("/")) {
                        // it's blocked for new user
                        strId = R.string.toast_topic_not_found;
                    }
                    break;
            }

            if (strId != 0) {
                if (getUserVisibleHint()) {
                    Toast.makeText(getActivity(), strId, Toast.LENGTH_SHORT).show();
                }
                finishActivity = true;
                handled = true;
            }
        }

        if (!handled) {
            finishActivity = ExceptionUtils.handleExceptionNoCatch(this, exception);
        }

        if (finishActivity) {
            getActivity().finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<TopicWithComments>> loader) {
        mCommentAdapter.setDataSource(null);
        mComments.clear();

        mCsrfToken = null;
        mOnceToken = null;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mIsLoaded) {
            TopicDao.updateLastRead(mTopic);

            if (mReplyForm != null) {
                // save comment draft
                final Editable content = mReplyForm.getContent();
                if (TextUtils.isEmpty(content)) {
                    return;
                }

                DraftDao.update(mTopic.getId(), content.toString());

                Toast.makeText(getActivity(), R.string.toast_reply_saved_as_draft, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onReply(final CharSequence content) {
        TrackerUtils.onTopicReply();

        doActionRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestHelper.INSTANCE.reply(mTopic, content.toString(), mOnceToken);
                } catch (ConnectionException | RemoteException e) {
                    ExecutorUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TopicFragment.this.doActionException(e);
                        }
                    });
                    return;
                }

                AppCtx.getEventBus().post(new TopicEvent(TopicEvent.TYPE_REPLY));
            }
        }, new Function<Future<?>, Void>() {
            @Override
            public Void apply(Future<?> future) {
                if (TopicFragment.this.cancelRequest(future)) {
                    mReplyForm.setContent(content);
                    if (!mReplyForm.getVisibility()) {
                        mReplyForm.toggle();
                    }
                }
                return null;
            }
        });

        mReplyForm.setVisibility(false);
    }

    private void doActionException(Exception e) {
        ExceptionUtils.handleExceptionNoCatch(this, e);
        getActivity().finish();
    }

    @Subscribe
    public void onTopicEvent(TopicEvent e) {
        AppCtx.getEventBus().unregister(this);
        if (e.mType == TopicEvent.TYPE_REPLY) {
            if (mDraft != null) {
                DraftDao.delete(mDraft.mId);
                mDraft = null;
            }
            mReplyForm.setContent(null);
        } else if (e.mType == TopicEvent.TYPE_IGNORE_TOPIC) {
            Toast.makeText(getActivity(), R.string.toast_topic_ignored, Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        } else if (e.mType == TopicEvent.TYPE_FAV_TOPIC) {
            updateFavIcon();
            return;
        }

        onRefresh();
    }

    private void doActionRequest(final Runnable sendAction, final Function<Future<?>, Void> cancelCallback) {
        AppCtx.getEventBus().register(this);
        mLayout.setRefreshing(true);

        final Snackbar snackbar = Snackbar.make(mLayout, R.string.toast_sending, Snackbar.LENGTH_LONG);
        if (PrefStore.getInstance().isUndoEnabled()) {
            final ScheduledFuture<?> future = ExecutorUtils.schedule(sendAction, 3, TimeUnit.SECONDS);
            snackbar.setAction(R.string.action_cancel, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelCallback.apply(future);
                }
            });
        } else {
            ExecutorUtils.execute(new Runnable() {
                @Override
                public void run() {
                    sendAction.run();
                    ExecutorUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            snackbar.dismiss();
                        }
                    });
                }
            });
        }
        snackbar.show();
    }

    @Override
    public void onCommentIgnore(final Comment comment) {
        onIgnore(comment, false);
    }

    private void onIgnore(final Ignorable obj, final boolean isTopic) {
        doActionRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestHelper.INSTANCE.ignore(obj, mOnceToken);
                } catch (ConnectionException | RemoteException e) {
                    ExecutorUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TopicFragment.this.doActionException(e);
                        }
                    });
                    return;
                }

                AppCtx.getEventBus().post(new TopicEvent(isTopic ? TopicEvent.TYPE_IGNORE_TOPIC
                        : TopicEvent.TYPE_IGNORE_COMMENT));
            }
        }, new Function<Future<?>, Void>() {
            @Override
            public Void apply(Future<?> future) {
                TopicFragment.this.cancelRequest(future);
                return null;
            }
        });
    }

    private boolean cancelRequest(Future<?> future) {
        if (future.cancel(false)) {
            AppCtx.getEventBus().unregister(this);
            mLayout.setRefreshing(false);
            return true;
        }

        Snackbar.make(mLayout, R.string.toast_cancel_failed, Snackbar.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onCommentThank(final Comment comment) {
        onThank(comment);
    }

    private void onThank(final Thankable obj) {
        doActionRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestHelper.INSTANCE.thank(obj, mCsrfToken);
                } catch (ConnectionException | RemoteException e) {
                    ExecutorUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TopicFragment.this.doActionException(e);
                        }
                    });
                    return;
                }

                AppCtx.getEventBus().post(new TopicEvent(TopicEvent.TYPE_THANK));
            }
        }, new Function<Future<?>, Void>() {
            @Override
            public Void apply(Future<?> future) {
                TopicFragment.this.cancelRequest(future);
                return null;
            }
        });
    }

    @Override
    public void onCommentReply(Comment comment) {
        if (mReplyForm == null || !mReplyForm.getVisibility()) {
            toggleReplyForm();
        }

        mReplyForm.getContent().append("@").append(comment.getMember().getUsername()).append(" ");
        mReplyForm.requestFocus();
    }

    @Override
    public void onCommentCopy(Comment comment, String content) {
        final FragmentActivity context = getActivity();
        MiscUtils.setClipboard(context, ClipData.newHtmlText(null, content, comment.getContent()));
    }

    @Override
    public void onCommentUrlClick(String url, int pos) {
        if (url.startsWith(MiscUtils.PREFIX_MEMBER)) {
            findComment(Member.getNameFromUrl(url), pos);
            return;
        }

        onUrlClick(url);
    }

    private void findComment(String member, int pos) {
        for (int i = pos - 1; i >= 0; i--) {
            final Comment comment = mComments.get(i);
            if (comment.getMember().getUsername().equals(member)) {
                scrollToPos(pos, i + 1);
                return;
            }
        }

        if (mTopic.getMember().getUsername().equals(member)) {
            scrollToPos(pos, 0);
            return;
        }

        Toast.makeText(getActivity(), getString(R.string.toast_can_not_found_comments_of_the_author,
                member), Toast.LENGTH_SHORT).show();
    }

    private void scrollToPos(int curPos, final int destPos) {
        mLastFocusPos = curPos;
        updateJumpBackButton();

        mCommentsView.scrollToPosition(destPos);
        mCommentsView.postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = mCommentsLayoutManager.findViewByPosition(destPos);
                if (view == null) {
                    return;
                }
                TopicFragment.this.highlightRow(view);
            }
        }, 200);
    }

    @Override
    public void onUrlClick(String url) {
        try {
            MiscUtils.openUrl(getActivity(), url);
        } catch (ActivityNotFoundException e) {
            LogUtils.i(TAG, "can't start activity for: %s", e, url);
            Toast.makeText(getActivity(), R.string.toast_activity_not_found,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onImageClick(String source) {
        onUrlClick(source);
    }

    @Override
    public void onNodeOpen(Node node) {
        final Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.BUNDLE_NODE, node);
        startActivity(intent);
    }

    @Override
    public void onMemberClick(Member member) {
        onUrlClick(member.getUrl());
    }

    private void onFavTopic() {
        AppCtx.getEventBus().register(this);
        mFavored = !mFavored;
        updateFavIcon();

        ExecutorUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestHelper.INSTANCE.favor(mTopic, mFavored, mCsrfToken);
                } catch (ConnectionException | RemoteException e) {
                    LogUtils.w(TAG, "favorite topic failed", e);
                    mFavored = !mFavored;
                }

                AppCtx.getEventBus().post(new TopicEvent(TopicEvent.TYPE_FAV_TOPIC));
            }
        });
    }

    private TopicLoader getLoader() {
        return (TopicLoader) getLoaderManager().<LoaderResult<TopicWithComments>>getLoader(0);
    }

    private void loadNextPageIfNeed(int totalItemCount, int lastVisibleItem) {
        if (mIsLoading || mLastIsFailed || (mCurPage >= mMaxPage)) {
            return;
        }

        if ((totalItemCount - lastVisibleItem) > 20) {
            return;
        }

        final TopicLoader loader = getLoader();

        setIsLoading(true);
        loader.setPage(mCurPage + 1);
        loader.startLoading();
    }

    private void highlightRow(View view) {
        float width = view.getWidth() / 20;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, -width, width, 0);
        animator.setInterpolator(null);
        animator.start();
    }

    private void updateJumpBackButton() {
        mJumpBack.setVisibility(mLastFocusPos == NO_POSITION ? View.GONE : View.VISIBLE);
    }
}
