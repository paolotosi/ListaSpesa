package com.mobile.paolo.listaspesa.view.home.template;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.mobile.paolo.listaspesa.database.remote.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * -- EditTemplateActivity --
 * This Activity lets the user modify a selectedTemplate.
 * The modifiable parameters are the selectedTemplate name and the list of products.
 * The changes in the product list are handled in conjunction with another activity, AddProductsActivity.
 * Specifically, this Activity shows the list of the products contained in the selectedTemplate; each of
 * them shows a delete button, and pressing this button adds the product in a delete set.
 * If the user decides to add other products, AddProductsActivity is called
 * using startActivityForResult. The result is a list of products to add, that will be added
 * in the add set. Sets are used to avoid duplicates.
 * When the confirm button is pressed, a query is sent to the database, changing the selectedTemplate name,
 * removing the product present in the delete set and adding the ones in the add set.
 */

public class EditTemplateActivity extends AppCompatActivity {

    // Constants
    private static final int ADD_PRODUCTS_REQUEST = 1;

    // Widgets
    private Toolbar editTemplateToolbar;
    private TextInputLayout templateNameInputLayout;
    private TextInputEditText templateNameField;
    private FloatingActionButton confirmEditTemplateButton;

    // The selectedTemplate to edit
    private Template selectedTemplate;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;

    // Add and delete sets. Sets are used to avoid duplicates
    private Set<Product> addSet = new HashSet<>();
    private Set<Product> deleteSet = new HashSet<>();

    private List<Integer> selectedIDs = new ArrayList<>();

    // Delete products response handler
    private NetworkResponseHandler updateTemplateDetailsResponseHandler;
    private NetworkResponseHandler deleteTemplateResponseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_template);

        loadTemplateFromExtras();

        initializeWidgets();

        setupToolbar();

        setupRecyclerView();

        setupConfirmTemplateEditButton();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Putting this here so that we can see the new products added after onActivityResult
        Template updatedTemplate = GlobalValuesManager.getInstance(getApplicationContext()).getTemplateByID(selectedTemplate.getID());
        adapter.replaceAll(updatedTemplate.getProductList());
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Load menu
        getMenuInflater().inflate(R.menu.template_edit_menu, menu);

        // Add listener to menu action
        MenuItem addProductsItem = menu.findItem(R.id.add_product_action);
        addProductsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startAddProductsActivityForResult();
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
    public void onBackPressed()
    {   if(templateModified())
        {
            showAlertDialog();
        }
        else finish();
    }

    private void initializeWidgets()
    {
        editTemplateToolbar = (Toolbar) findViewById(R.id.editTemplateToolbar);
        confirmEditTemplateButton = (FloatingActionButton) findViewById(R.id.confirmEditTemplateButton);
        templateNameInputLayout = (TextInputLayout) findViewById(R.id.templateNameInputLayout);
        templateNameField = (TextInputEditText) findViewById(R.id.templateNameField);

        // Populate name field with the current name
        templateNameInputLayout.setHintAnimationEnabled(false);
        templateNameField.setText(selectedTemplate.getName());
    }

    private void setupToolbar()
    {
        editTemplateToolbar.setTitle("Modifica " + selectedTemplate.getName());
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
        else if(isTemplateNameAlreadyInUse())
        {
            isValid = false;
            templateNameInputLayout.setError(getString(R.string.template_creation_KO_same_name));
        }
        else
        {
            templateNameInputLayout.setErrorEnabled(false);
        }
        return isValid;

    }

    private boolean isTemplateNameAlreadyInUse()
    {
        if(!GlobalValuesManager.getInstance(getApplicationContext()).hasUserTemplates())
        {
            return false;
        }
        String insertedName = templateNameField.getText().toString();
        for(Template template : GlobalValuesManager.getInstance(getApplicationContext()).getUserTemplates())
        {
            if(template.getName().equalsIgnoreCase(insertedName) && !template.getName().equalsIgnoreCase(selectedTemplate.getName()))
            {
                return true;
            }
        }
        return false;
    }

    private void loadTemplateFromExtras()
    {
        Bundle extras = getIntent().getExtras();
        String jsonTemplate = extras.getString("TEMPLATE");
        try {
            selectedTemplate = Template.fromJSON(new JSONObject(jsonTemplate));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView()
    {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewProducts);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.EDIT_MODE);

        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(adapter.getItemCount() == 0)
                {
                    sendDeleteTemplateRequest();
                }
            }
        });

        // set the adapter object to the RecyclerView
        recyclerView.setAdapter(adapter);

        // Set the products from the cached selectedTemplate (in order to handle the addition of products)
        Template cachedTemplate = GlobalValuesManager.getInstance(getApplicationContext()).getTemplateByID(selectedTemplate.getID());
        adapter.replaceAll(cachedTemplate.getProductList());
    }

    private void setupConfirmTemplateEditButton()
    {
        confirmEditTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUpdateTemplateDetailsRequest();
            }
        });
    }

    private void startAddProductsActivityForResult()
    {
        /*
            Start the add product activity for result, passing the current products in the selectedTemplate
            This way the user won't see products already in the selectedTemplate in the add activity.
         */

        // Update cached selectedTemplate with deleted products
        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplateProducts(selectedTemplate.getID(), adapter.getDeleteList());

        // Take note of the deleted products before changing activity
        deleteSet.addAll(adapter.getDeleteList());

        // Get the current list from adapter
        List<Product> currentList = adapter.getModelAsCollection();

        // Convert to JSON
        JSONArray jsonCurrentList = Product.asJSONProductList(currentList);

        // Create intent
        Intent intent = new Intent(EditTemplateActivity.this, AddProductsActivity.class);
        intent.putExtra("CURRENT_PRODUCTS", jsonCurrentList.toString());

        // Start activity for result
        startActivityForResult(intent, ADD_PRODUCTS_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ADD_PRODUCTS_REQUEST)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                try {
                    // Get added products from result and add them to the list
                    addSet.addAll(Product.parseJSONProductList(new JSONArray(data.getStringExtra("RESULT"))));
                    adapter.add(addSet);
                    adapter.notifyDataSetChanged();

                    // Update cached selectedTemplate
                    GlobalValuesManager.getInstance(getApplicationContext()).addTemplateProducts(selectedTemplate.getID(), addSet);

                    // Remove from the delete set the products just inserted
                    adapter.getDeleteList().removeAll(addSet);
                    deleteSet.removeAll(addSet);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupUpdateTemplateDetailsResponseHandler()
    {
        this.updateTemplateDetailsResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("UPDATE_REQUEST", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        // Update cached selectedTemplate
                        GlobalValuesManager.getInstance(getApplicationContext()).changeTemplateName(selectedTemplate.getID(), templateNameField.getText().toString());
                        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplateProducts(selectedTemplate.getID(), adapter.getDeleteList());
                        Toast.makeText(getApplicationContext(), getString(R.string.edit_ok), Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else
                    {
                        // Get error message
                        Log.d("UPDATE_ERROR", response.getString("message"));
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
            // Group ID
            jsonParams.put("groupID", GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID());

            // ID
            jsonParams.put("templateID", selectedTemplate.getID());

            // New name
            jsonParams.put("newName", templateNameField.getText().toString());

            // Products to remove
            deleteSet.addAll(adapter.getDeleteList());
            JSONArray jsonDeleteProducts = Product.asJSONProductList(new ArrayList<>(deleteSet));
            jsonParams.put("deleteProducts", jsonDeleteProducts);

            // Products to add
            // If a product is in the remove set, it needs to be removed from the add set
            // This handles the case when a user deletes a product and inserts it again in the same update
            addSet.removeAll(deleteSet);
            JSONArray jsonAddProducts = Product.asJSONProductList(new ArrayList<>(addSet));
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

    private boolean templateModified()
    {
        String oldName = selectedTemplate.getName();
        String newName = templateNameField.getText().toString();

        boolean nameChanged = !newName.equals(oldName); // true when name is different from before
        boolean somethingRemoved = (adapter.getDeleteList().size() > 0);
        boolean somethingAdded = (addSet.size() > 0);

        return nameChanged || somethingRemoved || somethingAdded;
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
                        // Remove the products added but not committed
                        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplateProducts(selectedTemplate.getID(), addSet);
                        // Add products removed but not committed
                        GlobalValuesManager.getInstance(getApplicationContext()).addTemplateProducts(selectedTemplate.getID(), adapter.getDeleteList());
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

    private void setupDeleteTemplatesResponseHandler()
    {
        this.deleteTemplateResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("DELETE_RESPONSE", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(getApplicationContext(), getString(R.string.template_deletion_ok), Toast.LENGTH_LONG).show();

                        // Update cached selectedTemplate
                        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplates(selectedIDs);

                        // Check if it was the only template
                        if(GlobalValuesManager.getInstance(getApplicationContext()).getUserTemplates().size() == 0)
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(false);
                        }

                        // Close activity
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

    private void sendDeleteTemplateRequest()
    {
        // Get the selected templates ID
        selectedIDs.add(selectedTemplate.getID());

        // JSON POST request
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("templateIDs", new JSONArray(selectedIDs));
            jsonParams.put("groupID", GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("DELETE_REQUEST", jsonParams.toString());

        // Setup response handler
        setupDeleteTemplatesResponseHandler();

        // Send request
        TemplatesDatabaseHelper.sendDeleteTemplatesRequest(jsonParams, getApplicationContext(), deleteTemplateResponseHandler);
    }



}
