package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.local.ProductsLocalDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.ShoppingListDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;
import com.mobile.paolo.listaspesa.view.home.template.AddProductsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
Shows the shopping list of the group and provides operations to manipulate the shopping list
 or to change his state from "in preparazione" to "in acquisto"
 */
public class ManageShoppingListFragment extends Fragment implements ProductCardViewDataAdapter.ViewHolder.ClickListener
{

    // Constants
    private static final int ADD_PRODUCTS_REQUEST = 1;
    private static final boolean TAKEN_CHARGE = true;
    private static final boolean NOT_TAKEN_CHARGE = false;
    private static final boolean SHOW_SAVE_FEEDBACK = true;
    private static final boolean HIDE_SAVE_FEEDBACK = false;

    private List<Product> initialProductList;

    // Widgets
    private FloatingActionButton addProductToListButton;
    private Toolbar createListToolbar;
    private SwipeRefreshLayout refreshShoppingListLayout;
    private TextView emptyTextView;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;

    // Action mode
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    // Network response logic
    private NetworkResponseHandler createShoppingListResponseHandler;
    private NetworkResponseHandler refreshShoppingListResponseHandler;
    private NetworkResponseHandler takeShoppingListResponseHandler;
    private NetworkResponseHandler deleteShoppingListResponseHandler;

    // This boolean is needed to avoid showing save feedback when adding products
    private boolean addingProducts = false;

    // Avoid saving list on exit if the list has been deleted
    private boolean listDeleted = false;

    // Avoid saving list on exit if the list has been taken
    private boolean listTaken = false;

    private MenuItem takeInCharge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_manage_shopping_list, container, false);

        listDeleted = false;

        listTaken = false;

        initializeWidgets(loadedFragment);

        setupToolbar();

        setupSwipeToRefresh();

        setupActionModeCallback();

        setupRecyclerView();

        setupAddProductsButtonListener();

        populateProductList();

        // Create list on the database after selecting a template
        sendShoppingListCreationRequest(HIDE_SAVE_FEEDBACK);

        // Needed to show the menu action
        setHasOptionsMenu(true);

        return loadedFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Load menu
        getActivity().getMenuInflater().inflate(R.menu.shopping_list_menu, menu);

        // Add listener to 'Take in charge' menu action
        takeInCharge = menu.findItem(R.id.takeListInCharge);
        takeInCharge.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                sendTakeListInChargeRequest();
                return false;
            }
        });
        if(GlobalValuesManager.getInstance(getContext()).getShoppingListState().equals(GlobalValuesManager.EMPTY_LIST) || adapter.getItemCount() == 0) {
            takeInCharge.setVisible(false);
        }
        else {
            takeInCharge.setVisible(true);
        }
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

    @Override
    public void onPause() {
        if(!listDeleted && !listTaken)
        {
            sendShoppingListCreationRequest(SHOW_SAVE_FEEDBACK);
        }
        if(this.actionMode != null)
        {
            actionMode.finish();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        showListControlsBasedOnState();
        super.onResume();
    }


    private void showListControlsBasedOnState()
    {
        Log.d("State before saving", GlobalValuesManager.getInstance(getContext()).getShoppingListState());
        if(GlobalValuesManager.getInstance(getContext()).getShoppingListState().equals(GlobalValuesManager.LIST_NO_CHARGE) || adapter.getItemCount() > 0)
        {
            // Hide message and show 'take in charge' action
            emptyTextView.setVisibility(View.GONE);
            if(takeInCharge != null) {
                takeInCharge.setVisible(true);
            }

        }
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
        refreshShoppingListLayout = (SwipeRefreshLayout) loadedFragment.findViewById(R.id.refreshShoppingListLayout);
        addProductToListButton = (FloatingActionButton) loadedFragment.findViewById(R.id.addProductToListButton);
        emptyTextView = (TextView) loadedFragment.findViewById(R.id.emptyList);
    }

    private void setupSwipeToRefresh()
    {
        refreshShoppingListLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendRefreshShoppingListRequest();
            }
        });
    }

    private void setupRefreshShoppingListResponseHandler()
    {
        this.refreshShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("REFRESH_LIST_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        // No one has taken the list
                        JSONObject jsonShoppingList = response.getJSONObject("list");
                        ShoppingList shoppingList = ShoppingList.fromJSON(jsonShoppingList);
                        Log.d("State",GlobalValuesManager.getInstance(getContext()).getShoppingListState());
                        if(shoppingList.getProductList().size() == 0 && !GlobalValuesManager.getInstance(getContext()).getShoppingListState().equals(GlobalValuesManager.EMPTY_LIST))
                        {
                            GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.NO_LIST);
                        }
                        else if(!GlobalValuesManager.getInstance(getContext()).getShoppingListState().equals(GlobalValuesManager.EMPTY_LIST))
                        {
                            GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.LIST_NO_CHARGE);
                        }
                        adapter.deleteAllProducts();
                        adapter.add(shoppingList.getProductList());
                        deleteListIfEmpty();
                        refreshShoppingListLayout.setRefreshing(false);
                    }
                    else if(response.getInt("success") == 2)
                    {
                        // Someone has taken the list
                        GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.LIST_IN_CHARGE_ANOTHER_USER);
                        JSONObject jsonShoppingList = response.getJSONObject("list");
                        ShoppingList shoppingList = ShoppingList.fromJSON(jsonShoppingList);
                        adapter.deleteAllProducts();
                        adapter.add(shoppingList.getProductList());
                        deleteListIfEmpty();
                        refreshShoppingListLayout.setRefreshing(false);
                        int userTookListID = response.getInt("userID");
                        String userTookList = "";
                        for(User user : GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getMembers())
                        {
                            if(user.getID() == userTookListID)
                            {
                                userTookList = user.getUsername();
                            }
                        }
                        GlobalValuesManager.getInstance(getContext()).saveUserTookList(userTookList);
                        Toast.makeText(getContext(), userTookList + " ha già preso in carico la lista.", Toast.LENGTH_LONG).show();

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

    private void sendRefreshShoppingListRequest()
    {
        // Get group ID
        Integer groupID = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID();

        // JSON post parameters
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("groupID", groupID.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("REFRESH_LIST_REQ", jsonParams.toString());

        // Define what to do on response
        setupRefreshShoppingListResponseHandler();

        // Send the request
        ShoppingListDatabaseHelper.sendGetGroupListRequest(jsonParams, getContext(), refreshShoppingListResponseHandler);
    }

    private void sendDeleteShoppingListRequest(boolean takingCharge)
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
        setupDeleteShoppingListResponseHandler(takingCharge);

        // Send request
        ShoppingListDatabaseHelper.sendDeleteShoppingListRequest(jsonParams, getContext(), deleteShoppingListResponseHandler);
    }

    private void setupDeleteShoppingListResponseHandler(final boolean takingCharge)
    {
        this.deleteShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("DELETE_RESP", response.toString());
                    if(response.getInt("success") == 1)
                    {
                        listDeleted = true;
                        if(!takingCharge)
                        {
                            // Update cache
                            GlobalValuesManager.getInstance(getContext()).deleteShoppingList();
                            Log.d("State", GlobalValuesManager.getInstance(getContext()).getShoppingListState());
                            if(GlobalValuesManager.getInstance(getContext()).getShoppingListState().equalsIgnoreCase(GlobalValuesManager.LIST_NO_CHARGE) || GlobalValuesManager.getInstance(getContext()).getShoppingListState().equalsIgnoreCase(GlobalValuesManager.EMPTY_LIST))
                            {
                                GlobalValuesManager.getInstance(getContext()).saveHasUserShoppingList(false);
                                GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.NO_LIST);
                            }
                            else
                            {
                                GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.LIST_IN_CHARGE_ANOTHER_USER);
                            }

                            // Change fragment: show EmptyShoppingListFragment
                            FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getEmptyShoppingListFragment());
                            transaction.commit();
                        }
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

    private void deleteListIfEmpty()
    {
        if(adapter.getItemCount() == 0 && !GlobalValuesManager.getInstance(getContext()).getShoppingListState().equals(GlobalValuesManager.EMPTY_LIST))
        {
            sendDeleteShoppingListRequest(NOT_TAKEN_CHARGE);
        }
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
                        sendDeleteShoppingListRequest(NOT_TAKEN_CHARGE);
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
        ShoppingList shoppingList = GlobalValuesManager.getInstance(getContext()).getUserShoppingList();
        if(shoppingList.getProductList().size() < 1 && !GlobalValuesManager.getInstance(getContext()).areThereProductsNotFound())
        {
            emptyTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            // Save the initial the product list
            initialProductList = shoppingList.getProductList();
            emptyTextView.setVisibility(View.GONE);
            // Set the adapter model
            if(GlobalValuesManager.getInstance(getContext()).areThereProductsNotFound())
            {
                for(int i = 0; i < GlobalValuesManager.getInstance(getContext()).getProductsNotFound().size(); i++)
                {

                    initialProductList.add(GlobalValuesManager.getInstance(getContext()).getProductsNotFound().get(i));
                }
                // Delete products not found
                GlobalValuesManager.getInstance(getContext()).saveProductsNotFound(new JSONArray());
            }
            Log.d("Lista in populateUser", initialProductList.toString());
            adapter.replaceAll(initialProductList);
        }

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
        setupTakeShoppingListResponseHandler();

        // Send the request
        ShoppingListDatabaseHelper.sendTakeShoppingListRequest(jsonParams, getContext(), takeShoppingListResponseHandler);
    }

    private void setupTakeShoppingListResponseHandler()
    {
        this.takeShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("TAKE_CHARGE_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        listTaken = true;

                        // Save the list locally
                        saveShoppingListInLocalDatabase();

                        // Delete the list on the server
                        sendDeleteShoppingListRequest(TAKEN_CHARGE);

                        // Update cache
                        GlobalValuesManager.getInstance(getContext()).updateShoppingListProducts(new ArrayList<Product>());
                        GlobalValuesManager.getInstance(getContext()).saveShoppingListTaken(true);
                        GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.LIST_IN_CHARGE_LOGGED_USER);

                        // Change fragment: show GroceryStoreFragment after resetting it
                        HomeFragmentContainer.getInstance().resetGroceryStoreFragment();
                        FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getGroceryStoreFragment());
                        transaction.commit();
                    }
                    else
                    {
                        // Somebody has already taken list
                        sendRefreshShoppingListRequest();
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

    private void setupCreateShoppingListRequest(final Context context, final boolean showFeedback)
    {
        // Context is needed because the response arrives after the fragment has been changed
        this.createShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("CREATE_LIST_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        if(showFeedback && !addingProducts)
                        {
                            Toast.makeText(context, context.getString(R.string.list_saved), Toast.LENGTH_SHORT).show();
                        }

                        List<Product> productList =  adapter.getModelAsCollection();
                        if(productList == null)
                        {
                            productList = new ArrayList<>();
                        }

                        GlobalValuesManager.getInstance(context).updateShoppingListProducts(productList);
                        GlobalValuesManager.getInstance(context).saveAreThereProductsNotFound(false);
                        GlobalValuesManager.getInstance(context).saveProductsNotFound(new JSONArray());
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

    private void sendShoppingListCreationRequest(boolean showFeedback)
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


        Log.d("CREATE_LIST_REQ", jsonParams.toString());

        setupCreateShoppingListRequest(getContext(), showFeedback);

        ShoppingListDatabaseHelper.sendCreateShoppingListRequest(jsonParams, getContext(), createShoppingListResponseHandler);

    }

    private boolean productListChanged(List<Product> finalProductList)
    {
        // Sort the lists
        Collections.sort(initialProductList, Product.ALPHABETICAL_COMPARATOR);
        Collections.sort(finalProductList, Product.ALPHABETICAL_COMPARATOR);

        // Check sizes
        if(finalProductList.size() != initialProductList.size())
        {
            return true;
        }
        else
        {
            for(int i = 0; i < finalProductList.size(); i++)
            {
                if(!finalProductList.get(i).getName().equalsIgnoreCase(initialProductList.get(i).getName())) return true;
                if(!finalProductList.get(i).getBrand().equalsIgnoreCase(initialProductList.get(i).getBrand())) return true;
                if(finalProductList.get(i).getQuantity() != initialProductList.get(i).getQuantity()) return true;
            }
            return false;
        }
    }

    private void setupAddProductsButtonListener()
    {
        addProductToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addingProducts = true;
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

        addingProducts = false;

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

                    // Save new list on the db
                    sendShoppingListCreationRequest(SHOW_SAVE_FEEDBACK);

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
                    if(adapter.getItemCount() == 0 && !GlobalValuesManager.getInstance(getContext()).getShoppingListState().equals(GlobalValuesManager.EMPTY_LIST))
                    {
                        deleteListIfEmpty();
                    }
                    else
                    {
                        sendShoppingListCreationRequest(SHOW_SAVE_FEEDBACK);
                    }
                    mode.finish();
                    adapter.notifyDataSetChanged();
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
