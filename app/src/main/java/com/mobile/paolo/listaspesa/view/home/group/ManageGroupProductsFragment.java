package com.mobile.paolo.listaspesa.view.home.group;

import android.content.Intent;
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
import com.mobile.paolo.listaspesa.database.remote.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.adapters.TemplateCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
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
public class ManageGroupProductsFragment extends Fragment implements ProductCardViewDataAdapter.ViewHolder.ClickListener
{
    // Widgets
    private Toolbar toolbar;
    private FloatingActionButton newTemplateButton;

    // RecyclerView, adapter and model
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private List<Product> initialProductList;

    // Action mode
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    // Delete templates response handler
    NetworkResponseHandler deleteProductResponseHandler;
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
        View loadedFragment = inflater.inflate(R.layout.fragment_manage_group_products, container, false);

        initializeWidgets(loadedFragment);

        setupToolbar();

        setupAddProductButtonListener(loadedFragment);

        setupActionModeCallback();

        setupRecyclerView();

        populateProductList();

        return loadedFragment;
    }

    private void setupRecyclerView()
    {
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // This class listens to click events, we pass it to the adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.MANAGE_MODE);

        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private void populateProductList()
    {
        // Get the shopping list from cache
        List<Product> productList = GlobalValuesManager.getInstance(getContext()).getGroupProducts();

        // Save the initial the product list
        initialProductList = productList;

        // Set the adapter model
        adapter.replaceAll(productList);
    }

    private void setupToolbar()
    {
        toolbar.setTitle(getString(R.string.manage_products_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void initializeWidgets(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewProductsHandler);
        toolbar = (Toolbar) loadedFragment.findViewById(R.id.manageProductsToolbar);
        newTemplateButton = (FloatingActionButton) loadedFragment.findViewById(R.id.addProductButton);
    }

    private void setupAddProductButtonListener(final View loadedFragment)
    {
        newTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        loadedFragment.findViewById(R.id.addProductButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), InsertProductsActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    private void setupDeleteProductResponseHandler()
    {
        this.deleteProductResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("DELETE_RESPONSE", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(getContext(), "Sembra funzionare", Toast.LENGTH_LONG).show();

                        // Update cached template
                        for(int i = 0; i < selectedIDs.size(); i++) {
                            Integer selectedID = selectedIDs.get(i);
                            GlobalValuesManager.getInstance(getContext()).removeProduct(selectedID);
                        }

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

    private void sendDeleteProductRequest()
    {
        // Get the selected templates IDs
        List<Product> productList = adapter.getModelAsCollection();
        for(int i = 0; i < adapter.getSelectedItemCount(); i++)
        {
            selectedIDs.add(productList.get(adapter.getSelectedItems().get(i)).getID());
        }

        // JSON POST request
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("productIDs", new JSONArray(selectedIDs));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("DELETE_REQUEST", jsonParams.toString());

        // Setup response handler
        setupDeleteProductResponseHandler();

        // Send request
        GroupsDatabaseHelper.sendDeleteProductRequest(jsonParams, getContext(), deleteProductResponseHandler);
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
                mode.getMenuInflater().inflate (R.menu.delete_action_mode, menu);
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
                    sendDeleteProductRequest();
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
