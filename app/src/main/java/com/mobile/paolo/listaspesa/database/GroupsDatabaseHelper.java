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
    private static final String URL_GET_GROUP_DETAILS = "http://10.0.2.2/listaspesa/android_connect/groups/get_user_group_details.php";
    private static final String URL_MODIFY_GROUP_NAME = "http://10.0.2.2/listaspesa/android_connect/groups/modify_group_name.php";

    public static void sendCreateGroupRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_CREATE_GROUP, jsonPostData, context, networkResponseHandler);
    }

    public static void sendGetGroupDetailsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_GROUP_DETAILS, jsonPostData, context, networkResponseHandler);
    }

    public static void sendModifyGroupNameRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_MODIFY_GROUP_NAME, jsonPostData, context, networkResponseHandler);
    }


}
