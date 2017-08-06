package com.mobile.paolo.listaspesa.view.home.template;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.paolo.listaspesa.R;


/**
 * -- EmptyTemplateFragment --
 * An empty state shown when the user selects the "Template" tab but his group hasn't defined
 * any template yet.
 * It has a button that guides the user to the template creation section.
 */

public class EmptyTemplateFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_empty_template, container, false);
    }

}
