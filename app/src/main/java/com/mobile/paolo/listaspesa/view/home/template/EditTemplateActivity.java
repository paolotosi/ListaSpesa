package com.mobile.paolo.listaspesa.view.home.template;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EditTemplateActivity extends AppCompatActivity {

    // Widgets
    private Toolbar editTemplateToolbar;
    private TextInputLayout templateNameInputLayout;
    private TextInputEditText templateNameField;
    private FloatingActionButton addProductsButton;

    // The template to edit
    private Template template;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;

    // Delete products response handler
    private NetworkResponseHandler updateTemplateDetailsResponseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_template);

        loadTemplateFromExtras();

        initializeWidgets();

        setupToolbar();

        setupRecyclerView();

        setupAddProductsButton();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Load menu
        getMenuInflater().inflate(R.menu.template_edit_menu, menu);

        // Add listener to menu action
        MenuItem confirmItem = menu.findItem(R.id.confirm_action);
        confirmItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                sendUpdateTemplateDetailsRequest();
                return false;
            }
        });
        return true;
    }

    // Make the 'Up' button work as the 'Back' button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        showAlertDialog();
    }

    private void initializeWidgets()
    {
        editTemplateToolbar = (Toolbar) findViewById(R.id.editTemplateToolbar);
        addProductsButton = (FloatingActionButton) findViewById(R.id.addProductsButton);
        templateNameInputLayout = (TextInputLayout) findViewById(R.id.templateNameInputLayout);
        templateNameField = (TextInputEditText) findViewById(R.id.templateNameField);

        // Populate name field with the current name
        templateNameInputLayout.setHintAnimationEnabled(false);
        templateNameField.setText(template.getName());
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

    private boolean isInsertionValid()
    {
        boolean isValid = true;
        if(templateNameField.getText().toString().isEmpty())
        {
            isValid = false;
            templateNameInputLayout.setError(getString(R.string.template_creation_KO_no_name));
        }
        else
        {
            templateNameInputLayout.setErrorEnabled(false);
        }
        return isValid;

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

        // set the adapter object to the RecyclerView
        recyclerView.setAdapter(adapter);

        adapter.replaceAll(template.getProductList());
    }

    private void setupAddProductsButton()
    {
        addProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setupUpdateTemplateDetailsResponseHandler()
    {
        this.updateTemplateDetailsResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("DELETE_REQUEST", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        // Update cached template
                        GlobalValuesManager.getInstance(getApplicationContext()).changeTemplateName(template.getID(), templateNameField.getText().toString());
                        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplateProducts(template.getID(), adapter.getDeleteList());
                        Toast.makeText(getApplicationContext(), getString(R.string.edit_ok), Toast.LENGTH_LONG).show();
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    private void sendUpdateTemplateDetailsRequest()
    {
        // Create post request
        JSONObject jsonParams = new JSONObject();
        try {
            // ID
            jsonParams.put("templateID", template.getID());

            // New name
            jsonParams.put("newName", templateNameField.getText().toString());

            // Products to remove
            JSONArray jsonDeleteProducts = new JSONArray();
            for(int i = 0; i < adapter.getDeleteList().size(); i++)
            {
                jsonDeleteProducts.put(i, adapter.getDeleteList().get(i).toJSON());
            }
            jsonParams.put("deleteProducts", jsonDeleteProducts);

            // Products to add
            JSONArray jsonAddProducts = new JSONArray();
            jsonParams.put("addProducts", jsonAddProducts);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("UPDATE_REQUEST_PARAM", jsonParams.toString());

        // Define what to do on response
        setupUpdateTemplateDetailsResponseHandler();

        // Send request
        if(isInsertionValid())
        {
            TemplatesDatabaseHelper.sendDeleteTemplateProductsRequest(jsonParams, getApplicationContext(), updateTemplateDetailsResponseHandler);
        }

    }

    private void showAlertDialog()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditTemplateActivity.this, R.style.Theme_AppCompat_Light_Dialog);
        dialogBuilder.setMessage(getString(R.string.exit_dialog_message));
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                getString(R.string.exit_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
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
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.materialRed500));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.materialGrey600));
    }



}
