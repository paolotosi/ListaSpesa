package com.mobile.paolo.listaspesa.view.home.template;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
 * -- ManageTemplateFragment --
 * This fragment is loaded when the user selects the "Template" tab and his group has at least
 * one template.
 * It shows a list of all templates with a preview of the products.
 */
public class ManageTemplateFragment extends Fragment
{
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Template> templateModelList;

    public static ManageTemplateFragment newInstance()
    {
        ManageTemplateFragment fragment = new ManageTemplateFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_manage_template, container, false);

        setupRecyclerView(loadedFragment);

        setupToolbar(loadedFragment);

        return loadedFragment;
    }

    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewTemplates);

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        templateModelList = GlobalValuesManager.getInstance(getContext()).getUserTemplates();

        adapter = new TemplateCardViewDataAdapter(templateModelList);

        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private void setupToolbar(View loadedFragment)
    {
        Toolbar toolbar = (Toolbar) loadedFragment.findViewById(R.id.manageTemplateToolbar);
        toolbar.setTitle(getString(R.string.manage_fragment_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }





}
