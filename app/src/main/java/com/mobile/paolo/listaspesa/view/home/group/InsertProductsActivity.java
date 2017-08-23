package com.mobile.paolo.listaspesa.view.home.group;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.remote.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InsertProductsActivity extends AppCompatActivity {

    // Network response status
    private static final int TAG_SUCCESS = 1;

    // Widgets
    private TextInputLayout editNameTextInputLayout;
    private TextInputEditText editNameField;
    private TextInputLayout editBrandTextInputLayout;
    private TextInputEditText editBrandField;
    private TextInputLayout editDescriptionTextInputLayout;
    private TextInputEditText editDescriptionField;
    private Button confirmButton;
    private Button cancelButton;

    // Old group name
    private String oldName;

    // Network response handler
    private NetworkResponseHandler insertProductResponseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_products);

        initializeWidgets();
        initializeButtonListeners();
    }

    private void initializeWidgets()
    {
        editNameTextInputLayout = (TextInputLayout) findViewById(R.id.editNameTextInputLayout);
        editNameField = (TextInputEditText) findViewById(R.id.editNameField);
        editBrandTextInputLayout = (TextInputLayout) findViewById(R.id.editBrandTextInputLayout);
        editBrandField = (TextInputEditText) findViewById(R.id.editBrandField);
        editDescriptionTextInputLayout = (TextInputLayout) findViewById(R.id.editDescriptionTextInputLayout);
        editDescriptionField = (TextInputEditText) findViewById(R.id.editDescriptionField);
        confirmButton = (Button) findViewById(R.id.confirmButtonInsert);
        cancelButton = (Button) findViewById(R.id.cancelButtonInsert);

        oldName = getIntent().getExtras().getString("groupName");
    }

    private void initializeButtonListeners()
    {
        // If the insertion is ok, send the update request to the database.
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInsertionValid())
                {
                    setupModifyNameResponseHandler();

                    JSONObject jsonParam = new JSONObject();
                    try {
                        jsonParam.put("id", String.valueOf(GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID()));
                        Product toInsert = new Product(0, editNameField.getText().toString(), editBrandField.getText().toString(), editDescriptionField.getText().toString());
                        JSONObject jsonProduct = toInsert.toJSON();
                        jsonParam.put("productDetails", jsonProduct);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("UPDATE_PRODUCT_TABLE", jsonParam.toString());
                    GroupsDatabaseHelper.updateProductTable(jsonParam, getApplicationContext(), insertProductResponseHandler);

                }
            }
        });

        // If "Annulla" is clicked, return home.
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnHome();
            }
        });
    }

    // Define what to do after the update request has been handled by the server.
    private void setupModifyNameResponseHandler()
    {
        this.insertProductResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if(response.getInt("success") == TAG_SUCCESS)
                    {
                        // Everything is ok, show a toast to inform the user
                        Toast.makeText(getApplicationContext(), getString(R.string.success_feedback_product), Toast.LENGTH_LONG).show();
                        returnHome();
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

    // Verify that the insertion isn't empty / the same name
    private boolean isInsertionValid()
    {
        boolean isValid = true;

        if(editNameField.getText().toString().isEmpty())
        {
            isValid = false;
            editNameTextInputLayout.setError(getString(R.string.empty_field_error));
        }

        if(editBrandField.getText().toString().isEmpty())
        {
            isValid = false;
            editBrandTextInputLayout.setError(getString(R.string.empty_field_error));
        }

        return isValid;
    }

    private void returnHome()
    {
        Intent intent = new Intent(InsertProductsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    // After the name is changed, the group info saved needs to be updated
    private void updateSharedPreferences()
    {

    }

}
