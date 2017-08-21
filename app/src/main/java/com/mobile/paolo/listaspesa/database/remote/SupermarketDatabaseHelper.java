package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 21/08/17.
 */

public class SupermarketDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();

    // The URLs.
    private static final String URL_GET_ALL_SUPERMARKETS = "http://" + host + "/listaspesa/android_connect/supermarkets/get_all_supermarkets.php";

    public static void sendGetAllSupermarketsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_ALL_SUPERMARKETS, jsonPostData, context, networkResponseHandler);
    }
}
