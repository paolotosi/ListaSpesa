package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import com.mobile.paolo.listaspesa.database.local.ProductsLocalDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.ShoppingListDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.HomeFragmentContainer;
import com.mobile.paolo.listaspesa.view.home.template.AddProductsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    // Action mode
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    // Network response logic
    private NetworkResponseHandler createShoppingListResponseHandler;
    private NetworkResponseHandler stateShoppingListResponseHandler;
    private NetworkResponseHandler deleteShoppingListResponseHandler;

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

        setHasOptionsMenu(true);

        initializeWidgets(loadedFragment);

        setupToolbar();

        setupActionModeCallback();

        setupRecyclerView();

        setupAddProductsButtonListener();

        populateProductList();

        // Needed to show the menu action
        setHasOptionsMenu(true);

        return loadedFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Load menu
        getActivity().getMenuInflater().inflate(R.menu.shopping_list_menu, menu);

        // Add listener to 'Confirm' menu action
        MenuItem addProductsItem = menu.findItem(R.id.confirmListCreationButton);
        addProductsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                sendShoppingListCreationRequest();
                return false;
            }
        });

        // Add listener to 'Take in charge' menu action
        MenuItem takeInCharge = menu.findItem(R.id.takeListInCharge);
        takeInCharge.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                sendTakeListInChargeRequest();
                return false;
            }
        });

        // Add listener to 'Delete' menu action
        MenuItem deleteList = menu.findItem(R.id.deleteList);
        deleteList.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showDeleteListAlertDialog();
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

    private void sendDeleteShoppingListRequest()
    {
        int groupID = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID();

        // JSON POST parameters
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("groupID", groupID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("DELETE_REQ", jsonParams.toString());

        // Define what to do on response
        setupDeleteShoppingListResponseHandler();

        // Send request
        ShoppingListDatabaseHelper.sendDeleteShoppingListRequest(jsonParams, getContext(), deleteShoppingListResponseHandler);
    }

    private void setupDeleteShoppingListResponseHandler()
    {
        this.deleteShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("DELETE_RESP", response.toString());
                    if(response.getInt("success") == 1)
                    {
                        // Update cache
                        GlobalValuesManager.getInstance(getContext()).saveHasUserShoppingList(false);
                        GlobalValuesManager.getInstance(getContext()).deleteShoppingList();

                        // Change fragment: show EmptyShoppingListFragment
                        FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getEmptyShoppingListFragment());
                        transaction.commit();
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

    private void showDeleteListAlertDialog()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog);

        dialogBuilder.setMessage(getString(R.string.delete_list_dialog));
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                getString(R.string.delete_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send delete request
                        sendDeleteShoppingListRequest();
                    }
                });

        dialogBuilder.setNegativeButton(
                getString(R.string.cancel_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActivity().getColor(R.color.materialRed500));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getActivity().getColor(R.color.materialGrey600));
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

        // Set the adapter model
        adapter.replaceAll(productModelList);
    }

    private void sendTakeListInChargeRequest()
    {
        GlobalValuesManager gvm = GlobalValuesManager.getInstance(getContext());
        int userID = gvm.getLoggedUser().getID();
        int groupID = gvm.getLoggedUserGroup().getID();

        // JSON POST parameters
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("userID", userID);
            jsonParams.put("groupID", groupID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("TAKE_CHARGE_REQ", jsonParams.toString());

        // Define what to do on response
        setupShoppingListStateUpdateResponseHandler();

        // Send the request
        ShoppingListDatabaseHelper.shoppingListStateUpdate(jsonParams, getContext(), stateShoppingListResponseHandler);
    }

    private void setupShoppingListStateUpdateResponseHandler()
    {
        this.stateShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("TAKE_CHARGE_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        saveShoppingListInLocalDatabase();

                        GlobalValuesManager.getInstance(getContext()).setShoppingListState(true);

                        // Change fragment: show GroceryStoreFragment
                        FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getGroceryStoreFragment());
                        transaction.commit();
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

    private void saveShoppingListInLocalDatabase()
    {
        List<Product> productListToSave = adapter.getModelAsCollection();
        ProductsLocalDatabaseHelper localDatabaseHelper = ProductsLocalDatabaseHelper.getInstance(getContext());
        localDatabaseHelper.open();
        localDatabaseHelper.resetAllProducts();
        for(Product product : productListToSave)
        {
            localDatabaseHelper.insertProduct(product);
        }
        localDatabaseHelper.close();
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
                        GlobalValuesManager.getInstance(getContext()).updateShoppingList(adapter.getModelAsCollection());
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
        // Get the products from the adapter
        List<Product> finalProductList = adapter.getModelAsCollection();

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
                    // Get added products from result
                    List<Product> productsAdded = Product.parseJSONProductList(new JSONArray(data.getStringExtra("RESULT")));

                    // For each of them, set the quantity to 1
                    for(Product product : productsAdded)
                    {
                        product.setQuantity(1);
                    }

                    // Update adapter list
                    adapter.add(productsAdded);
                    adapter.notifyDataSetChanged();

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
}
