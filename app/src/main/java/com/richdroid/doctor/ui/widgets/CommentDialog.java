package com.richdroid.doctor.ui.widgets;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.richdroid.doctor.R;
import com.richdroid.doctor.models.Comment;

import java.util.ArrayList;

/**
 * Created by richa.khanna on 11/29/15.
 */
public class CommentDialog extends DialogFragment {

    private static final String COMMENT_LIST = "comment_list";
    private static Context mContext;
    private String mHeaderText;
    private String mMessageText;
    private ArrayList<Comment> mCommentArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCommentArrayList = getArguments().getParcelableArrayList(CommentDialog.COMMENT_LIST);
        }

        mHeaderText = mContext.getResources().getString(R.string.comment_title);
        mMessageText = mContext.getResources().getString(R.string.no_comment_msg);
    }

    public CommentDialog() {
    }


    public static CommentDialog newInstance(Context context, ArrayList<Comment> commentList) {
        mContext = context;
        CommentDialog commentDialog = new CommentDialog();
        Bundle bundle = new Bundle();
        if (null != commentList && commentList.size() > 0) {
            bundle.putParcelableArrayList(CommentDialog.COMMENT_LIST, commentList);
        }
        commentDialog.setArguments(bundle);
        commentDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        return commentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView;

        if (mCommentArrayList != null && mCommentArrayList.size() > 0) {
            contentView = inflater.inflate(R.layout.view_comment_list_dialog, container, false);
            ListView mListView = (ListView) contentView.findViewById(R.id.item_list);
            initializeClickButtons(contentView);
            CommentListAdapter cancelListAdapter =
                    new CommentListAdapter(mContext, mCommentArrayList);
            mListView.setAdapter(cancelListAdapter);
            cancelListAdapter.notifyDataSetChanged();
            mListView.setFocusable(false);

        } else {
            contentView = inflater.inflate(R.layout.view_message_dialog, container, false);
            initializeClickButtons(contentView);
            AlertDialog mCancelConfirmAlertDialog = new AlertDialog.Builder(mContext).create();
            mCancelConfirmAlertDialog.setView(contentView);
            ((TextView) contentView.findViewById(R.id.item_header)).setText(mHeaderText);
            ((TextView) contentView.findViewById(R.id.item_message)).setText(mMessageText);
        }
        setCancelable(false);
        return contentView;
    }

    private void initializeClickButtons(View contentView) {
        View mButtonClose = contentView.findViewById(R.id.button_close);
        mButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


    private class CommentListAdapter extends BaseAdapter {

        private final Context mContext;
        private final ArrayList<Comment> commentArrayList;
        private final LayoutInflater mLayoutInflater;

        public CommentListAdapter(Context context, ArrayList<Comment> commentList) {
            this.mContext = context;
            this.commentArrayList = commentList;
            this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return (null != commentArrayList ? commentArrayList.size() : 0);
        }

        @Override
        public Object getItem(int i) {
            return commentArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean isEmpty() {
            return commentArrayList.isEmpty();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_item_layout, parent, false);
                holder = new ViewHolder();
                holder.tvIssueUser = (TextView) convertView.findViewById(R.id.tv_issue_title);
                holder.tvIssueBody = (TextView) convertView.findViewById(R.id.tv_issue_body);
                // SetTag is used to associate an object with a View.
                convertView.setTag(holder);
            } else {
                // Returns the Object Stored in this view.
                holder = (ViewHolder) convertView.getTag();
            }

            String title = commentArrayList.get(position).getUser();
            holder.tvIssueUser.setText(title);

            String body = commentArrayList.get(position).getBody();
            holder.tvIssueBody.setText(body);


            return convertView;
        }

        private class ViewHolder {
            private TextView tvIssueUser;
            private TextView tvIssueBody;
        }
    }
}