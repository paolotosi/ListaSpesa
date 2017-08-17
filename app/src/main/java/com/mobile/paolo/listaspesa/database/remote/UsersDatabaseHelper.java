package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkQueueManager;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * This class handles database queries regarding users.
 * It simply asks the NetworkMessageSender class to perform
 * HTTP requests specifying which URLs to connect to.
 */

public class UsersDatabaseHelper
{
    // The URLs.
    private static final String URL_GET_ALL_USERS = "http://10.0.2.2/listaspesa/android_connect/users/get_all_users.php";
    private static final String URL_LOGIN = "http://10.0.2.2/listaspesa/android_connect/users/login.php";
    private static final String URL_ADD_USER = "http://10.0.2.2/listaspesa/android_connect/users/add_user.php";

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
