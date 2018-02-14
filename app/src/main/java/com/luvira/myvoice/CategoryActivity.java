package com.luvira.myvoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class CategoryActivity extends AppCompatActivity {

    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.category_menu, menu);

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_note_item:
                Intent intent = new Intent(this, NewNoteActivity.class);
                startActivity(intent);
                return true;
            case R.id.my_profile_item:
                int savedUserID = mPreferences.getInt("userID", 0);
                if(savedUserID == 0) {
                    Toast.makeText(CategoryActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent profileIntent = new Intent(this, ViewProfileActivity.class);
                    profileIntent.putExtra("userID", savedUserID);
                    startActivity(profileIntent);
                }
                return true;
            case R.id.settings_item:
                Toast.makeText(CategoryActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.help_item:
                Toast.makeText(CategoryActivity.this, "Help", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    //deal with which card has been clicked
    public void onClick(View view) {
        int viewID = view.getId();

        Intent intent = new Intent(CategoryActivity.this, BrowseNotesActivity.class);

        switch (viewID) {
            case R.id.socialCard:
                intent.putExtra("category", "Social");
                break;
            case R.id.techCard:
                intent.putExtra("category", "Technology");
                break;
            case R.id.politicalCard:
                intent.putExtra("category", "Political");
                break;
            case R.id.genderCard:
                intent.putExtra("category", "Gender");
                break;
            case R.id.religionCard:
                intent.putExtra("category", "Religion");
                break;
            case R.id.artCard:
                intent.putExtra("category", "Art");
                break;
            case R.id.historyCard:
                intent.putExtra("category", "History");
                break;
            case R.id.vanityCard:
                intent.putExtra("category", "Vanity");
                break;
        }

        //start the activity
        startActivity(intent);
    }
}
