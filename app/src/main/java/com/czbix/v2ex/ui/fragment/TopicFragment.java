package com.czbix.v2ex.ui.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.ListView;
import android.widget.Toast;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.dao.DraftDao;
import com.czbix.v2ex.eventbus.CommentEvent;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.IgnoreAble;
import com.czbix.v2ex.model.ThankAble;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.czbix.v2ex.model.db.Draft;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.ui.TopicActivity;
import com.czbix.v2ex.ui.adapter.CommentAdapter;
import com.czbix.v2ex.ui.adapter.TopicAdapter;
import com.czbix.v2ex.ui.helper.ReplyFormHelper;
import com.czbix.v2ex.ui.loader.TopicLoader;
import com.czbix.v2ex.ui.widget.HtmlMovementMethod;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.MiscUtils;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TopicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<TopicWithComments>,
        ReplyFormHelper.OnReplyListener, CommentAdapter.OnCommentActionListener, HtmlMovementMethod.OnHtmlActionListener {
    private static final String TAG = TopicFragment.class.getSimpleName();
    private static final String ARG_TOPIC = "topic";
    private static final int[] MENU_REQUIRED_LOGGED_IN = {R.id.action_ignore, R.id.action_reply, R.id.action_thank};

    private Topic mTopic;
    private SwipeRefreshLayout mLayout;
    private ListView mCommentsView;
    private TopicAdapter.ViewHolder mTopicHolder;
    private CommentAdapter mCommentAdapter;
    private View mTopicView;
    private ReplyFormHelper mReplyForm;
    private String mCsrfToken;
    private String mOnceToken;
    private Draft mDraft;


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

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_topic, container, false);
        mLayout = ((SwipeRefreshLayout) rootView.findViewById(R.id.comments_layout));
        mLayout.setOnRefreshListener(this);

        mCommentsView = ((ListView) mLayout.findViewById(R.id.comments));

        mTopicView = inflater.inflate(R.layout.view_topic, mCommentsView, false);
        mTopicView.setBackgroundColor(Color.WHITE);

        mTopicHolder = new TopicAdapter.ViewHolder(mTopicView);
        mTopicHolder.setContentListener(this);
        mTopicHolder.fillData(mTopic);

        mCommentAdapter = new CommentAdapter(getActivity(), mTopicView, this);
        mCommentsView.setAdapter(mCommentAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final TopicActivity activity = (TopicActivity) getActivity();
        activity.setTitle(null);

        final ActionBar actionBar = activity.getSupportActionBar();
        Preconditions.checkNotNull(actionBar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mLayout.setRefreshing(true);
        getLoaderManager().initLoader(0, null, this);
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
        final Loader<?> loader = getLoaderManager().getLoader(0);
        if (loader == null) {
            return;
        }
        loader.forceLoad();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mTopic.hasInfo()) {
            return;
        }

        inflater.inflate(R.menu.menu_topic, menu);

        if (UserState.getInstance().isAnonymous()) {
            for (int i : MENU_REQUIRED_LOGGED_IN) {
                menu.findItem(i).setVisible(false);
            }
        }

        setupShareActionMenu(menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupShareActionMenu(Menu menu) {
        final ShareActionProvider actionProvider = ((ShareActionProvider)
                MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share)));
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTopic.getUrl());
        actionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_link:
                MiscUtils.setClipboard(getActivity(), getString(R.string.desc_topic_link),
                        mTopic.getUrl());
                return true;
            case R.id.action_refresh:
                mLayout.setRefreshing(true);
                onRefresh();
                return true;
            case R.id.action_reply:
                toggleReplyForm();
                return true;
            case R.id.action_thank:
                onThank(mTopic);
                return true;
            case R.id.action_ignore:
                onIgnore(mTopic);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleReplyForm() {
        if (mReplyForm != null) {
            mReplyForm.toggle();
            return;
        }

        final View rootView = getView();
        Preconditions.checkNotNull(rootView);
        final ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.reply_form);
        mReplyForm = new ReplyFormHelper(viewStub, this);
    }

    @Override
    public Loader<TopicWithComments> onCreateLoader(int id, Bundle args) {
        LogUtils.d(TAG, "load topic, id: %d, title: %s", mTopic.getId(), mTopic.getTitle());
        return new TopicLoader(getActivity(), mTopic);
    }

    @Override
    public void onLoadFinished(Loader<TopicWithComments> loader, TopicWithComments data) {
        mTopic = data.mTopic;
        mTopicHolder.fillData(mTopic, true);
        mCommentAdapter.setDataSource(data.mComments);
        mLayout.setRefreshing(false);
        getActivity().invalidateOptionsMenu();

        mCsrfToken = data.mCsrfToken;
        mOnceToken = data.mOnceToken;

        if (mDraft != null) {
            Preconditions.checkState(mReplyForm == null);

            toggleReplyForm();
            mReplyForm.setContent(mDraft.mContent);

            DraftDao.delete(mDraft.mId);
            mDraft = null;
        }
    }

    @Override
    public void onLoaderReset(Loader<TopicWithComments> loader) {
        mCommentAdapter.setDataSource(null);
        mCsrfToken = null;
        mOnceToken = null;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mReplyForm == null) {
            return;
        }

        final Editable content = mReplyForm.getContent();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        DraftDao.insert(mTopic.getId(), content.toString());

        Toast.makeText(getActivity(), R.string.toast_reply_saved_as_draft, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReply(final CharSequence content) {
        AppCtx.getEventBus().register(this);
        final ScheduledFuture<?> future = ExecutorUtils.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestHelper.reply(mTopic, content.toString(), mOnceToken);
                } catch (ConnectionException | RemoteException e) {
                    e.printStackTrace();
                    return;
                }

                AppCtx.getEventBus().post(new CommentEvent(true));
            }
        }, 3, TimeUnit.SECONDS);

        mLayout.setRefreshing(true);
        Snackbar.make(mLayout, R.string.toast_sending, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cancelRequest(future)) {
                            mReplyForm.setContent(content);
                        }
                    }
                }).show();

        mReplyForm.setVisibility(false);
    }

    @Subscribe
    public void onCommentRequestFinish(CommentEvent e) {
        AppCtx.getEventBus().unregister(this);
        if (e.mIsReply) {
            mReplyForm.setContent(null);
        }
        onRefresh();
    }

    @Override
    public void onCommentIgnore(final Comment comment) {
        onIgnore(comment);
    }

    private void onIgnore(final IgnoreAble obj) {
        AppCtx.getEventBus().register(this);
        final ScheduledFuture<?> future = ExecutorUtils.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestHelper.ignore(obj, mOnceToken);
                } catch (ConnectionException | RemoteException e) {
                    e.printStackTrace();
                    return;
                }

                AppCtx.getEventBus().post(new CommentEvent());
            }
        }, 3, TimeUnit.SECONDS);

        showSendingMsg(future);
    }

    private void showSendingMsg(final ScheduledFuture<?> future) {
        mLayout.setRefreshing(true);
        Snackbar.make(mLayout, R.string.toast_sending, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelRequest(future);
                    }
                }).show();
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

    private void onThank(final ThankAble obj) {
        AppCtx.getEventBus().register(this);
        final ScheduledFuture<?> future = ExecutorUtils.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestHelper.thank(obj, mCsrfToken);
                } catch (ConnectionException | RemoteException e) {
                    e.printStackTrace();
                    return;
                }

                AppCtx.getEventBus().post(new CommentEvent());
            }
        }, 3, TimeUnit.SECONDS);

        showSendingMsg(future);
    }

    @Override
    public void onCommentReply(Comment comment) {
        if (mReplyForm == null) {
            toggleReplyForm();
        } else {
            mReplyForm.setVisibility(true);
        }

        mReplyForm.getContent().append("@").append(comment.getMember().getUsername()).append(" ");
        mReplyForm.requestFocus();
    }

    @Override
    public void onCommentCopy(Comment comment) {
        final FragmentActivity context = getActivity();
        MiscUtils.setClipboard(context, null, comment.getContent());
    }

    @Override
    public void onCommentUrlClick(String url) {
        onUrlClick(url);
    }

    @Override
    public void onUrlClick(String url) {
        startActivity(MiscUtils.getUrlIntent(url));
    }
}
