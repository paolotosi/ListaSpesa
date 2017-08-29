package com.mobile.paolo.listaspesa.view.home.supermarket;

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
import com.mobile.paolo.listaspesa.database.remote.SupermarketDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.SupermarketCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paolo on 26/08/17.
 */

public class ManageSupermarketFragment extends Fragment implements SupermarketCardViewDataAdapter.ViewHolder.ClickListener
{
    // Widgets
    private Toolbar manageSupermarketToolbar;
    private FloatingActionButton newSupermarketButton;

    // RecyclerView, adapter and model
    private RecyclerView recyclerView;
    private SupermarketCardViewDataAdapter adapter;
    private List<Supermarket> supermarketList;

    // Action mode
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    // Delete templates response handler
    NetworkResponseHandler deleteSupermarketResponseHandler;
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
        View loadedFragment = inflater.inflate(R.layout.fragment_manage_supermarket, container, false);

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
        List<Supermarket> updatedSupermarketList = GlobalValuesManager.getInstance(getContext()).getSupermarkets();
        adapter.replaceAll(updatedSupermarketList);
        adapter.notifyDataSetChanged();

    }

    private void setupRecyclerView()
    {
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        supermarketList = GlobalValuesManager.getInstance(getContext()).getSupermarkets();

        // This class listens to click events, we pass it to the adapter
        adapter = new SupermarketCardViewDataAdapter(supermarketList, this);

        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private void setupToolbar()
    {
        manageSupermarketToolbar.setTitle(getString(R.string.manage_supermarket_toolbat));
        manageSupermarketToolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void initializeWidgets(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewSupermarkets);
        manageSupermarketToolbar = (Toolbar) loadedFragment.findViewById(R.id.manageSupermarketToolbar);
        newSupermarketButton = (FloatingActionButton) loadedFragment.findViewById(R.id.addSupermarketButton);
    }

    private void setupNewTemplateButtonListener()
    {
        newSupermarketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSupermarketFragment();
            }
        });
    }

    private void showAddSupermarketFragment()
    {
        // Reset create template fragment (get rid of old insertion)
        HomeFragmentContainer.getInstance().resetCreateSupermarketFragment();

        // Save this fragment in the stack and change fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateSupermarketFragment()).addToBackStack("ManageSupermarkets");
        transaction.commit();

        // Signal that the stack is not empty
        HomeFragmentContainer.getInstance().setStackEmpty(false);
    }

    private void setupDeleteTemplatesResponseHandler()
    {
        this.deleteSupermarketResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("SUPERMARKET_DEL_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(getContext(), getString(R.string.template_deletion_ok), Toast.LENGTH_LONG).show();

                        // Update cached template
                        GlobalValuesManager.getInstance(getContext()).deleteSupermarkets(selectedIDs);

                        adapter.removeItems(selectedListItems);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    private void sendDeleteSupermarketsRequest()
    {
        // Get the selected templates IDs
        List<Supermarket> templateList = adapter.getSupermarketList();
        for(int i = 0; i < adapter.getSelectedItemCount(); i++)
        {
            selectedIDs.add(templateList.get(adapter.getSelectedItems().get(i)).getID());
        }

        // JSON POST request
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("supermarketIDs", new JSONArray(selectedIDs));
            jsonParams.put("groupID", GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("SUPERMARKET_DEL_REQ", jsonParams.toString());

        // Setup response handler
        setupDeleteTemplatesResponseHandler();

        // Send request
        SupermarketDatabaseHelper.sendDeleteSupermarketRequest(jsonParams, getContext(), deleteSupermarketResponseHandler);
    }

    @Override
    public void onItemClicked(int position)
    {
        if (actionMode != null)
        {
            // If all items except one are selected
            if(adapter.getSelectedItems().size() == adapter.getItemCount() - 1)
            {
                // Determine if the item clicked is the only one not selected
                boolean clickOnLastItem = true;
                for(Integer i : adapter.getSelectedItems())
                {
                    if (i == position)
                    {
                        clickOnLastItem = false;
                    }
                }
                // It was the last item: feedback
                if(clickOnLastItem)
                {
                    Toast.makeText(getContext(), "Non è possibile eliminare tutti i supermercati", Toast.LENGTH_SHORT).show();
                }
                // It wasn't: unselect
                else toggleSelection(position);

            }
            // Normal scenario
            else
            {
                toggleSelection(position);
            }
        }
    }

    @Override
    public boolean onItemLongClicked(int position)
    {
        if (actionMode == null)
        {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
        }

        // Toggle position only if it's not the only item remaining and if the item clicked is not the only one not selected
        if(adapter.getItemCount() > 1 && !(adapter.getSelectedItems().size() == adapter.getItemCount() - 1))
        {
            toggleSelection(position);
        }
        else
        {
            Toast.makeText(getContext(), "Non è possibile eliminare tutti i supermercati", Toast.LENGTH_SHORT).show();
        }

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
                if(adapter.getItemCount() == 1)
                {
                    Toast.makeText(getContext(), "Non è possibile eliminare tutti i supermercati", Toast.LENGTH_SHORT).show();
                    return false;
                }
                else
                {
                    mode.getMenuInflater().inflate (R.menu.delete_action_mode, menu);
                    selectedListItems.clear();
                    return true;
                }
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if(item.getItemId() == R.id.deleteTemplate)
                {
                    sendDeleteSupermarketsRequest();
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

    @Override
    public void onPause() {
        super.onPause();
        if(actionMode != null)
        {
            actionMode.finish();
        }
    }
}
