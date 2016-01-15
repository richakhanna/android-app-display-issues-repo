package com.richdroid.doctor.ui;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.richdroid.doctor.R;
import com.richdroid.doctor.http.JSONGetWebService;
import com.richdroid.doctor.http.JSONWebServiceResponse;
import com.richdroid.doctor.models.Comment;
import com.richdroid.doctor.models.Issue;
import com.richdroid.doctor.ui.widgets.CommentDialog;
import com.richdroid.doctor.utils.NetworkUtils;
import com.richdroid.doctor.utils.ProgressBarUtil;
import com.richdroid.doctor.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String SCHEME = "https";
    private static final String HOST = "api.github.com";
    private static final String REPOS = "repos";
    private static final String ORG_NAME = "rails";
    private static final String REPO_NAME = "rails";
    private static final String ISSUES = "issues";

    private IssueAdapter mIssueAdapter;
    private ArrayList<Issue> issueList;
    private ArrayList<Comment> commentList;
    private ProgressBarUtil progressBar;
    private AlertDialog mErrorDialog;
    private LinearLayout mNoNetworkRetryLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.rails_open_issues));
        setSupportActionBar(toolbar);

        progressBar = new ProgressBarUtil(this);

        issueList = new ArrayList<Issue>();
        commentList = new ArrayList<Comment>();

        ListView mListView = (ListView) findViewById(R.id.listview_issues);
        mNoNetworkRetryLayout = (LinearLayout) findViewById(R.id.network_retry_full_linearlayout);
        Button retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(this);
        mIssueAdapter = new IssueAdapter(this, issueList);

        mListView.setAdapter(mIssueAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                commentList.clear();
                Issue issue = issueList.get(position);
                int noOfComments = issue.getComments();

                if (noOfComments > 0) {
                    //Network call to Fetch comments for issue if noOfComments > 0
                    if (NetworkUtils.isOnline(MainActivity.this)) {
                        FetchCommentTask fetchCommentTask = new FetchCommentTask(MainActivity.this);
                        fetchCommentTask.execute(String.valueOf(issue.getNumber()));
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.internet_con), Toast.LENGTH_LONG).show();
                    }

                } else {
                    //No need to call network as noOfComments = 0
                    showCommentsForIssueDialog();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchIssueDataIfOnline();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_retry:
                fetchIssueDataIfOnline();
                break;
        }
    }

    private void fetchIssueDataIfOnline() {
        if (NetworkUtils.isOnline(this)) {
            mNoNetworkRetryLayout.setVisibility(View.GONE);
            if (issueList.isEmpty()) {
                FetchOpenIssueTask fetchOpenIssueTask = new FetchOpenIssueTask(this);
                fetchOpenIssueTask.execute();
            }
        } else {
            if (issueList.isEmpty()) {
                mNoNetworkRetryLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private class IssueUpdatedAtComparator implements Comparator<Issue> {
        @Override
        public int compare(Issue i1, Issue i2) {
            return i2.getUpdatedAt().compareTo(i1.getUpdatedAt());
        }
    }


    private class FetchOpenIssueTask extends
            AsyncTask<String, Void, JSONWebServiceResponse> {

        private final String LOG_TAG = FetchOpenIssueTask.class.getSimpleName();
        private JSONWebServiceResponse response;
        private final JSONGetWebService jsonGetWS;
        private final Context context;
        private boolean isApiHit = false;
        private final int noOfChars = 140;

        public FetchOpenIssueTask(Context context) {
            super();
            this.response = new JSONWebServiceResponse(new JSONArray(),
                    HttpURLConnection.HTTP_NO_CONTENT);
            this.jsonGetWS = new JSONGetWebService();
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // To spin the progress bar while loading data
            progressBar.show();
        }

        @Override
        protected JSONWebServiceResponse doInBackground(String... params) {
            Log.d(LOG_TAG, "doInBackground started");

            if (NetworkUtils.isOnline(context)) {

                // Construct the URL for the api.github.com query
                final String STATE_PARAM = "state";
                String state = "open";


                Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME)
                        .authority(HOST)
                        .appendPath(REPOS)
                        .appendPath(ORG_NAME)
                        .appendPath(REPO_NAME)
                        .appendPath(ISSUES)
                        .appendQueryParameter(STATE_PARAM, state);
                Uri builtUri = builder.build();

                try {
                    URL url = new URL(builtUri.toString());
                    Log.v(LOG_TAG, "Built Uri : " + builtUri.toString());

                    response = jsonGetWS.hit(url);
                    isApiHit = true;
                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }

            }

            Log.d(LOG_TAG, "doInBackground completed");
            return response;

        }

        private void getIssueDataFromJsonIntoIssueList(JSONArray jsonArrayResponse) {
            issueList.clear();
            Issue issue;
            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArrayResponse.getJSONObject(i);
                    String title = jsonObject.getString("title");
                    String updatedAt = jsonObject.getString("updated_at");
                    String body = jsonObject.getString("body");
                    int number = jsonObject.getInt("number");
                    int comments = jsonObject.getInt("comments");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = sdf.parse(updatedAt);

                    issue = new Issue();
                    issue.setTitle(title);
                    issue.setUpdatedAt(date);
                    issue.setNumber(number);
                    issue.setComments(comments);

                    if (StringUtils.isNotEmpty(body) && body.length() > noOfChars) {
                        issue.setBody(body.substring(0, noOfChars));
                    } else {
                        issue.setBody("");
                    }

                    issueList.add(issue);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                } catch (ParseException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }

        }

        @Override
        protected void onPostExecute(JSONWebServiceResponse response) {

            progressBar.hide();
            if (response != null && response.getCode() == HttpURLConnection.HTTP_OK) {
                if (isApiHit) {
                    JSONArray jsonArrayResponse = response.getResult();
//                    String jsonString = jsonArrayResponse.toString();
//                    Log.d(LOG_TAG, "jsonString : " + jsonString);
                    getIssueDataFromJsonIntoIssueList(jsonArrayResponse);
                    // Ordered by most-recently updated issue first
                    Collections.sort(issueList, new IssueUpdatedAtComparator());
                    mIssueAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(context, getString(R.string.internet_con),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                showErrorDialog(getString(R.string.sorry_header),
                        getString(R.string.generic_failure_desc));
            }
        }
    }


    private class FetchCommentTask extends
            AsyncTask<String, Void, JSONWebServiceResponse> {

        private final String LOG_TAG = FetchCommentTask.class.getSimpleName();
        private JSONWebServiceResponse response;
        private final JSONGetWebService jsonGetWS;
        private final Context context;
        private boolean isApiHit = false;

        public FetchCommentTask(Context context) {
            super();
            this.response = new JSONWebServiceResponse(new JSONArray(),
                    HttpURLConnection.HTTP_NO_CONTENT);
            this.jsonGetWS = new JSONGetWebService();
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // To spin the progress bar while loading data
            progressBar.show();
        }

        @Override
        protected JSONWebServiceResponse doInBackground(String... params) {
            Log.d(LOG_TAG, "doInBackground started");

            if (NetworkUtils.isOnline(context)) {

                // Construct the URL for the api.github.com query
                final String issueNo = params[0];

                Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME)
                        .authority(HOST)
                        .appendPath(REPOS)
                        .appendPath(ORG_NAME)
                        .appendPath(REPO_NAME)
                        .appendPath(ISSUES)
                        .appendPath(issueNo)
                        .appendPath("comments");
                Uri builtUri = builder.build();
                try {
                    URL url = new URL(builtUri.toString());
                    Log.v(LOG_TAG, "Built Uri : " + builtUri.toString());

                    response = jsonGetWS.hit(url);
                    isApiHit = true;
                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }

            }

            Log.d(LOG_TAG, "doInBackground completed");
            return response;

        }

        private void getCommentDataFromJsonIntoCommentList(JSONArray jsonArrayResponse) {
            commentList.clear();
            Comment comment;
            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArrayResponse.getJSONObject(i);
                    String user = jsonObject.getJSONObject("user").getString("login");
                    String body = jsonObject.getString("body");

                    comment = new Comment();
                    comment.setUser(user);
                    comment.setBody(body);
                    commentList.add(comment);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        @Override
        protected void onPostExecute(JSONWebServiceResponse response) {

            progressBar.hide();
            if (response != null && response.getCode() == HttpURLConnection.HTTP_OK) {
                if (isApiHit) {
                    JSONArray jsonArrayResponse = response.getResult();
//                    String jsonString = jsonArrayResponse.toString();
//                    Log.d(LOG_TAG, "jsonString : " + jsonString);
                    getCommentDataFromJsonIntoCommentList(jsonArrayResponse);
                    showCommentsForIssueDialog();
                } else {
                    Toast.makeText(context, getString(R.string.internet_con),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                showErrorDialog(getString(R.string.sorry_header),
                        getString(R.string.generic_failure_desc));
            }
        }
    }

    /**
     * Method to show a list of comments when user presses on a issue .
     */

    private void showCommentsForIssueDialog() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        CommentDialog mCommentDialog = CommentDialog.newInstance(this, commentList);
        mCommentDialog.show(fragmentTransaction, "CommentDialog");
    }

    private void showErrorDialog(String header, String message) {
        if (mErrorDialog == null || !mErrorDialog.isShowing()) {

            View contentView = LayoutInflater.from(this).inflate(R.layout.view_message_dialog, null, false);
            mErrorDialog = new AlertDialog.Builder(this).setView(contentView).create();
            ((TextView) contentView.findViewById(R.id.item_header)).setText(header);
            ((TextView) contentView.findViewById(R.id.item_message)).setText(message);

            contentView.findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mErrorDialog.dismiss();
                }
            });
            mErrorDialog.setCancelable(false);
            mErrorDialog.show();
        }
    }
}
