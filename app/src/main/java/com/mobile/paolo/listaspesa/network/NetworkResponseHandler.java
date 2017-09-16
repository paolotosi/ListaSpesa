package com.mobile.paolo.listaspesa.network;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * This interface handles the HTTP string request received from the server.
 * In order to use this, a class must overwrite onSuccess and onError methods
 * with the desired behaviour.
 * In this way it's possible to separate the request logic from the request handling logic.
 */

public interface NetworkResponseHandler
{
    
    void onSuccess(JSONObject response);

    void onError(VolleyError error);
}
