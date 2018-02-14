package com.luvira.myvoice;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

public class NewNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //necessary constants
    private static final String NEW_NOTE_API_URL = "http://10.0.2.2:3000/api/notes";
    static final String[] categoryItems = {"Social", "Technology", "Political", "Gender",
            "Religion", "Art", "History", "Vanity"};

    //define the fields
    EditText titleEdit;
    EditText contentEdit;
    Spinner spin;

    private String title;
    private String content;
    private String category;
    private int categoryID;
    private int userID;
    private boolean isCategorySelected = false;

    //constants
    final String NOTE_ID = "id";
    final String NOTE_TITLE = "title";
    final String NOTE_CONTENT = "content";
    final String CREATED_AT = "created_at";
    final String UPDATED_AT = "updated_at";
    final String NOTE_OWNER = "user_id";
    final String NOTE_CATEGORY = "category_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        //initialize the fields
        titleEdit = (EditText) findViewById(R.id.titleEdit);
        contentEdit = (EditText) findViewById(R.id.contentEdit);
        spin = (Spinner) findViewById(R.id.categorySpinner);
        spin.setOnItemSelectedListener(this);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categoryItems);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(myAdapter);

        //set id of current user
        SharedPreferences mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        userID = mPreferences.getInt("userID", 0);

        //check if from category tabs
        String defaultCategory = getIntent().getStringExtra("cat");
        if(defaultCategory != null && !defaultCategory.isEmpty()) {
            //loop category array and get pos of required string
            int pos = 0;
            for (int looper = 0; looper < categoryItems.length; looper++) {
                if(defaultCategory.equals(categoryItems[looper])) {
                    pos = looper;
                }
            }
            spin.setSelection(pos);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = categoryItems[position];
        categoryID = position + 1;
        isCategorySelected = true;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        isCategorySelected = false;
    }

    //handle clicking of the save button
    public void onClick(View view) {
        int viewID = view.getId();

        if(viewID == R.id.saveNoteButton) {
            //verify fields have data
            title = titleEdit.getText().toString();
            content = contentEdit.getText().toString();

            if(!title.isEmpty() && !content.isEmpty()) {
                if (isCategorySelected) {
                    if (userID != 0) {
                        //save the note
                        NewNoteTask newNoteTask = new NewNoteTask(NewNoteActivity.this);
                        newNoteTask.setMessageLoading("Saving your note");
                        newNoteTask.execute();
                    }
                    else {
                        new AlertDialog.Builder(this)
                                .setTitle("Unknown User")
                                .setMessage("This is strange. It appears you aren't logged in. Sorry.")
                                                .setPositiveButton("Show Toast", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                }).show();
                    }
                }
                else {
                    Toast.makeText(NewNoteActivity.this, "Ensure you have selected a category", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(NewNoteActivity.this, "Ensure all fields are filled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class NewNoteTask extends UrlJsonAsyncTask {

        //constructor
        public NewNoteTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            JSONObject noteJson = null;

            try {

                URL url = new URL(NEW_NOTE_API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                //write the email and password to stream

                //OutputStreamWriter writer = new OutputStreamWriter(out);
                String data = "{\"title\": \"" + title + "\"," +
                        " \"content\": \"" + content + "\"," +
                        " \"user_id\": " + userID + ", \"category_id\": " + categoryID + "}";
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

                noteJson = new JSONObject(response);

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
            return noteJson;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                int noteID = json.getInt(NOTE_ID);
                String noteTitle = json.getString(NOTE_TITLE);
                String noteContent = json.getString(NOTE_CONTENT);
                String noteCreatedAt = json.getString(CREATED_AT);
                int noteOwner = json.getInt(NOTE_OWNER);
                String noteCategory = category;

                //add the strings to the intent and start view note activity
                Intent intent = new Intent(getApplicationContext(), ViewNoteActivity.class);
                intent.putExtra("noteID", noteID);
                intent.putExtra("noteTitle", noteTitle);
                intent.putExtra("noteContent", noteContent);
                intent.putExtra("noteCreatedAt", noteCreatedAt);
                intent.putExtra("noteOwner", noteOwner);
                intent.putExtra("noteCategory", noteCategory);

                intent.putExtra("isFromNew", true);

                //start the activity
                startActivity(intent);
                finish();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(json);
        }
    }
}
