package com.chrisventura.apps.noteline.Data;

import android.content.Context;
import android.content.SharedPreferences;

import com.chrisventura.apps.noteline.UI.NotesListFragment;

/**
 * Created by ventu on 26/5/2017.
 */

public class PreferencesHelper {
    Context context;

    public PreferencesHelper(Context context) {
        this.context = context;
    }

    public boolean setStringValue(String key, String value) {
        SharedPreferences preferences = context
                .getSharedPreferences(
                        NotesListFragment.PREFERENCES_CURRENT_CAT, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        if (editor != null) {
            editor.putString(key, value);
            return editor.commit();
        }
        return false;
    }

    public String getStringValue(String key, String defaultValue) {
        SharedPreferences preferences = context
                .getSharedPreferences(NotesListFragment.PREFERENCES_CURRENT_CAT, Context.MODE_PRIVATE);
        return preferences.getString(key, defaultValue);
    }
}
