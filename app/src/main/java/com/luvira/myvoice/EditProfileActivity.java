package com.luvira.myvoice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.luvira.myvoice.com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditProfileActivity extends AppCompatActivity {

    private static final String EDIT_PROFILE_URL = "http://10.0.2.2:3000/api/profiles/";

    //declare the views
    EditText websiteEdit;
    EditText aboutEdit;

    String website;
    String about;
    int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //initialize variables
        websiteEdit = (EditText) findViewById(R.id.websiteEdit);
        aboutEdit = (EditText) findViewById(R.id.aboutMeEdit);

    }

    public void onClick(View view) {
        int viewID = view.getId();

        //check if its the save button n perform action
        if(viewID == R.id.saveProfileButton) {
            website = websiteEdit.getText().toString();
            about = aboutEdit.getText().toString();

            if(!website.isEmpty() && !about.isEmpty()) {
                //call task to update the profile
                Intent parentIntent = getIntent();

                userID = parentIntent.getIntExtra("userID", 0);

                if(userID != 0) {
                    PatchProfileTask profileTask = new PatchProfileTask(this);
                    profileTask.execute();
                }
                else {
                    Toast.makeText(EditProfileActivity.this, "No user was found", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(EditProfileActivity.this, "Ensure all fields are filled", Toast.LENGTH_SHORT).show();
            }
        }
        else if(viewID == R.id.uploadPhotoButton) {
            Toast.makeText(EditProfileActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
        }
    }

    //task to update user profile on server
    private class PatchProfileTask extends UrlJsonAsyncTask {
        //constructor
        public PatchProfileTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject profileJson = null;
            try {


                URL url = new URL(EDIT_PROFILE_URL + userID);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                String data = "{\"about\": \"" + about + "\"," +
                        " \"website\": \"" + website + "\"}";
                wr.writeBytes(data);
                wr.flush();
                wr.close();

                urlConnection.connect();

                //read the response
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                StringBuffer buffer = new StringBuffer();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;

                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                String response = buffer.toString();

                System.out.println(response);

                profileJson = new JSONObject(response);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return profileJson;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                //get the profile id and open the view profile activity
                int id = json.getInt("user_id");

                Intent profileIntent = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
                profileIntent.putExtra("userID", id);
                profileIntent.putExtra("isFromEdit", true);

                startActivity(profileIntent);
                finish();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(json);
        }
    }
}
