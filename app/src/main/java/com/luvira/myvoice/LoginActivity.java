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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    //necessary variables
    private final static String LOGIN_API_URL = "http://10.0.2.2:3000/api/sessions";
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClick(View view) {
        //get the view id
        int viewId = view.getId();

        if(viewId == R.id.signUpButton) {
            //open the sign up activity
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);

            finish(); //close this activity
        }
        else if(viewId == R.id.loginButton) {
            //check entered credentials and call logintask
            EditText emailField = (EditText) findViewById(R.id.emailEdit);
            email = emailField.getText().toString();

            EditText passwordField = (EditText) findViewById(R.id.passwordEdit);
            password = passwordField.getText().toString();

            //check if fields are empty
            if(email.length() == 0 || password.length() == 0) {
                Toast.makeText(LoginActivity.this, "Please complete all fields",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(this, CategoryActivity.class);
                startActivity(intent);
                finish();
//                LoginTask loginTask = new LoginTask(LoginActivity.this);
//                loginTask.setMessageLoading("Loggin in...");
//                loginTask.execute();
            }
            //open the sign up activity
//            Intent intent = new Intent(this, CategoryActivity.class);
//            startActivity(intent);
//
//            finish(); //close this activity
        }
    }

    private class LoginTask extends UrlJsonAsyncTask {

        //constructor
        public LoginTask(Context context) {
            super(context);
        }
        @Override
        protected JSONObject doInBackground(String... urls) {
            try {

                URL url = new URL(LOGIN_API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                //write the email and password to stream

                //OutputStreamWriter writer = new OutputStreamWriter(out);
                String data = "{\"email\": \"jack5@gmail.com\", \"password\": \"jack123\"}";
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

                //print the header
                for (int i = 0;;i++) {
                    String headerKey = urlConnection.getHeaderFieldKey(i);
                    String headerValue = urlConnection.getHeaderField(i);

                    System.out.println("Name: " + headerKey + "\nValue: " + headerValue + "\n");

                    if(headerKey == null && headerValue == null) {
                        break;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            Toast.makeText(getApplicationContext(), "I am done executing", Toast.LENGTH_SHORT).show();

            super.onPostExecute(json);
        }
    }
}
