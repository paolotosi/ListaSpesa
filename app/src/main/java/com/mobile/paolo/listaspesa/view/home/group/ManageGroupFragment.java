package com.mobile.paolo.listaspesa.view.home.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.adapters.UserCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.SharedPreferencesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;
import com.mobile.paolo.listaspesa.view.init.WelcomeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * -- ManageGroupActivity --
 * This fragment is loaded when the user select the "Gruppo" tab and he's already part of a group.
 * It shows the group info (name and members) and offers an Edit button.
 */

public class ManageGroupFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<User> groupMembersModelList = new ArrayList<>();
    private String groupName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment.
        return inflater.inflate(R.layout.fragment_manage_group, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadGroupInfo();

        setupToolbar(this.getView());

        setupEditButtonListener(this.getView());

        setupLogoutButtonListener(this.getView());

        setupAddMemberButtonListener(this.getView());

        setupProductHandlerButton((this.getView()));

        setupRecyclerView(this.getView());
    }

    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewGroupMembers);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new UserCardViewDataAdapter(groupMembersModelList, 2);

        // set the adapter object to the RecyclerView
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    private void loadGroupInfo()
    {
        Group userGroup = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup();
        groupName = userGroup.getName();
        groupMembersModelList = userGroup.getMembers();
    }

    private void setupToolbar(View loadedFragment)
    {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) loadedFragment.findViewById(R.id.collapsingToolbarGroup);
        collapsingToolbarLayout.setTitle(groupName);
    }

    private void setupEditButtonListener(View loadedFragment)
    {
        loadedFragment.findViewById(R.id.editGroupFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditGroupActivity.class);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
            }
        });
    }

    private void setupProductHandlerButton(View loadedFragment)
    {
        loadedFragment.findViewById(R.id.manageProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                if(HomeFragmentContainer.getInstance().getManageGroupProductsFragment() == null)
                {
                    ManageGroupProductsFragment manageGroupProductsFragment = new ManageGroupProductsFragment();
                    HomeFragmentContainer.getInstance().setManageGroupProductsFragment(manageGroupProductsFragment);
                }
                transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageGroupProductsFragment());
                transaction.commit();
            }
        });
    }

    private void setupLogoutButtonListener(View loadedFragment)
    {
        loadedFragment.findViewById(R.id.logoutFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void setupAddMemberButtonListener(View loadedFragment)
    {
        loadedFragment.findViewById(R.id.addMembers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getAddMemberFragment());
                    transaction.commit();
            }
        });
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
