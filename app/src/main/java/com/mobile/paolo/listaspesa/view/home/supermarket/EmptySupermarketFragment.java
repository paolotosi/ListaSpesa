package com.mobile.paolo.listaspesa.view.home.supermarket;
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
 * Created by paolo on 26/08/17.
 */

public class EmptySupermarketFragment extends Fragment
{
    // Widgets
    private Button createNewSupermarketButton;
    private Button goToGroupCreationButton;
    private TextView emptySupermarketMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_empty_supermarket, container, false);

        initializeWidgets(loadedFragment);

        return loadedFragment;
    }

    private void initializeWidgets(final View loadedFragment)
    {
        createNewSupermarketButton = (Button) loadedFragment.findViewById(R.id.createNewSupermarketButton);
        goToGroupCreationButton = (Button) loadedFragment.findViewById(R.id.goToGroupCreationButton);
        emptySupermarketMessage = (TextView) loadedFragment.findViewById(R.id.emptySupermarketMessage);

        if(!GlobalValuesManager.getInstance(getContext()).isUserPartOfAGroup())
        {
            // Change message
            emptySupermarketMessage.setText(getString(R.string.no_template_no_group_message));

            // Disable template creation, show group creation
            createNewSupermarketButton.setEnabled(false);
            goToGroupCreationButton.setVisibility(View.VISIBLE);

            // If the group creation button is clicked, go to to the group creation section of the app
            setupCreateGroupButtonListener();

        }
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
