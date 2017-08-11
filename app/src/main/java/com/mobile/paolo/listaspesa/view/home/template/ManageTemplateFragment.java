package com.mobile.paolo.listaspesa.view.home.template;

import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.adapters.TemplateCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.HomeFragmentContainer;

import java.util.List;


/**
 * -- ManageTemplateFragment --
 * This fragment is loaded when the user selects the "Template" tab and his group has at least
 * one template.
 * It shows a list of all templates with a preview of the products.
 */
public class ManageTemplateFragment extends Fragment
{
    // Widgets
    private Toolbar toolbar;
    private FloatingActionButton newTemplateButton;

    // RecyclerView, adapter and model
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Template> templateModelList;


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

        initializeWidgets(loadedFragment);

        setupToolbar();

        setupNewTemplateButtonListener();

        return loadedFragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        setupRecyclerView();

    }

    private void setupRecyclerView()
    {
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        templateModelList = GlobalValuesManager.getInstance(getContext()).getUserTemplates();

        adapter = new TemplateCardViewDataAdapter(templateModelList);

        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private void setupToolbar()
    {
        toolbar.setTitle(getString(R.string.manage_fragment_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void initializeWidgets(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewTemplates);
        toolbar = (Toolbar) loadedFragment.findViewById(R.id.manageTemplateToolbar);
        newTemplateButton = (FloatingActionButton) loadedFragment.findViewById(R.id.newTemplateButton);
    }

    private void setupNewTemplateButtonListener()
    {
        newTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });
    }

    private void changeFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if(HomeFragmentContainer.getInstance().getCreateTemplateFragment() == null)
        {
            CreateTemplateFragment createTemplateFragment = new CreateTemplateFragment();
            HomeFragmentContainer.getInstance().setCreateTemplateFragment(createTemplateFragment);
        }
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateTemplateFragment());
        transaction.commit();
    }








}
