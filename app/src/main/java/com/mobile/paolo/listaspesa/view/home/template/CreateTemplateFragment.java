package com.mobile.paolo.listaspesa.view.home.template;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.ProductsDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class CreateTemplateFragment extends Fragment implements SearchView.OnQueryTextListener {

    // Widgets
    private TextInputEditText templateNameField;
    private Button confirmTemplateCreationButton;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private List<Product> productList = new ArrayList<>();

    private final static String BASE = "baseProduct";



    // Network response handlers
    private NetworkResponseHandler fetchProductsResponseHandler;
    private NetworkResponseHandler createTemplateResponseHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_create_template, container, false);

        setupToolbar(loadedFragment);

        setupRecyclerView(loadedFragment);

        setupFetchProductsResponseHandler();

        ProductsDatabaseHelper.sendGetAllProductsRequest(null, getContext(), fetchProductsResponseHandler);

        setHasOptionsMenu(true);

        return loadedFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate menu
        getActivity().getMenuInflater().inflate(R.menu.template_creation_menu, menu);

        // Get the search bar
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Setup hint, width and listener
        searchView.setQueryHint(getString(R.string.action_search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);

        // Close the search when it loses focus (after pressing the back button)
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    searchView.onActionViewCollapsed();
                }
            }
        });
    }

    private void setupToolbar(View loadedFragment)
    {
        Toolbar toolbar = (Toolbar) loadedFragment.findViewById(R.id.createTemplateToolbar);
        toolbar.setTitle(getString(R.string.create_template_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
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
                error.printStackTrace();
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
        adapter = new ProductCardViewDataAdapter();

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }

    private void populateProductList(JSONArray jsonProducts)
    {
        try {
            for(int i = 0; i < jsonProducts.length(); i++)
            {
                Product product = Product.fromJSON((JSONObject) jsonProducts.get(i), BASE);
                productList.add(product);
                Log.d("Prodotto", product.getName());
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        adapter.replaceAll(productList);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<Product> filteredModelList = filter(productList, query);
        adapter.replaceAll(filteredModelList);
        recyclerView.scrollToPosition(0);
        return true;
    }


    private static List<Product> filter(List<Product> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<Product> filteredModelList = new ArrayList<>();
        for (Product model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }



}
