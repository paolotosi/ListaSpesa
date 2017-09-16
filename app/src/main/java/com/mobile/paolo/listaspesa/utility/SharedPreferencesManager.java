package com.mobile.paolo.listaspesa.utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class is used to maintain a centralized access to SharedPreferences.
 * It uses the Singleton pattern as more than one instance isn't needed.
 */

public class SharedPreferencesManager
{
    //Object fields
    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;
    
    //Constant variables
    private static final String PREFS_NAME = "SHARED_PREF";
    private static final String RETRIEVE_ERROR = "ENTRY_NOT_FOUND";
    
    //Private constructor
    private SharedPreferencesManager(Context context)
    {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    //Get sharedPreferencesManager object current instance
    public static synchronized SharedPreferencesManager getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }
    
    //Clean sharedPreferences
    public void flush()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
    
    //Write a string in the shared preferences
    public void writeString(String key, String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    
    // Read a string in the shared preferences
    public String readString(String key)
    {
        return sharedPreferences.getString(key, RETRIEVE_ERROR);
    }
    
    //Write a boolean in the shared preferences
    public void writeBoolean(String key, boolean value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    //Read a boolean in shared preferences
    public boolean readBoolean(String key)
    {
        return sharedPreferences.getBoolean(key, false);
    }
    
}
