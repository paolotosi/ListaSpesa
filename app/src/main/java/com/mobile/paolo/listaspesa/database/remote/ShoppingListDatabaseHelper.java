package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by revoc on 10/08/17.
 */

/**
 * This class handles database queries regarding shopping list.
 * It simply asks the NetworkMessageSender class to perform
 * HTTP requests specifying which URLs to connect to.
 */

public class ShoppingListDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();
    
    // The URLs.
    private static final String URL_GET_LIST = "http://" + host + "/listaspesa/android_connect/shoppingList/get_group_list.php";
    private static final String URL_CREATE_LIST = "http://" + host + "/listaspesa/android_connect/shoppingList/create_shopping_list.php";
    private static final String URL_TAKE_LIST = "http://" + host + "/listaspesa/android_connect/shoppingList/take_shopping_list.php";
    private static final String URL_DELETE_LIST = "http://" + host + "/listaspesa/android_connect/shoppingList/delete_shopping_list.php";
    private static final String URL_COMPLETE_LIST = "http://" + host + "/listaspesa/android_connect/shoppingList/complete_shopping_list.php";
    
    // DB interaction methods: sending at a specific URL, some data, response is handled by networkResponseHandler
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
