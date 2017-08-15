package com.mobile.paolo.listaspesa.database;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by revoc on 10/08/17.
 */

public class ShoppingListDatabaseHelper
{
    // The URLs.
    private static final String URL_GET_LIST = "http://10.0.2.2/listaspesa/android_connect/shoppingList/get_group_list.php";
    private static final String URL_CREATE_LIST = "http://10.0.2.2/listaspesa/android_connect/shoppingList/create_shopping_list.php";

    public static void sendGetGroupListRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_LIST, jsonPostData, context, networkResponseHandler);
    }

    public static void sendCreateShoppingListRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_CREATE_LIST, jsonPostData, context, networkResponseHandler);
    }
}
