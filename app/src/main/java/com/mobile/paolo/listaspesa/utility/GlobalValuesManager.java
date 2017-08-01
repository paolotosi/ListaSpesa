package com.mobile.paolo.listaspesa.utility;

import android.content.Context;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.Group;
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
        sharedPreferencesManager.writeString(context.getResources().getString(R.string.logged_user), loggedUser.toJSON().toString());
    }

    public User getLoggedUser()
    {
        String jsonLoggedUser = sharedPreferencesManager.readString(context.getResources().getString(R.string.logged_user));
        User loggedUser = null;
        try {
            loggedUser = new User(new JSONObject(jsonLoggedUser));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loggedUser;
    }

    public void saveIsUserPartOfAGroup(boolean isUserPartOfAGroup)
    {
        sharedPreferencesManager.writeBoolean(context.getResources().getString(R.string.is_user_part_of_a_group), isUserPartOfAGroup);
    }

    public boolean isUserPartOfAGroup()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.is_user_part_of_a_group));
    }

    public void saveLoggedUserGroup(Group group)
    {
        sharedPreferencesManager.writeString(context.getResources().getString(R.string.logged_user_group), group.toJSON().toString());
    }

    public Group getLoggedUserGroup()
    {
        String jsonLoggedUserGroup = sharedPreferencesManager.readString(context.getResources().getString(R.string.logged_user_group));
        Group loggedUserGroup = null;
        try {
            loggedUserGroup = Group.fromJSON(new JSONObject(jsonLoggedUserGroup));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loggedUserGroup;
    }

}
