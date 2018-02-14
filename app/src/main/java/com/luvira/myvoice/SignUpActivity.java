package com.luvira.myvoice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.luvira.myvoice.com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private static final String SIGN_UP_API_URL = "http://10.0.2.2:3000/api/users/";
    private static final String RESPONSE_HEADER = "X-Android-Response-Source";
    private static final String RESPONSE_OK = "NETWORK 201";
    private static final String RESPONSE_BAD = "NETWORK 422";

    private String username;
    private String email;
    private String password;
    private String passwordConfirmation;

    //constants
    final String USER_NAME = "user_name";
    final String USER_ID = "id";
    final String EMAIL = "email";
    final String CREATED_AT = "created_at";
    final String UPDATED_AT = "updated_at";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void onClick(View view) {
        int viewId = view.getId();

        //check if the sign up button was clicked
        if(viewId == R.id.signButton) {
            //check the fields
            EditText usernameEdit = (EditText) findViewById(R.id.usernameFormEdit);
            username = usernameEdit.getText().toString();

            EditText emailEdit = (EditText) findViewById(R.id.emailFormEdit);
            email = emailEdit.getText().toString();

            EditText passwordEdit = (EditText) findViewById(R.id.passwordFormEdit);
            password = passwordEdit.getText().toString();

            EditText passwordConfirmEdit = (EditText) findViewById(R.id.passwordConfirmEdit);
            passwordConfirmation = passwordConfirmEdit.getText().toString();

            if(verifyData()) {
                //call signuptask
                SignUpTask signUpTask = new SignUpTask(SignUpActivity.this);
                signUpTask.setMessageLoading("Signing Up");
                signUpTask.execute();
            }

        }
    }

    private boolean verifyData() {
        boolean success = false;
        //check if all fields are non_blank
        if(!username.isEmpty() && !email.isEmpty() && !password.isEmpty()
                && !passwordConfirmation.isEmpty()) {
            if(email.contains("@")) {
                //check if password and confirmation are similar
                if(password.equals(passwordConfirmation)) {
                    success = true;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Your passwords are not similar", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Please check your email", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "Ensure all fields are filled", Toast.LENGTH_SHORT).show();
            success = false;
        }
        return success;
    }

    private class SignUpTask extends UrlJsonAsyncTask {
        //constructor
        public SignUpTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            JSONObject json = new JSONObject();
            try {

                URL url = new URL(SIGN_UP_API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                //write the email and password to stream

                //OutputStreamWriter writer = new OutputStreamWriter(out);
                String data = "{\"user\": {\"email\": \"" + email + "\"," +
                        " \"password\": \"" + password + "\", \"password_confirmation\": \"" + passwordConfirmation + "\"," +
                        " \"user_name\": \"" + username + "\"}}";
                wr.writeBytes(data);
                wr.flush();
                wr.close();
                //writer.append("{\"user\": {\"email\": \"jack@gmail.com\", \"password\": \"jack123\", \"password_confirmation\": \"jack123\", \"user_name\": \"jack\"}}");
                //writer.flush();
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

                String responseValue = urlConnection.getHeaderField(RESPONSE_HEADER);
                if (responseValue == RESPONSE_OK) {
                    System.out.println("response values are equal");
                }
                else {
                    System.out.println("response values are not equal");
                }
                System.out.println("response: " + responseValue);

                if(responseValue.equals(RESPONSE_OK)) {
                    json.put("success", true);

                    //get the values from buffer and add to json object
                    JSONObject userJson = new JSONObject(response);

                    int userID = userJson.getInt(USER_ID);
                    String username = userJson.getString(USER_NAME);
                    String email = userJson.getString(EMAIL);
                    String createdAt = userJson.getString(CREATED_AT);
                    String updatedAt = userJson.getString(UPDATED_AT);

                    json.put("userID", userID);
                    json.put("username", username);
                    json.put("email", email);
                    json.put("createdAt", createdAt);
                    json.put("updatedAt", updatedAt);
                }
                else {
                    json.put("info", RESPONSE_BAD);

                }
                //print the header
                for (int i = 0;;i++) {
                    String headerKey = urlConnection.getHeaderFieldKey(i);
                    String headerValue = urlConnection.getHeaderField(i);

                    System.out.println("Name: " + headerKey + "\nValue: " + headerValue + "\n");

                    if(headerKey == null && headerValue == null) {
                        break;
                    }
                }

                System.out.println(response);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if(json.getBoolean("success")) {
                    SharedPreferences mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putInt("userID",json.getInt("userID"));
                    editor.commit();

                    //open the main Activity
                    Intent intent = new Intent(getApplicationContext(), ViewProfileActivity.class);
                    intent.putExtra("userID", json.getInt("userID"));
                    intent.putExtra("username", json.getString("username"));
                    intent.putExtra("isFromSignUp", true);

                    //Toast.makeText(getApplicationContext(), "You have signed up", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                super.onPostExecute(json);
            }
            Toast.makeText(getApplicationContext(), "I am done executing", Toast.LENGTH_SHORT).show();
        }
    }
}
