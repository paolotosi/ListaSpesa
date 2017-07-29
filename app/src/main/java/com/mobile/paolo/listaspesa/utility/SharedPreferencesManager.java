package com.mobile.paolo.listaspesa.utility;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

/**
 * This class is used to maintain a centralized access to SharedPreferences.
 * It uses the Singleton pattern as more than one instance isn't needed.
 */

public class SharedPreferencesManager
{
    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SHARED_PREF";
    private static final String RETRIEVE_ERROR = "ENTRY_NOT_FOUND";

    private SharedPreferencesManager(Context context)
    {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    public void writeString(String key, String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String readString(String key)
    {
        return sharedPreferences.getString(key, RETRIEVE_ERROR);
    }


}
