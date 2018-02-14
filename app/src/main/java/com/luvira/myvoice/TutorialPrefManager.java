package com.luvira.myvoice;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Zephaniah on 6/28/2016.
 */
public class TutorialPrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "bonga-prefs";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public TutorialPrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

}

