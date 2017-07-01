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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.database.UsersDatabaseHelper;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CreateGroupFragment extends Fragment
{
    public static CreateGroupFragment newInstance()
    {
        CreateGroupFragment fragment = new CreateGroupFragment();
        return fragment;
    }

    private ListView listView;
    private ArrayList<String> userList = new ArrayList<>();
    private NetworkResponseHandler networkResponseHandler;
    private UsersDatabaseHelper usersDatabaseHelper;
    private static final String url_login = "http://10.0.2.2/listaspesa/android_connect/users/get_all_users.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        setupConfirmButtonListener(view);

        // Tell listView which items to show
        bindUserList(view);

        // Define what to do when the server response is received
        setupNetworkMessageHandler();

        // Send the network request to get all users
        UsersDatabaseHelper.sendGetAllUsersRequest(getActivity().getApplicationContext(), networkResponseHandler);



        return view;
    }

    // Bind the ListView with the list of users
    private void bindUserList(View fragment)
    {
        listView = (ListView) fragment.findViewById(R.id.userList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.user_list_item, R.id.userListItemTextView, userList);
        listView.setAdapter(adapter);

    }

    private void setupNetworkMessageHandler()
    {
        this.networkResponseHandler = new NetworkResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonResponse) {
                Log.d("RESPONSE_MSG", jsonResponse.toString());
                populateUserList(jsonResponse);
                //setupListViewItemClickListener();
                setupListViewCheckboxListeners();
                checkSelectedItems();
            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    // Populate userList with server response
    private void populateUserList(JSONObject serverResponse)
    {
        try {
            // Split the response in a list
            JSONArray jsonUserList = serverResponse.getJSONArray("users");

            // For each user, take his name and add it to the list shown in the ListView
            for(int i = 0; i < jsonUserList.length(); i++)
            {
                JSONObject jsonUser = (JSONObject) jsonUserList.get(i);
                Log.d("JSON", jsonUser.getString("nome"));
                userList.add(jsonUser.getString("nome"));
            }

            // Tell the ListView to reload elements
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkSelectedItems()
    {
        RelativeLayout element;
        Integer result = 0;
        for(int i = 0; i < listView.getAdapter().getCount(); i++)
        {
            element = (RelativeLayout) listView.getAdapter().getView(i, null, listView);
            CheckBox check = (CheckBox) element.findViewById(R.id.userListItemCheckbox);
            if(check.isChecked())
            {
                result++;
            }
        }
        Log.d("Check count", result.toString());

    }

    private void setupConfirmButtonListener(View fragment)
    {
        fragment.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                checkSelectedItems();
            }
        });
    }

//    private void setupListViewItemClickListener()
//    {
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
//                Toast.makeText(getActivity().getApplicationContext(), "Selected position: " + i,
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void setupListViewCheckboxListeners()
    {
        RelativeLayout element;
        for(int i = 0; i < listView.getAdapter().getCount(); i++)
        {
            element = (RelativeLayout) listView.getAdapter().getView(i, null, listView);
            Log.d("Check", ((TextView) element.findViewById(R.id.userListItemTextView)).getText().toString());
            CheckBox checkbox = (CheckBox) element.findViewById(R.id.userListItemCheckbox);
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d("Check", ((Boolean)isChecked).toString());
                    CheckBox checkbox = (CheckBox)buttonView;
                    boolean isChecked2 = checkbox.isChecked();
                    Log.d("Check", ((Boolean)isChecked2).toString());
                }
            });
        }
    }

}
