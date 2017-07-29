package com.mobile.paolo.listaspesa.utility;

import android.content.Context;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class manages the access to a small number of global variables, such ad the logged user.
 */

public class GlobalValuesManager
{
    private static GlobalValuesManager instance;
    private SharedPreferencesManager sharedPreferencesManager;
    private Context context;

    private GlobalValuesManager(Context context)
    {
        this.sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        this.context = context;
    }

    public static synchronized GlobalValuesManager getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new GlobalValuesManager(context);
        }
        return instance;
    }

    public void saveLoggedUser(User loggedUser)
    {
        sharedPreferencesManager.writeString(context.getResources().getString(R.string.LOGGED_USER), loggedUser.toJSON().toString());
    }

    public User getLoggedUser()
    {
        String jsonLoggedUser = sharedPreferencesManager.readString(context.getResources().getString(R.string.LOGGED_USER));
        User loggedUser = null;
        try {
            loggedUser = new User(new JSONObject(jsonLoggedUser));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loggedUser;
    }

}
