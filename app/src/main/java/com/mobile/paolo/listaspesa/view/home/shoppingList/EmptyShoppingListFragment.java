package com.mobile.paolo.listaspesa.view.home.shoppingList;

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
 * -- EmptyListFragment --
 * An empty state shown when the user selects the "List" tab but his group hasn't defined
 * any list yet.
 * It has a button that guides the user to the list creation section.
 */
public class EmptyShoppingListFragment extends Fragment {

    private Button createNewListButton;
    private TextView emptyListMessage;
    private Button goToGroupCreationButton;
    private Button goToTemplateCreationButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_empty_shopping_list, container, false);

        initializeWidgets(loadedFragment);

        setupCreateListButtonListener();

        return loadedFragment;

    }

    private void initializeWidgets(View loadedFragment)
    {
        createNewListButton = (Button) loadedFragment.findViewById(R.id.createNewListButton);
        goToGroupCreationButton = (Button) loadedFragment.findViewById(R.id.goToGroupCreationButtonSL);
        goToTemplateCreationButton = (Button) loadedFragment.findViewById(R.id.goToTemplateCreation);
        emptyListMessage = (TextView) loadedFragment.findViewById(R.id.emptyTemplateMessageSL);

        if(!GlobalValuesManager.getInstance(getContext()).isUserPartOfAGroup())
        {
            // The user cannot create the list if he's not part of a group

            // Change message
            emptyListMessage.setText(getString(R.string.no_list_no_group_message));

            // Disable list creation, show group creation
            createNewListButton.setEnabled(false);
            goToGroupCreationButton.setVisibility(View.VISIBLE);

            // If the group creation button is clicked, go to to the group creation section of the app
            goToGroupCreationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGroupCreationFragment();
                }
            });
        }

        if(GlobalValuesManager.getInstance(getContext()).isUserPartOfAGroup() && !GlobalValuesManager.getInstance(getContext()).hasUserTemplates())
        {
            // The user cannot create the list if his group hasn't any template

            // Change message
            emptyListMessage.setText(getString(R.string.no_list_no_template_message));

            // Disable list creation, show template creation
            createNewListButton.setEnabled(false);
            goToGroupCreationButton.setVisibility(View.GONE);
            goToTemplateCreationButton.setVisibility(View.VISIBLE);

            // If the template creation button is clicked, go to the template creation section of the app
            goToTemplateCreationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTemplateCreationFragment();
                }
            });

        }

        if(GlobalValuesManager.getInstance(getContext()).hasUserShoppingList())
        {
            emptyListMessage.setText(getString(R.string.create_new_list_button_alt) + "\n\n" + GlobalValuesManager.getInstance(getContext()).getUserTookList() + " ha preso in carico la lista.");
        }
    }

    private void setupCreateListButtonListener()
    {
        createNewListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingShoppingList(true);
                showListCreationFragment();
            }
        });
    }

    private void showListCreationFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateShoppingListFragment());
        transaction.commit();
    }

    private void showGroupCreationFragment()
    {
        // Update bottom navigation selected tab
        BottomNavigationViewEx homeBottomNavigationView = (BottomNavigationViewEx) getActivity().findViewById(R.id.home_bottom_navigation);
        homeBottomNavigationView.getMenu().getItem(3).setChecked(true);

        // Change fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateGroupFragment());
        transaction.commit();

        // Remember that the user is creating a group
        GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingGroup(true);
    }

    private void showTemplateCreationFragment()
    {
        // Update bottom navigation selected tab
        BottomNavigationViewEx homeBottomNavigationView = (BottomNavigationViewEx) getActivity().findViewById(R.id.home_bottom_navigation);
        homeBottomNavigationView.getMenu().getItem(1).setChecked(true);

        // Reset template creation fragment
        HomeFragmentContainer.getInstance().resetCreateTemplateFragment();

        // Change fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateTemplateFragment());
        transaction.commit();

        // Remember that the user is creating a template
        GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingTemplate(true);
    }

}
