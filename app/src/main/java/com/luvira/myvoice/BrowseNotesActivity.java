package com.luvira.myvoice;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.luvira.myvoice.com.savagelook.android.UrlJsonAsyncTask;
import com.luvira.myvoice.com.savagelook.android.adapter.MyPagerAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrowseNotesActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_notes);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getIntent().getStringExtra("category") + " Notes");

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

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

    public void onClick(View view) {
        int viewID = view.getId();

        if(viewID == R.id.beFirstNewButton || viewID == R.id.beFirstPopularButton
                || viewID == R.id.beFirstTrendingButton) {
            //open activity to add a new note
            Intent newNoteIntent = new Intent(this, NewNoteActivity.class);
            newNoteIntent.putExtra("cat", getIntent().getStringExtra("category"));
            startActivity(newNoteIntent);
            finish();
        }

    }
}
