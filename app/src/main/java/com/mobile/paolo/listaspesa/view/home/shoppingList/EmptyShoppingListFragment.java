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
import com.mobile.paolo.listaspesa.utility.Contextualizer;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.HomeFragmentContainer;
import com.mobile.paolo.listaspesa.view.home.group.CreateGroupFragment;

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
        emptyListMessage = (TextView) loadedFragment.findViewById(R.id.emptyTemplateMessageSL);

        if(!Contextualizer.getInstance().isUserPartOfAGroup())
        {
            // Change message
            emptyListMessage.setText(getString(R.string.no_list_no_group_message));

            // Disable list creation, show group creation
            createNewListButton.setEnabled(false);
            goToGroupCreationButton.setVisibility(View.VISIBLE);

            // If the group creation button is clicked, go to to the group creation section of the app
            goToGroupCreationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BottomNavigationViewEx homeBottomNavigationView = (BottomNavigationViewEx) getActivity().findViewById(R.id.home_bottom_navigation);
                    homeBottomNavigationView.getMenu().getItem(3).setChecked(true);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.home_main_content, new CreateGroupFragment());
                    transaction.commit();
                }
            });
        }
    }

    private void setupCreateListButtonListener()
    {
        createNewListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingShoppingList(true);
                changeFragment();
            }
        });
    }

    private void changeFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateShoppingListFragment());
        transaction.commit();
    }

}
