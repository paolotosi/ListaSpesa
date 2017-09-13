package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * This class handles database queries regarding users.
 * It simply asks the NetworkMessageSender class to perform
 * HTTP requests specifying which URLs to connect to.
 */

public class UsersDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();
    
    // The URLs.
    private static final String URL_GET_ALL_USERS = "http://" + host + "/listaspesa/android_connect/users/get_all_users.php";
    private static final String URL_LOGIN = "http://" + host + "/listaspesa/android_connect/users/login.php";
    private static final String URL_ADD_USER = "http://" + host + "/listaspesa/android_connect/users/add_user.php";
    
    // Retrieve all users.
    public static void sendGetAllUsersRequest(Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_ALL_USERS, null, context, networkResponseHandler);
    }
    
    // Check if a username/password pair is valid.
    public static void sendLoginRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_LOGIN, jsonPostData, context, networkResponseHandler);
    }
    
    // Add a new user to the database.
    public static void sendRegistrationRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_ADD_USER, jsonPostData, context, networkResponseHandler);
    }
}
