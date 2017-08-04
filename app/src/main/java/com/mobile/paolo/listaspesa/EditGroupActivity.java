package com.mobile.paolo.listaspesa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.database.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**

 */
public class EditGroupActivity extends AppCompatActivity
{
    // Network response status
    private static final int TAG_SUCCESS = 1;

    // Widgets
    private TextInputLayout editNameTextInputLayout;
    private TextInputEditText editNameField;
    private Button confirmButton;
    private Button cancelButton;

    // Old group name
    private String oldName;

    // Network response handler
    private NetworkResponseHandler modifyNameResponseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        initializeWidgets();
        initializeButtonListeners();
    }

    private void initializeWidgets()
    {
        editNameTextInputLayout = (TextInputLayout) findViewById(R.id.editNameTextInputLayout);
        editNameField = (TextInputEditText) findViewById(R.id.editNameField);
        confirmButton = (Button) findViewById(R.id.confirmButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        oldName = getIntent().getExtras().getString("groupName");
        editNameField.setText(oldName);
    }

    private void initializeButtonListeners()
    {
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInsertionValid())
                {
                    setupModifyNameResponseHandler();

                    // The POST parameters.
                    Map<String, String> params = new HashMap<>();
                    params.put("newName", editNameField.getText().toString());
                    params.put("groupID", ((Integer) GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID()).toString());

                    // Encapsulate in JSON.
                    JSONObject jsonPostParameters = new JSONObject(params);

                    // Print parameters to console for debug purposes.
                    Log.d("JSON_GET_GROUP_DETAILS", jsonPostParameters.toString());

                    GroupsDatabaseHelper.sendModifyGroupNameRequest(jsonPostParameters, getApplicationContext(), modifyNameResponseHandler);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnHome();
            }
        });
    }

    private void setupModifyNameResponseHandler()
    {
        this.modifyNameResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if(response.getInt("success") == TAG_SUCCESS)
                    {
                        Toast.makeText(getApplicationContext(), getString(R.string.success_feedback), Toast.LENGTH_LONG).show();
                        updateSharedPreferences();
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

    private boolean isInsertionValid()
    {
        boolean isValid = true;

        if(editNameField.getText().toString().isEmpty())
        {
            isValid = false;
            editNameTextInputLayout.setError(getString(R.string.empty_group_error));
        }
        if(editNameField.getText().toString().equals(oldName))
        {
            isValid = false;
            editNameTextInputLayout.setError(getString(R.string.same_name_warning));
        }

        return isValid;
    }

    private void returnHome()
    {
        Intent intent = new Intent(EditGroupActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    private void updateSharedPreferences()
    {
        Group group = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup();
        group.setName(editNameField.getText().toString());
        GlobalValuesManager.getInstance(getApplicationContext()).saveLoggedUserGroup(group);
    }


}
