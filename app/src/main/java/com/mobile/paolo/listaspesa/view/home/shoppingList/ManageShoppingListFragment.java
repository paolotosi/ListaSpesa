package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    // Widgets
    private FloatingActionButton confirmShoppingListCreationButton;
    private Toolbar createListToolbar;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private ShoppingList shoppingList;
    private List<Product> productModelList = new ArrayList<>();

    // Network response logic
    private NetworkResponseHandler fetchProductsResponseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(!(getArguments() == null)){
        String strtext=getArguments().getString("LIST");
        Log.d("JSON", strtext);}

        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_manage_shopping_list, container, false);

        initializeWidgets(loadedFragment);

        setupToolbar(loadedFragment);

        setupRecyclerView(loadedFragment);

        setupConfirmButtonListener();

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
        createListToolbar.setTitle(getString(R.string.toolbar_title));
        createListToolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void initializeWidgets(View loadedFragment)
    {
        createListToolbar = (Toolbar) loadedFragment.findViewById(R.id.shoppingListToolbar);
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewShopProducts);
        confirmShoppingListCreationButton = (FloatingActionButton) loadedFragment.findViewById(R.id.confirmShoppingListCreationButton);
    }

    private void setupRecyclerView(View loadedFragment)
    {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.LIST_MODE);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }


    // Populate product list with the products of the shopping list
    private void populateProductList()
    {
        // Get the shopping list from cache
        shoppingList = GlobalValuesManager.getInstance(getContext()).getUserList();

        // Get the product list
        productModelList = shoppingList.getProductList();

        // Set the adapter model
        adapter.replaceAll(productModelList);
    }

    private void setupConfirmButtonListener()
    {
        confirmShoppingListCreationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Product> productList = adapter.getModelAsCollection();
                for(int i = 0; i < productList.size(); i++)
                {
                    Log.d("Product quantity: ", ((Integer) productList.get(i).getQuantity()).toString());
                }
            }
        });
    }
}
