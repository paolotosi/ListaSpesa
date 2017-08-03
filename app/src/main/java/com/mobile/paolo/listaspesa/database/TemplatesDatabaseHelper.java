package com.mobile.paolo.listaspesa.database;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 03/08/17.
 */

public class TemplatesDatabaseHelper
{
    // The URLs.
    private static final String URL_GET_TEMPLATES = "http://10.0.2.2/listaspesa/android_connect/templates/get_group_templates.php";

    public static void sendGetGroupTemplatesRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_TEMPLATES, jsonPostData, context, networkResponseHandler);
    }
}
