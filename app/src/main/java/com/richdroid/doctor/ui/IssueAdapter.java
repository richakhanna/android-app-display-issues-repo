package com.richdroid.doctor.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.richdroid.doctor.R;
import com.richdroid.doctor.models.Issue;

import java.util.ArrayList;

/**
 * Created by richa.khanna on 11/28/15.
 */
public class IssueAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private final ArrayList<Issue> mIssueList;

    public IssueAdapter(Context context, ArrayList<Issue> issueList) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIssueList = issueList;
    }

    @Override
    public int getCount() {
        return (null != mIssueList ? mIssueList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return mIssueList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item_layout, parent, false);
            holder = new ViewHolder();
            holder.tvIssueTitle = (TextView) convertView.findViewById(R.id.tv_issue_title);
            holder.tvIssueBody = (TextView) convertView.findViewById(R.id.tv_issue_body);

            // SetTag is used to associate an object with a View.
            convertView.setTag(holder);
        } else {
            // Returns the Object Stored in this view.
            holder = (ViewHolder) convertView.getTag();
        }

        String title = mIssueList.get(position).getTitle();
        holder.tvIssueTitle.setText(title);

        String body = mIssueList.get(position).getBody();
        holder.tvIssueBody.setText(body);

        return convertView;
    }

    private static class ViewHolder {
        private TextView tvIssueTitle;
        private TextView tvIssueBody;
    }
}
