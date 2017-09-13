package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 21/08/17.
 */

/**
 * This class handles database queries regarding supermarkets.
 * It simply asks the NetworkMessageSender class to perform
 * HTTP requests specifying which URLs to connect to.
 */

public class SupermarketDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();

    // The URLs.
    private static final String URL_GET_ALL_SUPERMARKETS = "http://" + host + "/listaspesa/android_connect/supermarkets/get_all_supermarkets.php";
    private static final String URL_SAVE_SUPERMARKET_PRODUCTS = "http://" + host + "/listaspesa/android_connect/supermarkets/save_supermarket_products.php";
    private static final String URL_ADD_SUPERMARKET = "http://" + host + "/listaspesa/android_connect/supermarkets/add_supermarket.php";
    private static final String URL_DELETE_SUPERMARKET = "http://" + host + "/listaspesa/android_connect/supermarkets/delete_supermarket.php";

    // DB interaction methods: sending at a specific URL, some data, response is handled by networkResponseHandler
    public static void sendGetAllSupermarketsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_ALL_SUPERMARKETS, jsonPostData, context, networkResponseHandler);
    }

    public static void sendSaveSupermarketProductsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_SAVE_SUPERMARKET_PRODUCTS, jsonPostData, context, networkResponseHandler);
    }

    public static void sendAddSupermarketRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_ADD_SUPERMARKET, jsonPostData, context, networkResponseHandler);
    }

    public static void sendDeleteSupermarketRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_DELETE_SUPERMARKET, jsonPostData, context, networkResponseHandler);
    }


}
