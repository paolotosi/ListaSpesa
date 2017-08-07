package com.mobile.paolo.listaspesa.view.home.template;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobile.paolo.listaspesa.R;


/**
 * -- EmptyTemplateFragment --
 * An empty state shown when the user selects the "Template" tab but his group hasn't defined
 * any template yet.
 * It has a button that guides the user to the template creation section.
 */

public class EmptyTemplateFragment extends Fragment
{
    private Button createNewTemplateButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_empty_template, container, false);

        initializeWidgets(loadedFragment);

        setupCreateTemplateButtonListener();

        return loadedFragment;

    }

    private void initializeWidgets(View loadedFragment)
    {
        createNewTemplateButton = (Button) loadedFragment.findViewById(R.id.createNewTemplateButton);
    }

    private void setupCreateTemplateButtonListener()
    {
        createNewTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });
    }

    private void changeFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, new CreateTemplateFragment());
        transaction.commit();
    }

}
