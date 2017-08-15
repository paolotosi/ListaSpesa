package com.mobile.paolo.listaspesa.view.home.group;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.HomeFragmentContainer;

/**
 * -- EmptyGroupFragment --
 * An empty state shown when the user selects the "Group" tab but he isn't part of a group yet.
 * It has a button that guides the user to the group creation section.
 */

public class EmptyGroupFragment extends Fragment {

    private Button createNewGroupButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_empty_group, container, false);

        initializeWidgets(loadedFragment);

        setupCreateGroupButtonListener();

        return loadedFragment;
    }

    private void initializeWidgets(View loadedFragment)
    {
        createNewGroupButton = (Button) loadedFragment.findViewById(R.id.createNewGroupButton);
    }

    private void setupCreateGroupButtonListener()
    {
        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingGroup(true);
                changeFragment();
            }
        });
    }

    private void changeFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateGroupFragment());
        transaction.commit();
    }

}
