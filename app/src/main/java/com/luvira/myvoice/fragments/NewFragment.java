package com.luvira.myvoice.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.luvira.myvoice.NewNoteActivity;
import com.luvira.myvoice.R;
import com.luvira.myvoice.ViewNoteActivity;
import com.luvira.myvoice.com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewFragment extends Fragment implements AdapterView.OnItemClickListener{

    //declare constants
    private static final String ALL_NOTES_URL = "http://10.0.2.2:3000/api/notes";
    private final String NOTE_ID = "id";
    private final String NOTE_TITLE = "title";
    private final String NOTE_CONTENT = "content";
    private final String NOTE_OWNER_ID = "user_id";

    private List<Map<String, String>> notesList;
    private List<String> fullNotesList;
    private ArrayList<Integer> noteOwnerIDList;
    private ArrayList<Integer> noteIDList;

    ListView browseNotesList;
    RelativeLayout emptyLayout;

    public NewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        browseNotesList = (ListView) view.findViewById(R.id.browseNotesList);
        emptyLayout = (RelativeLayout) view.findViewById(R.id.emptyNewLayout);

        //list to hold the maps
        notesList = new ArrayList();
        fullNotesList = new ArrayList<>();
        noteOwnerIDList = new ArrayList<>();
        noteIDList = new ArrayList<>();


        //call NotesTask to pull the notes data from server
        NotesTask notesTask = new NotesTask(getActivity());
        notesTask.execute();

        refreshList();

        //browseNotesList.setOnItemSelectedListener(this);
        browseNotesList.setOnItemClickListener(this);


        return view;
    }

    public void refreshList() {
        SimpleAdapter newAdapter = new SimpleAdapter(getActivity(), notesList,
                R.layout.browse_notes_item,
                new String[]{NOTE_TITLE, NOTE_CONTENT},
                new int[]{R.id.browse_note_title, R.id.browse_note_content});

        browseNotesList.setEmptyView(emptyLayout);

        browseNotesList.setAdapter(newAdapter);
    }

    public void onClick(View view) {
        int viewID = view.getId();

        if(viewID == R.id.beFirstNewButton) {
            //open activity to add a new note
            Intent newNoteIntent = new Intent(getActivity(), NewNoteActivity.class);
            newNoteIntent.putExtra("cat", getActivity().getIntent().getStringExtra("category"));
            startActivity(newNoteIntent);
            getActivity().finish();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView noteTitle = (TextView) view.findViewById(R.id.browse_note_title);

        //open view note activity
        Intent noteIntent = new Intent(getActivity(), ViewNoteActivity.class);

        String title = notesList.get(position).get(NOTE_TITLE);
        String content = fullNotesList.get(position);
        int userID = noteOwnerIDList.get(position);
        int noteID = noteIDList.get(position);

        System.out.println("The user id is: " + userID);
        Intent parentIntent = getActivity().getIntent();
        String category = parentIntent.getStringExtra("category");

        //add data to intent
        noteIntent.putExtra("noteID", noteID);
        noteIntent.putExtra("noteTitle", title);
        noteIntent.putExtra("noteContent", content);
        noteIntent.putExtra("noteOwner", userID);
        noteIntent.putExtra("noteCategory", category);

        startActivity(noteIntent); //start the activity
    }

    private class NotesTask extends UrlJsonAsyncTask {
        //constructor
        public NotesTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            JSONObject notesJson = null;
            try {
                URL url;
                //String specificUserUrl = ALL_NOTES_URL + intent.getStringExtra("noteOwner");
                Intent parentIntent = getActivity().getIntent();

                //check if category was selected
                String chosenCategory = parentIntent.getStringExtra("category");

                if(chosenCategory.isEmpty()) {
                    url = new URL(ALL_NOTES_URL);
                }
                else {
                    String specificCategoryUrl = ALL_NOTES_URL + "?category=" + chosenCategory;
                    url = new URL(specificCategoryUrl);
                }


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
                notesJson = new JSONObject(response);

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return notesJson;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray notesArray = json.getJSONArray("notes");

                for (int looper = 0; looper < notesArray.length(); looper++) {
                    //create a hashmap, add the data to it and add it to the list

                    JSONObject note = notesArray.getJSONObject(looper);

                    Map<String, String> tempMap = new HashMap<String, String>();

                    tempMap.put(NOTE_TITLE, note.getString(NOTE_TITLE));

                    String noteContent = note.getString(NOTE_CONTENT);

                    if(noteContent.length() > 200) {
                        tempMap.put(NOTE_CONTENT, noteContent.substring(0, 200) + "...");
                    }
                    else {
                        tempMap.put(NOTE_CONTENT, noteContent);
                    }

                    fullNotesList.add(noteContent);

                    notesList.add(tempMap);

                    //add the user id of note to list
                    int noteOwnerID = note.getInt(NOTE_OWNER_ID);
                    noteOwnerIDList.add(noteOwnerID);

                    //add note id to list
                    int noteID = note.getInt(NOTE_ID);
                    noteIDList.add(noteID);
                }
                refreshList();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            super.onPostExecute(json);
        }
    }

}
