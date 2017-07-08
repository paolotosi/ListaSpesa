package com.mobile.paolo.listaspesa.database;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 08/07/17.
 */

public class GroupsDatabaseHelper
{
    // The URLs.
    private static final String URL_CREATE_GROUP = "http://10.0.2.2/listaspesa/android_connect/groups/create_group.php";

    public static void sendCreateGroupRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_CREATE_GROUP, jsonPostData, context, networkResponseHandler);
    }



}
