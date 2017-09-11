package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.adapters.TemplateCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import java.util.List;

/**
 * -- CreateShoppingListFragment --
 * This fragment shows group templates, user can choose one of them to initialize the shopping list with template products
 */

public class CreateShoppingListFragment extends Fragment {

    // RecyclerView, adapter and model
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Template> templateModelList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_create_list, container, false);

        setupToolbar(loadedFragment);

        setupRecyclerView(loadedFragment);

        return loadedFragment;
    }


    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewCreateList);

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        templateModelList = GlobalValuesManager.getInstance(getContext()).getUserTemplates();

        adapter = new TemplateCardViewDataAdapter(templateModelList, false);

        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private void setupToolbar(View loadedFragment)
    {
        Toolbar toolbar = (Toolbar) loadedFragment.findViewById(R.id.createListToolbar);
        toolbar.setTitle(getString(R.string.create_list_fragment_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

}

