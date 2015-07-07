package com.czbix.v2ex.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.ui.util.Html;
import com.czbix.v2ex.util.ExecutorUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TopicEditActivity extends AppCompatActivity {
    public static final String KEY_NODE = "node";

    private Node mNode;
    private TextView mSelectedNode;
    private EditText mTitle;
    private EditText mContent;
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_edit);

        mNode = getNodeFromIntent();

        mLayout = findViewById(R.id.edit_layout);
        mSelectedNode = (TextView) findViewById(R.id.select_node);
        mTitle = (EditText) findViewById(R.id.title);
        mContent = (EditText) findViewById(R.id.content);

        mSelectedNode.setText(getString(R.string.tv_selected_node, mNode.getTitle()));

        final ActionBar actionBar = getSupportActionBar();
        Preconditions.checkNotNull(actionBar);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    private Node getNodeFromIntent() {
        final Intent intent = getIntent();
        Node node = intent.getParcelableExtra(KEY_NODE);
        if (node == null && BuildConfig.DEBUG) {
            node = NodeDao.get("sandbox");
        }
        Preconditions.checkNotNull(node, "intent don't has node");

        return node;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topic_edit, menu);
        final MenuItem item = menu.findItem(R.id.action_post);

        final Drawable icon = DrawableCompat.wrap(ContextCompat.getDrawable(this,
                R.drawable.ic_send_black_24dp));
        DrawableCompat.setTint(icon, Color.WHITE);
        item.setIcon(icon);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_post:
                postTopic();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void postTopic() {
        if (TextUtils.isEmpty(mTitle.getText())) {
            mTitle.setError(getString(R.string.error_field_required));
            return;
        }

        final String title = mTitle.getText().toString();
        final String content = mContent.getText().toString();

        final ScheduledFuture<?> future = ExecutorUtils.schedule(new Runnable() {
            @Override
            public void run() {
                final int id;
                try {
                    id = RequestHelper.newTopic(mNode.getName(), title, content);
                } catch (ConnectionException | RemoteException e) {
                    // TODO
                    e.printStackTrace();
                    return;
                } catch (RequestException e) {
                    final String html = e.getErrorHtml();
                    if (Strings.isNullOrEmpty(html)) {
                        throw e;
                    }

                    showErrMsg(html);
                    return;
                }

                mLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        final Intent intent = new Intent(TopicEditActivity.this, TopicActivity.class);
                        intent.putExtra(TopicActivity.KEY_TOPIC_ID, id);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }, 3, TimeUnit.SECONDS);

        mLayout.setEnabled(false);
        Snackbar.make(mLayout, R.string.toast_sending, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelRequest(future);
                    }
                }).show();
    }

    private void showErrMsg(final String html) {
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                final View layout = getLayoutInflater().inflate(R.layout.view_alert_dialog, null);
                final TextView textView = (TextView) layout.findViewById(R.id.content);
                textView.setText(Html.fromHtml(html));

                final AlertDialog.Builder builder = new AlertDialog.Builder(TopicEditActivity.this);
                builder.setTitle(R.string.toast_post_failed).setView(layout)
                        .setCancelable(false)
                        .setPositiveButton(R.string.action_edit, null)
                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            }
        });
    }

    private void cancelRequest(Future<?> future) {
        mLayout.setEnabled(true);
        if (future.cancel(false)) {
            return;
        }

        Snackbar.make(mLayout, R.string.toast_cancel_failed, Snackbar.LENGTH_LONG).show();
    }
}
