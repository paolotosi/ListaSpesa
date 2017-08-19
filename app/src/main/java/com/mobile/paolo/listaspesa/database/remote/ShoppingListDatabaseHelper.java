package com.mobile.paolo.listaspesa.database.remote;

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
    private static final String URL_TAKE_LIST = "http://10.0.2.2/listaspesa/android_connect/shoppingList/take_shopping_list.php";
    private static final String URL_DELETE_LIST = "http://10.0.2.2/listaspesa/android_connect/shoppingList/delete_shopping_list.php";
    private static final String URL_COMPLETE_LIST = "http://10.0.2.2/listaspesa/android_connect/shoppingList/complete_shopping_list.php";

    public static void sendGetGroupListRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_LIST, jsonPostData, context, networkResponseHandler);
    }

    public static void sendCreateShoppingListRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_CREATE_LIST, jsonPostData, context, networkResponseHandler);
    }

    public static void sendTakeShoppingListRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_TAKE_LIST, jsonPostData, context, networkResponseHandler);
    }

    public static void sendDeleteShoppingListRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_DELETE_LIST, jsonPostData, context, networkResponseHandler);
    }

    public static void sendCompleteShoppingListRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_COMPLETE_LIST, jsonPostData, context, networkResponseHandler);
    }


}
