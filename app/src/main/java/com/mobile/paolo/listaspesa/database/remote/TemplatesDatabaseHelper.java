package com.mobile.paolo.listaspesa.database.remote;

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
    private static final String URL_CREATE_TEMPLATE = "http://10.0.2.2/listaspesa/android_connect/templates/create_template.php";
    private static final String URL_DELETE_TEMPLATES = "http://10.0.2.2/listaspesa/android_connect/templates/delete_templates.php";
    private static final String URL_UPDATE_TEMPLATE_DETAILS = "http://10.0.2.2/listaspesa/android_connect/templates/update_template_details.php";

    public static void sendGetGroupTemplatesRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_TEMPLATES, jsonPostData, context, networkResponseHandler);
    }

    public static void sendCreateTemplateRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_CREATE_TEMPLATE, jsonPostData, context, networkResponseHandler);
    }

    public static void sendDeleteTemplatesRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_DELETE_TEMPLATES, jsonPostData, context, networkResponseHandler);
    }

    public static void sendDeleteTemplateProductsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_UPDATE_TEMPLATE_DETAILS, jsonPostData, context, networkResponseHandler);
    }
}
