package com.mobile.paolo.listaspesa.view.home.template;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.Template;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditTemplateActivity extends AppCompatActivity {

    // Widgets
    private Toolbar editTemplateToolbar;

    // The template to edit
    private Template template;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_template);

        loadTemplateFromExtras();

        initializeWidgets();

        setupToolbar();

        setupRecyclerView();

    }

    // Make the 'Up' button work as the 'Back' button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initializeWidgets()
    {
        editTemplateToolbar = (Toolbar) findViewById(R.id.editTemplateToolbar);
    }

    private void setupToolbar()
    {
        editTemplateToolbar.setTitle("Modifica template " + template.getName());
        editTemplateToolbar.setTitleTextColor(0xFFFFFFFF);

        // Show 'Up' button in the toolbar
        setSupportActionBar(editTemplateToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void loadTemplateFromExtras()
    {
        Bundle extras = getIntent().getExtras();
        String jsonTemplate = extras.getString("TEMPLATE");
        try {
            template = Template.fromJSON(new JSONObject(jsonTemplate));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView()
    {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewProducts);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.EDIT_MODE);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);

        adapter.replaceAll(template.getProductList());
    }




}
