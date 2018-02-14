package com.luvira.myvoice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.luvira.myvoice.com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ViewNoteActivity extends AppCompatActivity {

    private static final String BASE_USER_URL = "http://10.0.2.2:3000/api/users/";

    //commment URLs
    private static final String COMMENT_URL = "http://10.0.2.2:3000/api/comments";
    private static final String COMMENT_CONTENT = "content";
    private static final String COMMENT_OWNER = "user_id";

    //declare the fields
    TextView noteTitle;
    TextView noteCategory;
    TextView noteOwner;
    TextView noteContent;

    ListView commentsListView;
    EditText commentEdit;
    String comment;

    Intent parentIntent;
    SharedPreferences mPreferences;

    private List<Map<String, String>> commentsList;
    private ArrayList<String> commentUsernames;
    TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        //make back button visible
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar = getSupportActionBar();

//Display home with the "up" arrow indicator
        actionBar.setDisplayHomeAsUpEnabled(true);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        //initialize the notes
        noteTitle = (TextView) findViewById(R.id.noteTitle);
        noteCategory = (TextView) findViewById(R.id.noteCategory);
        noteOwner = (TextView) findViewById(R.id.noteOwner);
        noteContent = (TextView) findViewById(R.id.noteContent);
        commentEdit = (EditText) findViewById(R.id.addCommentEdit);

        //get values from intent
        parentIntent = getIntent();

        if(parentIntent.getBooleanExtra("isFromNew",false)) {
            Toast.makeText(ViewNoteActivity.this, "Your note has been created successfully",
                    Toast.LENGTH_SHORT).show();
        }

        noteTitle.setText(parentIntent.getStringExtra("noteTitle"));
        noteCategory.setText(parentIntent.getStringExtra("noteCategory"));
        noteOwner.setText("by " + parentIntent.getIntExtra("noteOwner", 0));
        noteContent.setText(parentIntent.getStringExtra("noteContent"));

        //call task to update the username
        GetUsernameTask usernameTask = new GetUsernameTask(ViewNoteActivity.this);
        usernameTask.execute();

        commentsList = new ArrayList<>();
        commentUsernames = new ArrayList<>();


        //call task to get comments for this note
        GetCommentsTask getCommentsTask = new GetCommentsTask(this);
        getCommentsTask.execute();

        refreshComments();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshComments() {
        //update the comment listview
        commentsListView = (ListView) findViewById(R.id.commentsList);
        //empty = (TextView) findViewById(R.id.myempty);

        //commentsListView.setEmptyView(empty);

        SimpleAdapter newAdapter = new SimpleAdapter(this, commentsList,
                R.layout.comment_item,
                new String[]{COMMENT_CONTENT, COMMENT_OWNER},
                new int[]{R.id.commentContent, R.id.commentOwner});

        commentsListView.setAdapter(newAdapter);
    }

    public void onClick(View view) {
        int viewID = view.getId();

        if(viewID == R.id.noteOwner) {
            //get the userID and open activity to view profile
            int userID = parentIntent.getIntExtra("noteOwner", 0);

            Intent profileIntent = new Intent(ViewNoteActivity.this, ViewProfileActivity.class);
            profileIntent.putExtra("userID", userID);

            //start the activity
            startActivity(profileIntent);
            finish();
        }
        else if (viewID == R.id.saveCommentButton) {
            //check if field has data
            comment = commentEdit.getText().toString();
            if (!comment.isEmpty()) {
                //save the comment
                PostCommentTask postCommentTask = new PostCommentTask(ViewNoteActivity.this);
                postCommentTask.execute();
            }
            else {
                Toast.makeText(ViewNoteActivity.this, "Ensure you have entered a comment",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetUsernameTask extends UrlJsonAsyncTask {
        //constructor
        public GetUsernameTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject userJson = null;
            try {
                String specificUserUrl = BASE_USER_URL + parentIntent.getIntExtra("noteOwner", 0);
                URL url = new URL(specificUserUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                StringBuffer buffer = new StringBuffer();

                String line = "";

                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                String response = buffer.toString();

                //json where the username will be extracted from
                userJson = new JSONObject(response);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return userJson;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                String username = json.getString("user_name");

                //set the username
                noteOwner.setText(username);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            super.onPostExecute(json);
        }
    }

    private class PostCommentTask extends UrlJsonAsyncTask {
        //constructor
        public PostCommentTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            JSONObject commentJson = new JSONObject();
            try {

                URL url = new URL(COMMENT_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                //write the email and password to stream

                //use parent intent to get info
                int noteID = parentIntent.getIntExtra("noteID", 0);
                int userID = mPreferences.getInt("userID", 0);

                //OutputStreamWriter writer = new OutputStreamWriter(out);
                String data = "{\"content\": \"" + comment + "\"," +
                        " \"user_id\": " + userID + ", \"note_id\": " + noteID + "}";
                wr.writeBytes(data);
                wr.flush();
                wr.close();

                urlConnection.connect();

                //read the response
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                urlConnection.getResponseCode();

                //check response code
                if(urlConnection.getResponseCode() == 201) {
                    commentJson.put("success", true);
                }
                else {
                    commentJson.put("success", false);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return commentJson;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    //remove the comment from the edit text
                    commentEdit.setText("");
                    Toast.makeText(getApplicationContext(),
                            "Your comment has been posted", Toast.LENGTH_SHORT).show();

                    GetCommentsTask getCommentsTask = new GetCommentsTask(getApplicationContext());
                    getCommentsTask.execute();
                    refreshComments();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Your comment could not be saved", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            super.onPostExecute(json);
        }
    }

    private class GetCommentsTask extends UrlJsonAsyncTask {
        //constructor
        public GetCommentsTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            JSONObject commentJson = null;
            try {
                String specificCommentUrl = COMMENT_URL + "?note_id=" + parentIntent.getIntExtra("noteID", 0);
                URL url = new URL(specificCommentUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                StringBuffer buffer = new StringBuffer();

                String line = "";

                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                String response = buffer.toString();

                //json where the username will be extracted from
                commentJson = new JSONObject(response);

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return commentJson;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray commentsArray = json.getJSONArray("comments");
                JSONArray userNamesArray = json.getJSONArray("user_names");

                for (int looper = 0; looper < commentsArray.length(); looper++) {
                    JSONObject comment = commentsArray.getJSONObject(looper);

                    Map<String, String> tempMap = new HashMap<String, String>();

                    tempMap.put(COMMENT_CONTENT, comment.getString(COMMENT_CONTENT));
                    tempMap.put(COMMENT_OWNER, (String) userNamesArray.get(looper));

                    commentsList.add(tempMap);
                    commentUsernames.add((String) userNamesArray.get(looper));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            super.onPostExecute(json);
        }
    }
}
