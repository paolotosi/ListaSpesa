package com.mobile.paolo.listaspesa.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * This class manages network requests using a queue handled by Google's Volley library.
 * Volley handles the request asynchronously, so we don't have to use AsyncTask mechanisms.
 * Since we'll be using the same queue throughout the application, it is implemented
 * using the Singleton pattern.
 */

public class NetworkQueueManager
{
    private static NetworkQueueManager instance;
    private RequestQueue requestQueue;
    private static Context context;
    
    private NetworkQueueManager(Context context)
    {
        NetworkQueueManager.context = context;
        requestQueue = getRequestQueue();
    }
    
    public static synchronized NetworkQueueManager getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new NetworkQueueManager(context);
        }
        return instance;
    }
    
    public RequestQueue getRequestQueue()
    {
        if (requestQueue == null)
        {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }
    
    public <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }
    
}
