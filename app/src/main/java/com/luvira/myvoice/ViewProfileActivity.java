package com.luvira.myvoice;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.luvira.myvoice.com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ViewProfileActivity extends AppCompatActivity {

    private static final String USER_URL = "http://10.0.2.2:3000/api/users/";
    private static final String PROFILE_URL = "http://10.0.2.2:3000/api/profiles/";
    private final String USER_ID = "user_id";
    private final String USERNAME = "user_name";
    private final String EMAIL = "email";
    private final String ABOUT = "about";
    private final String WEBSITE = "website";

    //declare fields
    private TextView usernameView;
    private TextView aboutView;
    private TextView emailView;
    private TextView websiteView;

    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        //initialize the views
        usernameView = (TextView) findViewById(R.id.usernameText);
        aboutView = (TextView) findViewById(R.id.aboutText);
        emailView = (TextView) findViewById(R.id.displayEmailText);
        websiteView = (TextView) findViewById(R.id.websiteText);

        if (getIntent().getBooleanExtra("isFromEdit",false)) {
            Toast.makeText(ViewProfileActivity.this,
                    "Your profile has been updated successfully", Toast.LENGTH_SHORT).show();
        }

        //check if user profile is same as logged in user
        int userID = getIntent().getIntExtra("userID", 0);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        int currentUser = mPreferences.getInt("userID", 0);

        if (currentUser != 0 && userID != 0) {
            //check if users are similar
            if(userID == currentUser) {
                //enable the update profile button
                Button updateProfileButton = (Button) findViewById(R.id.updateProfileButton);
                updateProfileButton.setVisibility(View.VISIBLE);
            }
        }
        //call task to update the views
        GetUserTask userTask = new GetUserTask(this);
        userTask.execute();

        //show dialog if from signing up
        if(getIntent().getBooleanExtra("isFromSignUp", false)) {
            String msg = "Welcome to My Voice " + getIntent().getStringExtra("username") +
                    ".\nFeel free to edit your lonely profile with content. When you are ready, " +
                    "start reading other users' notes from the menu";
            //show dialog
            new AlertDialog.Builder(this)
                    .setTitle("Greetings")
                    .setMessage(msg)
                                    .setPositiveButton("Cool", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.profile_menu, menu);

        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_add_note_item:
                Intent intent = new Intent(this, NewNoteActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.category_item:
                Intent categoryIntent = new Intent(ViewProfileActivity.this, CategoryActivity.class);
                startActivity(categoryIntent);
                finish();
                return true;
            case R.id.profile_settings_item:
                Toast.makeText(ViewProfileActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.profile_help_item:
                Toast.makeText(ViewProfileActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.profile_about_item:
                Toast.makeText(ViewProfileActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    public void onClick(View view) {
        int viewID = view.getId();

        if(viewID == R.id.updateProfileButton) {
            //open the update profile activity and pass in the userID
            Intent updateProfileIntent = new Intent(this, EditProfileActivity.class);

            //get current user id
            Intent parentIntent = getIntent();
            int savedUserID = parentIntent.getIntExtra("userID", 0);

            if(savedUserID != 0) {
                updateProfileIntent.putExtra("userID", savedUserID);
                startActivity(updateProfileIntent);
            }
            else {
                Toast.makeText(ViewProfileActivity.this, "No user ID found", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //class to get user info from the server
    private class GetUserTask extends UrlJsonAsyncTask {
        //constructor
        public GetUserTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            JSONObject json = new JSONObject();
            JSONObject userJson;
            JSONObject profileJson;

            //define the URL
            Intent parentIntent = getIntent();

            //check if the username was passed
            int savedUserID = parentIntent.getIntExtra("userID", 0);

            if(savedUserID == 0) {
                return null;
            }
            else {
                try {
                    //generate the URL
                    String specificUserURL = USER_URL + savedUserID;

                    URL url = new URL(specificUserURL);

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
                    json.put("userJson", userJson);

                    //get the profile for this user from the server
                    String profileURLString = PROFILE_URL + savedUserID;
                    URL profileUrl = new URL(profileURLString);

                    urlConnection = (HttpURLConnection) profileUrl.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    in = new BufferedInputStream(urlConnection.getInputStream());

                    reader = new BufferedReader(new InputStreamReader(in));

                    buffer = new StringBuffer();

                    line = "";

                    while((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    String profileResponse = buffer.toString();
                    profileJson = new JSONObject(profileResponse);

                    //add it to json object to be returned
                    json.put("profileJson", profileJson);

                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                //get the user json object and extract values
                JSONObject userJson = json.getJSONObject("userJson");

                String username = userJson.getString(USERNAME);
                String email = userJson.getString(EMAIL);

                //get the profile json object and extract values
                JSONObject profileJson = json.getJSONObject("profileJson");

                String about = profileJson.getString(ABOUT);
                String website = profileJson.getString(WEBSITE);

                //update the views with these new values
                usernameView.setText(username);
                emailView.setText(email);
                aboutView.setText(about);
                websiteView.setText(website);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            super.onPostExecute(json);
        }

    }


}
