package com.czbix.v2ex.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Comment;

import java.util.List;

public class CommentAdapter extends ArrayAdapter<Comment> {
    private final LayoutInflater mInflater;

    public CommentAdapter(Context context) {
        super(context, 0);

        mInflater = LayoutInflater.from(context);
    }

    public void setDataSource(List<Comment> comments) {
        clear();
        if (comments != null) {
            addAll(comments);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_comment, parent , false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
        }

        final Comment comment = getItem(position);
        viewHolder.fillData(comment);

        return convertView;
    }

    private static class ViewHolder {
        private final TextView mTextView;

        public ViewHolder(View view) {
            mTextView = (TextView) view.findViewById(R.id.textView);
        }

        public void fillData(Comment comment) {
            mTextView.setText(Html.fromHtml(comment.getContent()));
        }
    }
}
