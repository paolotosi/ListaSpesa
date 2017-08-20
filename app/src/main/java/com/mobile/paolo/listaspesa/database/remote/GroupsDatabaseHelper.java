package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 08/07/17.
 */

public class GroupsDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();

    // The URLs.
    private static final String URL_CREATE_GROUP = "http://" + host + "/listaspesa/android_connect/groups/create_group.php";
    private static final String URL_GET_GROUP_DETAILS = "http://" + host + "/listaspesa/android_connect/groups/get_user_group_details.php";
    private static final String URL_MODIFY_GROUP_NAME = "http://" + host + "listaspesa/android_connect/groups/modify_group_name.php";
    private static final String URL_UPDATE_GROUP = "http://" + host + "/listaspesa/android_connect/groups/update_group_members.php";

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

    public static void sendUpdateGroupRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_UPDATE_GROUP, jsonPostData, context, networkResponseHandler);
    }



}
