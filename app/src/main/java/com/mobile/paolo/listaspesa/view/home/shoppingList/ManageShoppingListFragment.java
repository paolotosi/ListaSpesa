package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.database.ProductsDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
Shows the shopping list of the group and provides operations to manipulate the shopping list
 or to change his state from "in preparazione" to "in acquisto"
 */
public class ManageShoppingListFragment extends Fragment {

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private ShoppingList shoppingList;
    private List<Product> productModelList = new ArrayList<>();
    private GlobalValuesManager valuesManager;
    private final static String SHOPLIST = "shopListProduct";

    // Network response logic
    private NetworkResponseHandler fetchProductsResponseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_manage_shopping_list, container, false);

        // Setup toolbar
        setupToolbar(loadedFragment);

        shoppingList = GlobalValuesManager.getInstance(getContext()).getUserList();

        // Initialize the RecyclerView.
        setupRecyclerView(loadedFragment);

        populateProductList();

        // Define what to do when the server response to get products is received.
        //setupFetchProductsResponseHandler();

        // Send the network request to get all users.
/*        valuesManager = GlobalValuesManager.getInstance(getActivity().getApplicationContext());
        Map<String, String> params = new HashMap<>();
        int groupId = valuesManager.getLoggedUserGroup().getID();
        params.put("id", String.valueOf(groupId));
        JSONObject jsonPostParameters = new JSONObject(params);
        ProductsDatabaseHelper.sendGetProductsShopListRequest(jsonPostParameters, getActivity().getApplicationContext(), fetchProductsResponseHandler);*/

        return loadedFragment;
    }

    private void setupToolbar(View loadedFragment)
    {
        Toolbar toolbar = (Toolbar) loadedFragment.findViewById(R.id.shoppingListToolbar);
        toolbar.setTitle(getString(R.string.toolbar_title));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewShopProducts);

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

    private void setupFetchProductsResponseHandler()
    {
        this.fetchProductsResponseHandler = new NetworkResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonResponse) {
                populateProductList(jsonResponse);
            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    // Populate userList with server response
    private void populateProductList(JSONObject serverResponse)
    {
        try {
            // Split the response in a list.
            JSONArray jsonProductList = serverResponse.getJSONArray("products");

            // For each user, create a User object and add it to the list.
            for(int i = 0; i < jsonProductList.length(); i++)
            {
                JSONObject jsonProduct = (JSONObject) jsonProductList.get(i);
                Product toBeAdded = Product.fromJSON(jsonProduct, SHOPLIST);

                    productModelList.add(toBeAdded);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Tell the RecyclerView to reload elements
        adapter.replaceAll(productModelList);
    }

    // Populate product list with the products of the shopping list
    private void populateProductList()
    {

        productModelList = shoppingList.getProductList();
        adapter.replaceAll(productModelList);
    }

}
