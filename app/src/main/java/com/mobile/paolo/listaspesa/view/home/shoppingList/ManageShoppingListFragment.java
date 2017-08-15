package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.ShoppingListDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.template.AddProductsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
Shows the shopping list of the group and provides operations to manipulate the shopping list
 or to change his state from "in preparazione" to "in acquisto"
 */
public class ManageShoppingListFragment extends Fragment implements ProductCardViewDataAdapter.ViewHolder.ClickListener
{

    // Constants
    private static final int ADD_PRODUCTS_REQUEST = 1;

    // Widgets
    private FloatingActionButton addProductToListButton;
    private Toolbar createListToolbar;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private ShoppingList shoppingList;
    private List<Product> productModelList = new ArrayList<>();

    // Final set of list products
    private Set<Product> finalProductSet = new HashSet<>();

    // Action mode
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    // Network response logic
    private NetworkResponseHandler createShoppingListResponseHandler;

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

        setupToolbar();

        setupActionModeCallback();

        setupRecyclerView();

        setupAddProductsButtonListener();

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

        // Needed to show the menu action
        setHasOptionsMenu(true);

        return loadedFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Load menu
        getActivity().getMenuInflater().inflate(R.menu.shopping_list_confirm_menu, menu);

        // Add listener to menu action
        MenuItem addProductsItem = menu.findItem(R.id.confirmListCreationButton);
        addProductsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                sendShoppingListCreationRequest();
                return false;
            }
        });
    }

    private void setupToolbar()
    {
        createListToolbar.setTitle(getString(R.string.toolbar_title));
        createListToolbar.setTitleTextColor(0xFFFFFFFF);

        // Needed to show the menu action
        ((AppCompatActivity)getActivity()).setSupportActionBar(createListToolbar);
    }

    private void initializeWidgets(View loadedFragment)
    {
        createListToolbar = (Toolbar) loadedFragment.findViewById(R.id.shoppingListToolbar);
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewShopProducts);
        addProductToListButton = (FloatingActionButton) loadedFragment.findViewById(R.id.addProductToListButton);
    }

    private void setupRecyclerView()
    {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.LIST_MODE, this);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }


    // Populate product list with the products of the shopping list
    private void populateProductList()
    {
        // Get the shopping list from cache
        shoppingList = GlobalValuesManager.getInstance(getContext()).getUserShoppingList();

        // Get the product list
        productModelList = shoppingList.getProductList();

        // Initialize the final set
        finalProductSet.addAll(productModelList);

        // Set the adapter model
        adapter.replaceAll(productModelList);
    }

    private void setupCreateShoppingListRequest()
    {
        this.createShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(getContext(), "OK", Toast.LENGTH_LONG).show();
                        GlobalValuesManager.getInstance(getContext()).updateShoppingList(finalProductSet);
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

    private void sendShoppingListCreationRequest()
    {
        // Final set as list
        List<Product> finalProductList = new ArrayList<>(finalProductSet);

        // JSON POST parameters
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("groupID", GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID());
            jsonParams.put("products", Product.asJSONProductList(finalProductList));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("CREATE_LIST_PARAM", jsonParams.toString());

        setupCreateShoppingListRequest();

        ShoppingListDatabaseHelper.sendCreateShoppingListRequest(jsonParams, getContext(), createShoppingListResponseHandler);

    }

    private void setupAddProductsButtonListener()
    {
        addProductToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddProductsActivityForResult();
            }
        });
    }

    private void startAddProductsActivityForResult()
    {
        /*
            Start the add product activity for result, passing the current products in the template
            This way the user won't see products already in the template in the add activity.
         */

        // Update cached template with deleted products
        // GlobalValuesManager.getInstance(getContext()).removeTemplateProducts(template.getID(), adapter.getDeleteList());

        // Take note of the deleted products before changing activity
        // deleteSet.addAll(adapter.getDeleteList());

        // Get the current list from adapter
        List<Product> currentList = adapter.getModelAsCollection();

        // Convert to JSON
        JSONArray jsonCurrentList = Product.asJSONProductList(currentList);

        // Create intent
        Intent intent = new Intent(getContext(), AddProductsActivity.class);
        intent.putExtra("CURRENT_PRODUCTS", jsonCurrentList.toString());

        // Start activity for result
        startActivityForResult(intent, ADD_PRODUCTS_REQUEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ADD_PRODUCTS_REQUEST)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                try {
                    // Get added products from result and add them to the final set
                    List<Product> productsAdded = Product.parseJSONProductList(new JSONArray(data.getStringExtra("RESULT")));
                    for(Product product : productsAdded)
                    {
                        product.setQuantity(1);
                    }
                    finalProductSet.addAll(productsAdded);

                    // Update adapter
                    adapter.add(productsAdded);
                    adapter.notifyDataSetChanged();

                    // Update cached template
                    // GlobalValuesManager.getInstance(getApplicationContext()).addTemplateProducts(template.getID(), addSet);

                    // Remove from the delete set the products just inserted
                    // adapter.getDeleteList().removeAll(addSet);
                    // deleteSet.removeAll(addSet);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
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
                    // sendDeleteTemplatesRequest();
                    // selectedListItems.addAll(adapter.getSelectedItems());
                    List<Product> deletedProducts = getActionModeSelectedProducts(adapter.getSelectedItems());
                    finalProductSet.removeAll(deletedProducts);
                    adapter.removeItems(adapter.getSelectedItems());
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

    private List<Product> getActionModeSelectedProducts(List<Integer> selectedIndexes)
    {
        List<Product> selectedProducts = new ArrayList<>();
        for(int i = 0; i < selectedIndexes.size(); i++)
        {
            selectedProducts.add(adapter.getModelAsCollection().get(selectedIndexes.get(i)));
        }
        return selectedProducts;
    }
}
