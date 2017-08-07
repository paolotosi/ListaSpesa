package com.mobile.paolo.listaspesa.view.home.template;


import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.ProductsDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CreateTemplateFragment extends Fragment {

    // Widgets
    TextInputEditText templateNameField;
    Button confirmTemplateCreationButton;

    // RecyclerView, adapter and model list
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<Product> productList = new ArrayList<>();

    // Network response handlers
    NetworkResponseHandler fetchProductsResponseHandler;
    NetworkResponseHandler createTemplateResponseHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_create_template, container, false);

        setupToolbar(loadedFragment);

        setupRecyclerView(loadedFragment);

        setupFetchProductsResponseHandler();

        ProductsDatabaseHelper.sendGetAllProductsRequest(null, getContext(), fetchProductsResponseHandler);

        return loadedFragment;
    }

    private void setupToolbar(View loadedFragment)
    {
        Toolbar toolbar = (Toolbar) loadedFragment.findViewById(R.id.createTemplateToolbar);
        toolbar.setTitle(getString(R.string.create_template_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void setupFetchProductsResponseHandler()
    {
        this.fetchProductsResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("GET_ALL_PRODUCTS", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        populateProductList(response.getJSONArray("products"));
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

    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewProducts);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(productList);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }

    private void populateProductList(JSONArray jsonProducts)
    {
        try {
            for(int i = 0; i < jsonProducts.length(); i++)
            {
                Product product = Product.fromJSON((JSONObject) jsonProducts.get(i));
                productList.add(product);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }


}
