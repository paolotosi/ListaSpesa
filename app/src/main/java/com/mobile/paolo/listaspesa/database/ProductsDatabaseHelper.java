package com.mobile.paolo.listaspesa.database;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 07/08/17.
 */

public class ProductsDatabaseHelper
{
    // The URLs.
    private static final String URL_GET_PRODUCTS = "http://10.0.2.2/listaspesa/android_connect/products/get_all_products.php";

    public static void sendGetAllProductsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_PRODUCTS, jsonPostData, context, networkResponseHandler);
    }
}
