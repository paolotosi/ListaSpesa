package com.mobile.paolo.listaspesa.view.home.template;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
 * This Activity lets the user modify a template.
 * The modifiable parameters are the template name and the list of products.
 * The changes in the product list are handled in conjunction with another activity, AddTemplateProductsActivity.
 * Specifically, this Activity shows the list of the products contained in the template; each of
 * them shows a delete button, and pressing this button adds the product in a delete set.
 * If the user decides to add other products, AddTemplateProductsActivity is called
 * using startActivityForResult. The result is a list of products to add, that will be added
 * in the add set. Sets are used to avoid duplicates.
 * When the confirm button is pressed, a query is sent to the database, changing the template name,
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

    // The template to edit
    private Template template;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;

    // Add and delete sets. Sets are used to avoid duplicates
    private Set<Product> addSet = new HashSet<>();
    private Set<Product> deleteSet = new HashSet<>();

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

        setupConfirmTemplateEditButton();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Putting this here so that we can say the new products added after onActivityResult
        Template updatedTemplate = GlobalValuesManager.getInstance(getApplicationContext()).getTemplateByID(template.getID());
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
                startAddTemplateProductsActivityForResult();
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
        confirmEditTemplateButton = (FloatingActionButton) findViewById(R.id.confirmEditTemplateButton);
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
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.EDIT_MODE);

        // set the adapter object to the RecyclerView
        recyclerView.setAdapter(adapter);

        // Set the products from the cached template (in order to handle the addition of products)
        Template cachedTemplate = GlobalValuesManager.getInstance(getApplicationContext()).getTemplateByID(template.getID());
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

    private void startAddTemplateProductsActivityForResult()
    {
        /*
            Start the add product activity for result, passing the current products in the template
            This way the user won't see products already in the template in the add activity.
         */

        // Update cached template with deleted products
        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplateProducts(template.getID(), adapter.getDeleteList());

        // Take note of the deleted products before changing activity
        deleteSet.addAll(adapter.getDeleteList());

        // Get the current list from adapter
        List<Product> currentList = adapter.getModelAsCollection();

        // Convert to JSON
        JSONArray jsonCurrentList = Product.asJSONProductList(currentList);

        // Create intent
        Intent intent = new Intent(EditTemplateActivity.this, AddTemplateProductsActivity.class);
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

                    // Update cached template
                    GlobalValuesManager.getInstance(getApplicationContext()).addTemplateProducts(template.getID(), addSet);

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
                        // Update cached template
                        GlobalValuesManager.getInstance(getApplicationContext()).changeTemplateName(template.getID(), templateNameField.getText().toString());
                        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplateProducts(template.getID(), adapter.getDeleteList());
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
            // ID
            jsonParams.put("templateID", template.getID());

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
                        GlobalValuesManager.getInstance(getApplicationContext()).removeTemplateProducts(template.getID(), addSet);
                        // Add products removed but not committed
                        GlobalValuesManager.getInstance(getApplicationContext()).addTemplateProducts(template.getID(), adapter.getDeleteList());
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
