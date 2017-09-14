package com.mobile.paolo.listaspesa.view.home.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.remote.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;

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
public class ManageGroupProductsFragment extends Fragment
{
    // Widgets
    private Toolbar toolbar;
    private FloatingActionButton addProductButton;

    // RecyclerView, adapter and model
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private List<Product> initialProductList;


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

        setupAddProductButtonListener();

        setupRecyclerView();

        populateProductList();

        // Otherwise the 'Up' button won't work
        setHasOptionsMenu(true);

        return loadedFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Product> products = GlobalValuesManager.getInstance(getContext()).getGroupProducts();
        adapter.replaceAll(products);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingSupermarket(false);
            showManageGroupFragment();
        }
        return true;
    }

    private void showManageGroupFragment()
    {
        if(HomeFragmentContainer.getInstance().isStackEmpty())
        {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageGroupFragment());
            transaction.commit();
        }
        else
        {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            HomeFragmentContainer.getInstance().setStackEmpty(true);
        }
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

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initializeWidgets(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewProductsHandler);
        toolbar = (Toolbar) loadedFragment.findViewById(R.id.manageProductsToolbar);
        addProductButton = (FloatingActionButton) loadedFragment.findViewById(R.id.addProductButton);
    }

    private void setupAddProductButtonListener()
    {
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        Intent intent = new Intent(getActivity(), InsertProductsActivity.class);
                        intent.putExtra("flag", true);
                        startActivity(intent);
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
                error.printStackTrace();
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


}
