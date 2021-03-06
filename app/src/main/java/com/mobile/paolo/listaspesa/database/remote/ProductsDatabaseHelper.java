package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 07/08/17.
 */

/**
 * This class handles database queries regarding products.
 * It simply asks the NetworkMessageSender class to perform
 * HTTP requests specifying which URLs to connect to.
 */

public class ProductsDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();

    // The URLs.
    private static final String URL_GET_PRODUCTS = "http://" + host + "/listaspesa/android_connect/products/get_all_products.php";
    private static final String URL_GET_PRODUCTS_NOT_FOUND = "http://" + host + "/listaspesa/android_connect/products/get_products_not_found.php";


    // DB interaction methods: sending at a specific URL, some data, response is handled by networkResponseHandler
    public static void sendGetAllProductsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_PRODUCTS, jsonPostData, context, networkResponseHandler);
    }

    public static void sendGetProductsNotFoundRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_PRODUCTS_NOT_FOUND, jsonPostData, context, networkResponseHandler);
    }


}
