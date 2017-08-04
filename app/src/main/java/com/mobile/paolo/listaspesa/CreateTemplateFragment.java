package com.mobile.paolo.listaspesa;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.model.adapters.TemplateCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONObject;

import java.util.List;


/**
 *
 */
public class CreateTemplateFragment extends Fragment
{
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Template> templateModelList;

    public static CreateTemplateFragment newInstance()
    {
        CreateTemplateFragment fragment = new CreateTemplateFragment();
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
        View loadedFragment = inflater.inflate(R.layout.fragment_create_template, container, false);

        setupRecyclerView(loadedFragment);

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






}
