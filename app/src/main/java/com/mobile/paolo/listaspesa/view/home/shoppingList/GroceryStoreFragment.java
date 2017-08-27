package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.local.ProductsLocalDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.ShoppingListDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.SupermarketDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * -- GroceryStoreFragment --
 * This fragment is shown when a user takes the list in charge and goes to the supermarket.
 * It shows the list of products to buy with the quantity.
 * Each product has a checkbox: when the user find a product in the store and puts it in the
 * cart, it checks it and taps the 'Add to cart' button. When all the products have been taken,
 * the shopping mode end automatically.
 * The user can end this mode at any moment, pressing the 'End shopping' button. Products not
 * in cart will be saved and put in the next shopping list.
 * It is required to select the supermarket in which the shopping is done (the selection can be
 * performed in the MarketMapActivity called with the map button); the app will take note of which
 * products can be found in which supermarket.
 */

public class GroceryStoreFragment extends android.support.v4.app.Fragment {

    // Constants
    private static final int SELECT_MARKET_REQUEST = 1;
    private static final boolean PRODUCTS_LEFT = true;
    private static final boolean NO_PRODUCTS_LEFT = false;

    // Widgets
    private LinearLayout imageLayout;
    private Button finishButton;
    private Toolbar groceryToolbar;
    private Spinner supermarketSpinner;
    private MenuItem showMarketMapMenuAction;
    private MenuItem putInCartMenuAction;
    private MenuItem endShoppingMenuAction;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;

    // Products and supermarket lists
    private List<Product> groceryList = new ArrayList<>();
    private List<Supermarket> supermarketList = new ArrayList<>();

    // Complete list response handler
    private NetworkResponseHandler completeShoppingListResponseHandler;
    private NetworkResponseHandler newListCheckerResponseHandler;
    private NetworkResponseHandler saveSupermarketProductsResponseHandler;

    // A boolean to inform the user that products not found have been added to the new list
    private boolean productsLeft = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_grocery_store, container, false);

        setHasOptionsMenu(true);

        initializeWidgets(loadedFragment);

        setupToolbar();

        setupSupermarketSpinner();

        setupFinishButtonListener();

        readProductsFromLocalDatabase();

        setupRecyclerView(loadedFragment);

        return loadedFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Load menu
        getActivity().getMenuInflater().inflate(R.menu.grocery_menu, menu);

        // Add listener to 'Show Supermarket Map' menu action
        showMarketMapMenuAction = menu.findItem(R.id.showMapButton);
        showMarketMapMenuAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showMarketMapActivity();
                return false;
            }
        });

        // Add listener to 'Put in shopping cart' menu action
        putInCartMenuAction = menu.findItem(R.id.confirmProductButton);
        putInCartMenuAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(supermarketSelected())
                {
                    putProductsInShoppingCart();
                }
                return false;
            }
        });

        // Add listener to 'End shopping' action
        endShoppingMenuAction = menu.findItem(R.id.confirmEndShoppingButton);
        endShoppingMenuAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(supermarketSelected())
                {
                    showEndShoppingWithProductsAlertDialog();
                }
                return false;
            }
        });
    }

    private void initializeWidgets(View loadedFragment)
    {
        groceryToolbar = (Toolbar) loadedFragment.findViewById(R.id.groceryToolbar);
        imageLayout = (LinearLayout) loadedFragment.findViewById(R.id.imageLayout);
        finishButton = (Button) loadedFragment.findViewById(R.id.finishButton);
        supermarketSpinner = (Spinner) loadedFragment.findViewById(R.id.supermarketSpinner);
    }

    private void setupToolbar()
    {
        groceryToolbar.setTitle(getString(R.string.grocery_toolbar_title));
        groceryToolbar.setTitleTextColor(0xFFFFFFFF);

        // Needed to show the menu action
        ((AppCompatActivity)getActivity()).setSupportActionBar(groceryToolbar);

    }

    private void setupSupermarketSpinner()
    {
        // Define the ArrayAdapter overriding methods in order to show a non-selectable hint
        ArrayAdapter<Supermarket> supermarketArrayAdapter = new ArrayAdapter<Supermarket>(getContext(), R.layout.spinner_item){
            @Override
            public boolean isEnabled(int position){
                return position != 0; // all position are enabled except the first one, used by the hint
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Get the supermarkets from cache, add the hint
        supermarketList = GlobalValuesManager.getInstance(getContext()).getSupermarkets();
        Supermarket hint = new Supermarket(-1, "Seleziona un supermercato...", "", null);
        supermarketList.add(0, hint);

        // Add supermarkets to adapter
        supermarketArrayAdapter.addAll(supermarketList);

        // Set the adapter
        supermarketSpinner.setAdapter(supermarketArrayAdapter);
    }

    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewGrocery);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.GROCERY_MODE);

        // set the adapter object to the RecyclerView
        recyclerView.setAdapter(adapter);

        adapter.replaceAll(groceryList);

    }

    private void readProductsFromLocalDatabase()
    {
        ProductsLocalDatabaseHelper localDatabaseHelper = ProductsLocalDatabaseHelper.getInstance(getContext());
        localDatabaseHelper.open();
        groceryList = localDatabaseHelper.getAllProducts();
        localDatabaseHelper.close();
    }

    private void setupFinishButtonListener()
    {
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateListFragment();
            }
        });
    }

    private void showMarketMapActivity()
    {
        Intent intent = new Intent(getContext(), SupermarketMapActivity.class);
        startActivityForResult(intent, SELECT_MARKET_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_MARKET_REQUEST)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                try {
                    // Get supermarket from result
                    Supermarket selectedSupermarket = Supermarket.fromJSON(new JSONObject(data.getStringExtra("RESULT")));

                    // Find it and select it in the spinner list
                    for(int i = 0; i < supermarketSpinner.getAdapter().getCount(); i++)
                    {
                        if(((Supermarket) supermarketSpinner.getAdapter().getItem(i)).getID() == selectedSupermarket.getID())
                        {
                            supermarketSpinner.setSelection(i);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void showCreateListFragment()
    {
        // Change fragment: show CreateShoppingList
        FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getCreateShoppingListFragment());
        transaction.commit();

        // Update state
        GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingShoppingList(true);
    }

    private void showEmptyListFragment()
    {
        // Change fragment: show EmptyShoppingList
        FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getEmptyShoppingListFragment());
        transaction.commit();
    }

    private void showManagementFragment()
    {
        // Change fragment: show ManageShoppingList
        FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageShoppingListFragment());
        transaction.commit();
    }

    private void putProductsInShoppingCart()
    {
        if(adapter.getCheckedProducts().size() > 0)
        {
            // Remove from database
            ProductsLocalDatabaseHelper.getInstance(getContext()).open();
            ProductsLocalDatabaseHelper.getInstance(getContext()).deleteProducts(adapter.getCheckedProducts());
            ProductsLocalDatabaseHelper.getInstance(getContext()).close();

            // Remove from adapter
            adapter.remove(adapter.getCheckedProducts());

            // If the user has taken all products
            if(adapter.getItemCount() == 0)
            {
                // Hide menu actions
                showMarketMapMenuAction.setVisible(false);
                putInCartMenuAction.setVisible(false);
                endShoppingMenuAction.setVisible(false);

                // Show end button
                animateOnListCompletion();

                // Delete user charge from database
                sendCompleteShoppingListRequest(NO_PRODUCTS_LEFT);
            }
        }

        else
        {
            Toast.makeText(getContext(), "Seleziona i prodotti per poterli mettere nel carrello.", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateOnListCompletion()
    {
        supermarketSpinner.setVisibility(View.GONE);
        imageLayout.setVisibility(View.VISIBLE);

        int duration = 1000;

        ValueAnimator opacityAnimator = ValueAnimator.ofFloat(0f, 1f);
        opacityAnimator.setDuration(duration);
        opacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                imageLayout.setAlpha(((float) animation.getAnimatedValue()));
            }
        });
        opacityAnimator.start();
    }

    private void endShopping()
    {
        // User couldn't find all products
        productsLeft = true;

        // Save remaining products on the remote db or in the new list if the group has one
        sendCompleteShoppingListRequest(PRODUCTS_LEFT);

        // Save remaining products in the cache
        GlobalValuesManager.getInstance(getContext()).saveAreThereProductsNotFound(true);
        GlobalValuesManager.getInstance(getContext()).saveProductsNotFound(Product.asJSONProductList(adapter.getModelAsCollection()));
    }

    private void showEndShoppingWithProductsAlertDialog()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);

        dialogBuilder.setTitle(getString(R.string.end_shopping_title));
        dialogBuilder.setMessage(getString(R.string.end_shopping_dialog));
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                getString(R.string.end_shopping_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send delete request
                        endShopping();
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

    private void setupCheckNewListResponseHandler()
    {
        this.newListCheckerResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("GET_NEW_LIST_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        // -- Success --
                        // Update state
                        JSONObject jsonShoppingList = response.getJSONObject("list");
                        ShoppingList shoppingList = ShoppingList.fromJSON(jsonShoppingList);
                        if(shoppingList.getProductList().size() == 0)
                        {
                            // List is empty
                            GlobalValuesManager.getInstance(getContext()).saveHasUserShoppingList(false);
                            GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.NO_LIST);
                            showEmptyListFragment();
                        }
                        else
                        {
                            // List is not empty
                            Toast.makeText(getContext(), getString(R.string.list_completed_new_list), Toast.LENGTH_LONG).show();
                            if(productsLeft)
                            {
                                Toast.makeText(getContext(), getString(R.string.toast_list_with_old_products), Toast.LENGTH_LONG).show();
                            }
                            GlobalValuesManager.getInstance(getContext()).saveHasUserShoppingList(true);
                            GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.LIST_NO_CHARGE);
                            GlobalValuesManager.getInstance(getContext()).saveUserShoppingList(shoppingList.toJSON());
                            GlobalValuesManager.getInstance(getContext()).saveAreThereProductsNotFound(false);
                            GlobalValuesManager.getInstance(getContext()).saveProductsNotFound(new JSONArray());
                            showManagementFragment();
                        }
                    }
                    else
                    {
                        // -- Error --
                        Log.e("CHECK_LIST_ERR", response.getString("message"));
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

    private void sendGetNewListRequest()
    {
        Integer groupID = -1;

        if(GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup() != null)
        {
            groupID = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID();
        }

        // The POST parameters
        Map<String, String> params = new HashMap<>();
        params.put("groupID", groupID.toString());

        // Encapsulate in JSON
        JSONObject jsonPostParameters = new JSONObject(params);

        // Debug
        Log.d("GET_NEW_LIST_REQ", jsonPostParameters.toString());

        // Define what to do on response
        setupCheckNewListResponseHandler();

        ShoppingListDatabaseHelper.sendGetGroupListRequest(jsonPostParameters, getContext(), newListCheckerResponseHandler);
    }

    private void setupCompleteShoppingListResponseHandler()
    {
        this.completeShoppingListResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("COMPLETE_LIST_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        // -- Success --
                        // Update state
                        GlobalValuesManager.getInstance(getContext()).saveHasUserShoppingList(false);
                        GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.NO_LIST);

                        // Save products found in that supermarket on the remote database
                        sendSaveSupermarketProductsRequest();

                        // Check if the group has already defined another list
                        sendGetNewListRequest();
                    }
                    else
                    {
                        // -- Error --
                        Log.e("COMPLETE_LIST_ERR", response.getString("message"));
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

    private void sendCompleteShoppingListRequest(boolean productsLeft)
    {
        // Parameters
        int groupID = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID();
        List<Product> productsNotFound = new ArrayList<>();

        // JSON POST parameters
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("groupID", groupID);
            if(productsLeft)
            {
                productsNotFound = adapter.getModelAsCollection();
            }
            jsonParams.put("products", Product.asJSONProductList(productsNotFound));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("COMPLETE_LIST_REQ", jsonParams.toString());

        // Define what to do on response
        setupCompleteShoppingListResponseHandler();

        // Send request
        ShoppingListDatabaseHelper.sendCompleteShoppingListRequest(jsonParams, getContext(), completeShoppingListResponseHandler);
    }

    private void setupSaveSupermarketProductsResponseHandler()
    {
        this.saveSupermarketProductsResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("SAVE_MARKET_PROD_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {

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

    private void sendSaveSupermarketProductsRequest()
    {
        // Get selected supermarket ID
        int supermarketID = ((Supermarket) supermarketSpinner.getSelectedItem()).getID();

        // Get products found as groceryList \ productsNotFound
        List<Product> productsNotFound = adapter.getModelAsCollection();
        groceryList.removeAll(productsNotFound);

        // JSON params
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("groupID", GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID());
            jsonParams.put("supermarketID", supermarketID);
            jsonParams.put("products", Product.asJSONProductList(groceryList));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("SAVE_MARKET_PROD_REQ", jsonParams.toString());

        // Define what to do on response
        setupSaveSupermarketProductsResponseHandler();

        // Send request
        SupermarketDatabaseHelper.sendSaveSupermarketProductsRequest(jsonParams, getContext(), saveSupermarketProductsResponseHandler);

    }

    private boolean supermarketSelected()
    {
        Supermarket selectedSpinnerItem = ((Supermarket) supermarketSpinner.getSelectedItem());
        if(selectedSpinnerItem.getID() == -1)
        {
            // The hint is selected
            Toast.makeText(getContext(), getString(R.string.no_supermarket_selected_toast), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
