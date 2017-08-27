package com.mobile.paolo.listaspesa.view.home.supermarket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.paolo.listaspesa.R;

/**
 * Created by paolo on 26/08/17.
 */

public class ManageSupermarketFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_manage_supermarket, container, false);

        return loadedFragment;
    }
}
