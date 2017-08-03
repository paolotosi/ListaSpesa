package com.mobile.paolo.listaspesa;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONObject;


/**
 *
 */
public class CreateTemplateFragment extends Fragment
{
    public static CreateTemplateFragment newInstance()
    {
        CreateTemplateFragment fragment = new CreateTemplateFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_create_template, container, false);



        return loadedFragment;
    }

}
