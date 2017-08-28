package com.mobile.paolo.listaspesa.view.home.group;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.SharedPreferencesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;
import com.mobile.paolo.listaspesa.view.init.WelcomeActivity;

/**
 * -- EmptyGroupFragment --
 * An empty state shown when the user selects the "Group" tab but he isn't part of a group yet.
 * It has a button that guides the user to the group creation section.
 */

public class EmptyGroupFragment extends Fragment {

    private Button createNewGroupButton;
    private Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_empty_group, container, false);

        initializeWidgets(loadedFragment);

        setupCreateGroupButtonListener();

        setupLogoutButtonListener();

        return loadedFragment;
    }

    private void initializeWidgets(View loadedFragment)
    {
        createNewGroupButton = (Button) loadedFragment.findViewById(R.id.createNewGroupButton);
        logoutButton = (Button) loadedFragment.findViewById(R.id.logoutButton);
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

    private void setupLogoutButtonListener()
    {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void changeFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateGroupFragment());
        transaction.commit();
    }

    private void showLogoutDialog()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog);

        dialogBuilder.setMessage(getString(R.string.logout_dialog));
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                getString(R.string.logout_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logout();
                    }
                });

        dialogBuilder.setNegativeButton(
                getString(R.string.cancel_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActivity().getColor(R.color.materialRed500));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getActivity().getColor(R.color.materialGrey600));
    }

    private void logout()
    {
        // Flush SharedPreferences and reset fragments
        SharedPreferencesManager.getInstance(getContext()).flush();
        HomeFragmentContainer.getInstance().reset();

        Intent intent = new Intent(getContext(), WelcomeActivity.class);
        // Remove this activity from stack after loading the new one
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
