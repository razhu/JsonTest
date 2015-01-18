package com.casasmap.fromblog3raparte;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ListActivity{
    //protected String[] mBlogPostTitles;
    //private ListView mListView;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String URL_QUERY = "http://blog.teamtreehouse.com/api/get_recent_summary/?count=";
    private static final int NUMBER_OF_POSTS = 20;
    protected JSONObject mBlogData;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        if(isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();
            Toast.makeText(this, getString(R.string.yes_network), Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        super.onListItemClick(l, v, position, id);
        JSONArray jsonPosts = null;
        try {
            jsonPosts = mBlogData.getJSONArray("posts");
            JSONObject jsonPost = jsonPosts.getJSONObject(position);
            String theUrl = jsonPost.getString("url");
            Intent intent = new Intent(this, BlogWebViewActivity.class);
            intent.setData(Uri.parse(theUrl));

            startActivity(intent);
        } catch (JSONException e) {
            Log.e(TAG, "Exception caught!", e);
        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }


    private void handleBlogResponse() {
        mProgressBar.setVisibility(View.INVISIBLE);
        if(mBlogData == null){

            updateDisplayForError();

        }else{
            try {
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                ArrayList<HashMap<String, String>> blogPosts =
                        new ArrayList<HashMap<String, String>>();
                for(int i=0; i < jsonPosts.length(); i++){
                    JSONObject post = jsonPosts.getJSONObject(i);
                    String title = post.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();
                    String author = post.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();

                    HashMap<String, String> blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_TITLE, title);
                    blogPost.put(KEY_AUTHOR, author);

                    blogPosts.add(blogPost);

                }
                String[] keys = {KEY_TITLE, KEY_AUTHOR};
                int[] ids = {android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, keys, ids);
                setListAdapter(adapter);

//                mBlogPostTitles = new String[jsonPosts.length()];
//                for(int i=0; i < jsonPosts.length(); i++){
//                        JSONObject post  = jsonPosts.getJSONObject(i);
//                    String title = post.getString("title");
//                    mBlogPostTitles[i] = Html.fromHtml(title).toString();
//                }
//                ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this,
//                        android.R.layout.simple_list_item_1, mBlogPostTitles);
//                mListView.setAdapter(stringArrayAdapter);

            } catch (JSONException e) {
                Log.e(TAG, "Exception caught!", e);
            }
        }

    }

    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error_title))
                .setMessage(getString(R.string.error_message))
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        mTextView = (TextView)findViewById(R.id.textView1);
        mTextView.setText(getString(R.string.no_items));
    }

    private class GetBlogPostsTask extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... params) {
            int responseCode = -1;
            JSONObject jsonResponse = null;
            try{
                URL blogFeedUrl = new URL(URL_QUERY + NUMBER_OF_POSTS);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();
                responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK)
                {
                    // successful response from server
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLenght = connection.getContentLength();
                    char[] charArray = new char[contentLenght];
                    reader.read(charArray);
                    String responseData = new String(charArray);

                    jsonResponse = new JSONObject(responseData);

                }else{
                    Log.i(TAG, "Unsuccessful HttpResponse Code: " + responseCode);
                }

            }
            catch (MalformedURLException e){
                Log.d(TAG, "Exception caught", e);
            } catch (IOException e) {
                Log.d(TAG, "Exception caught", e);
            } catch (Exception e){
                Log.d(TAG, "Exception caught", e);

            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject s) {
            mBlogData = s;
            handleBlogResponse();
        }


    }


}
