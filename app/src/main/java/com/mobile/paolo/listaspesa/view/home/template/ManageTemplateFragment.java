package com.mobile.paolo.listaspesa.view.home.template;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.TemplateCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.HomeFragmentContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * -- ManageTemplateFragment --
 * This fragment is loaded when the user selects the "Template" tab and his group has at least
 * one template.
 * It shows a list of all templates with a preview of the products.
 */
public class ManageTemplateFragment extends Fragment implements TemplateCardViewDataAdapter.ViewHolder.ClickListener
{
    // Widgets
    private Toolbar toolbar;
    private FloatingActionButton newTemplateButton;

    // RecyclerView, adapter and model
    private RecyclerView recyclerView;
    private TemplateCardViewDataAdapter adapter;
    private List<Template> templateModelList;

    // Action mode
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    // Delete templates response handler
    NetworkResponseHandler deleteTemplateResponseHandler;
    List<Integer> selectedListItems = new ArrayList<>();
    List<Integer> selectedIDs = new ArrayList<>();

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

        setupActionModeCallback();

        setupRecyclerView();

        return loadedFragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update adapter list
        List<Template> updatedTemplateList = GlobalValuesManager.getInstance(getContext()).getUserTemplates();
        adapter.replaceAll(updatedTemplateList);
        adapter.notifyDataSetChanged();

    }


    private void setupRecyclerView()
    {
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        templateModelList = GlobalValuesManager.getInstance(getContext()).getUserTemplates();

        // This class listens to click events, we pass it to the adapter
        adapter = new TemplateCardViewDataAdapter(templateModelList, this);

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

    private void setupDeleteTemplatesResponseHandler()
    {
        this.deleteTemplateResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("DELETE_RESPONSE", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(getContext(), "Sembra funzionare", Toast.LENGTH_LONG).show();

                        // Update cached template
                        GlobalValuesManager.getInstance(getContext()).removeTemplates(selectedIDs);

                        adapter.removeItems(selectedListItems);
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

    private void sendDeleteTemplatesRequest()
    {
        // Get the selected templates IDs
        List<Template> templateList = adapter.getTemplateList();
        for(int i = 0; i < adapter.getSelectedItemCount(); i++)
        {
            selectedIDs.add(templateList.get(adapter.getSelectedItems().get(i)).getID());
        }

        // JSON POST request
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("templateIDs", new JSONArray(selectedIDs));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("DELETE_REQUEST", jsonParams.toString());

        // Setup response handler
        setupDeleteTemplatesResponseHandler();

        // Send request
        TemplatesDatabaseHelper.sendDeleteTemplatesRequest(jsonParams, getContext(), deleteTemplateResponseHandler);
    }

    @Override
    public void onItemClicked(int position)
    {
        if (actionMode != null) {
            toggleSelection(position);
        }
    }

    @Override
    public boolean onItemLongClicked(int position)
    {
        if (actionMode == null)
        {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);

        return true;
    }

    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0)
        {
            actionMode.finish();
        }
        else
        {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private void setupActionModeCallback()
    {
        this.actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate (R.menu.template_action_mode, menu);
                selectedListItems.clear();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if(item.getItemId() == R.id.deleteTemplate)
                {
                    sendDeleteTemplatesRequest();
                    selectedListItems.addAll(adapter.getSelectedItems());
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelection();
                actionMode = null;
            }
        };
    }
}
