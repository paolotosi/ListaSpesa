package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobile.paolo.listaspesa.R;

/**
 * -- EmptyListFragment --
 * An empty state shown when the user selects the "List" tab but his group hasn't defined
 * any list yet.
 * It has a button that guides the user to the list creation section.
 */
public class EmptyShoppingListFragment extends Fragment {

    private Button createNewListButton;

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
    }

    private void setupCreateListButtonListener()
    {
        createNewListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });
    }

    private void changeFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        //transaction.replace(R.id.home_main_content, new CreateListFragment());
        transaction.commit();
    }

}
