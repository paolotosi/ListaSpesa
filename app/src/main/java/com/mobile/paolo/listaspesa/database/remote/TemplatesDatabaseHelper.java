package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 03/08/17.
 */

/**
 * This class handles database queries regarding templates.
 * It simply asks the NetworkMessageSender class to perform
 * HTTP requests specifying which URLs to connect to.
 */

public class TemplatesDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();

    // The URLs.
    private static final String URL_GET_TEMPLATES = "http://" + host + "/listaspesa/android_connect/templates/get_group_templates.php";
    private static final String URL_CREATE_TEMPLATE = "http://" + host + "/listaspesa/android_connect/templates/create_template.php";
    private static final String URL_DELETE_TEMPLATES = "http://" + host + "/listaspesa/android_connect/templates/delete_templates.php";
    private static final String URL_UPDATE_TEMPLATE_DETAILS = "http://" + host + "/listaspesa/android_connect/templates/update_template_details.php";

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
