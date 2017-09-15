package com.mobile.paolo.listaspesa.view.home.template;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;


/**
 * -- EmptyTemplateFragment --
 * An empty state shown when the user selects the "Template" tab but his group hasn't defined
 * any template yet.
 * It has a button that guides the user to the template creation section.
 */

public class EmptyTemplateFragment extends Fragment
{
    private Button createNewTemplateButton;
    private Button goToGroupCreationButton;
    private TextView emptyTemplateMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_empty_template, container, false);

        initializeWidgets(loadedFragment);

        setupCreateTemplateButtonListener();

        return loadedFragment;

    }

    private void initializeWidgets(final View loadedFragment)
    {
        createNewTemplateButton = (Button) loadedFragment.findViewById(R.id.createNewTemplateButton);
        goToGroupCreationButton = (Button) loadedFragment.findViewById(R.id.goToGroupCreationButton);
        emptyTemplateMessage = (TextView) loadedFragment.findViewById(R.id.emptyTemplateMessage);

        if(!GlobalValuesManager.getInstance(getContext()).isUserPartOfAGroup())
        {
            // Change message
            emptyTemplateMessage.setText(getString(R.string.no_template_no_group_message));

            // Disable template creation, show group creation
            createNewTemplateButton.setEnabled(false);
            goToGroupCreationButton.setVisibility(View.VISIBLE);

            // If the group creation button is clicked, go to to the group creation section of the app
            setupCreateGroupButtonListener();

        }
    }

    private void setupCreateTemplateButtonListener()
    {
        createNewTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingTemplate(true);
                showCreateTemplateFragment();
            }
        });
    }

    private void showCreateTemplateFragment()
    {
        // Reset CreateTemplateFragment
        HomeFragmentContainer.getInstance().resetCreateTemplateFragment();

        // Change fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateTemplateFragment());
        transaction.commit();
    }

    private void setupCreateGroupButtonListener()
    {
        goToGroupCreationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingGroup(true);
                showCreateGroupFragment();
            }
        });
    }

    private void showCreateGroupFragment()
    {
        // Change bottom navigation selected tab
        BottomNavigationViewEx homeBottomNavigationView = (BottomNavigationViewEx) getActivity().findViewById(R.id.home_bottom_navigation);
        homeBottomNavigationView.getMenu().getItem(3).setChecked(true);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateGroupFragment());
        transaction.commit();
    }

}
