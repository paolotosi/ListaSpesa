/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.mobile.paolo.listaspesa;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mobile.paolo.listaspesa.network.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CreateGroupFragment extends Fragment {
    public static CreateGroupFragment newInstance() {
        CreateGroupFragment fragment = new CreateGroupFragment();
        return fragment;
    }

    private ListView listView;
    private ArrayList<String> userList = new ArrayList<>();
    private static final String url_login = "http://10.0.2.2/listaspesa/android_connect/users/get_all_users.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);
        bindUserList(view);
        sendHTTPRequest(view);

        return view;
    }

    private void bindUserList(View fragment)
    {
        listView = (ListView) fragment.findViewById(R.id.userList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, userList);
        listView.setAdapter(adapter);

    }

    private void sendHTTPRequest(View view)
    {
        // Get the RequestQueue from NetworkManager
        RequestQueue queue = NetworkManager.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_login,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse JSON response and check if the login was successful
                        Log.d("RESPONSE_MSG", response);
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray dataJsonArray = json.getJSONArray("users");
                            for(int i = 0; i < dataJsonArray.length(); i++)
                            {
                                JSONObject dataObj = (JSONObject) dataJsonArray.get(i);
                                Log.d("JSON", dataObj.getString("nome"));
                                userList.add(dataObj.getString("nome"));
                                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }

                });
        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }
}
