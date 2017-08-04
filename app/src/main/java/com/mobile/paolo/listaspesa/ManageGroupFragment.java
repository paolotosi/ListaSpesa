package com.mobile.paolo.listaspesa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.model.adapters.UserCardViewDataAdapter;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import java.util.ArrayList;
import java.util.List;


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



}
