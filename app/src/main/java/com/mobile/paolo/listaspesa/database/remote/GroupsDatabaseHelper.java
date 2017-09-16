package com.mobile.paolo.listaspesa.database.remote;

import android.content.Context;

import com.mobile.paolo.listaspesa.network.NetworkMessageSender;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;

/**
 * Created by paolo on 08/07/17.
 */

/**
 * This class handles database queries regarding groups.
 * It simply asks the NetworkMessageSender class to perform
 * HTTP requests specifying which URLs to connect to.
 */

public class GroupsDatabaseHelper
{
    private static String host = RemoteDatabaseHelper.getInstance().getHost();

    // The URLs.
    private static final String URL_CREATE_GROUP = "http://" + host + "/listaspesa/android_connect/groups/create_group.php";
    private static final String URL_GET_GROUP_DETAILS = "http://" + host + "/listaspesa/android_connect/groups/get_user_group_details.php";
    private static final String URL_MODIFY_GROUP_NAME = "http://" + host + "/listaspesa/android_connect/groups/modify_group_name.php";
    private static final String URL_UPDATE_GROUP = "http://" + host + "/listaspesa/android_connect/groups/update_group_members.php";
    private static final String URL_MULTIPLE_GROUPS = "http://" + host + "/listaspesa/android_connect/groups/check_multiple_groups.php";
    private static final String URL_UPDATE_PRODUCT_TABLE = "http://" + host + "/listaspesa/android_connect/groups/update_product_table.php";
    private static final String URL_DELETE_PRODUCT = "http://" + host + "/listaspesa/android_connect/groups/delete_product.php";
    private static final String URL_GROUP_PRODUCTS = "http://" + host + "/listaspesa/android_connect/groups/get_group_products.php";


    // DB interaction methods: sending at a specific URL, some data, response is handled by networkResponseHandler
    public static void sendCreateGroupRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_CREATE_GROUP, jsonPostData, context, networkResponseHandler);
    }

    public static void sendGetGroupDetailsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GET_GROUP_DETAILS, jsonPostData, context, networkResponseHandler);
    }

    public static void sendModifyGroupNameRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_MODIFY_GROUP_NAME, jsonPostData, context, networkResponseHandler);
    }

    public static void sendUpdateGroupRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_UPDATE_GROUP, jsonPostData, context, networkResponseHandler);
    }

    public static void sendMultipleGroupsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_MULTIPLE_GROUPS, jsonPostData, context, networkResponseHandler);
    }

    public static void updateProductTable(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_UPDATE_PRODUCT_TABLE, jsonPostData, context, networkResponseHandler);
    }

    public static void sendDeleteProductRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_DELETE_PRODUCT, jsonPostData, context, networkResponseHandler);
    }

    public static void sendGetGroupProductsRequest(JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandler)
    {
        NetworkMessageSender.sendHTTPRequest(URL_GROUP_PRODUCTS, jsonPostData, context, networkResponseHandler);
    }


}
