package com.mobile.paolo.listaspesa.network;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * This class has only a method, which is used to send generic HTTP requests.
 * It doesn't (and doesn't have to) know how the HTTP response will be handled.
 * That is up to the specific networkResponseHandlerImplementation received from the outside.
 */

public class NetworkMessageSender
{
    public static void sendHTTPRequest(String url, JSONObject jsonPostData, Context context, NetworkResponseHandler networkResponseHandlerImplementation)
    {
        final NetworkResponseHandler networkResponseHandler = networkResponseHandlerImplementation;

        /*
            Request a JSON response from the provided URL.
            If jsonPostData is null, it'll be sent a GET request.
            Otherwise, it'll be sent a POST request with the parameters specified in jsonPostData.
         */
        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, jsonPostData,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        networkResponseHandler.onSuccess(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        networkResponseHandler.onError(error);
                    }
                    
                });
        
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        
        // Add the request to the RequestQueue.
        NetworkQueueManager.getInstance(context).addToRequestQueue(jsonRequest);
    }
}
