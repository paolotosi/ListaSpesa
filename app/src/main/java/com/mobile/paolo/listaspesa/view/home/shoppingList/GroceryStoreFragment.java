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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.local.ProductsLocalDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.ShoppingListDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.HomeFragmentContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroceryStoreFragment extends android.support.v4.app.Fragment {

    // Constants
    private static final int SELECT_MARKET_REQUEST = 1;
    private static final boolean PRODUCTS_LEFT = true;
    private static final boolean NO_PRODUCTS_LEFT = false;

    // Widgets
    private LinearLayout imageLayout;
    private ImageView listImage;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                putProductsInShoppingCart();
                return false;
            }
        });

        // Add listener to 'End shopping' action
        endShoppingMenuAction = menu.findItem(R.id.confirmEndShoppingButton);
        endShoppingMenuAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showEndShoppingWithProductsAlertDialog();
                return false;
            }
        });
    }

    private void initializeWidgets(View loadedFragment)
    {
        groceryToolbar = (Toolbar) loadedFragment.findViewById(R.id.groceryToolbar);
        imageLayout = (LinearLayout) loadedFragment.findViewById(R.id.imageLayout);
        listImage = (ImageView) loadedFragment.findViewById(R.id.listImage);
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
        // Define the ArrayAdapter overriding methods in order to show a hint
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

    private void readProductsFromLocalDatabase()
    {
        ProductsLocalDatabaseHelper localDatabaseHelper = ProductsLocalDatabaseHelper.getInstance(getContext());
        localDatabaseHelper.open();
        groceryList = localDatabaseHelper.getAllProducts();
        localDatabaseHelper.close();
    }

    private void putProductsInShoppingCart()
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

    private void endShopping()
    {
        // Save remaining products on the remote db
        sendCompleteShoppingListRequest(PRODUCTS_LEFT);

        // Save remaining products in the cache
        GlobalValuesManager.getInstance(getContext()).saveAreThereProductsNotFound(true);
        GlobalValuesManager.getInstance(getContext()).saveProductsNotFound(Product.asJSONProductList(adapter.getModelAsCollection()));

        // Update state
        setupCheckNewListResponseHandler();

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
        ShoppingListDatabaseHelper.sendGetGroupListRequest(jsonPostParameters, getContext(), newListCheckerResponseHandler);
    }


    private void setupCheckNewListResponseHandler()
    {
        this.newListCheckerResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("CHECK_NEW_LIST_RESP", response.toString());
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
                            GlobalValuesManager.getInstance(getContext()).saveHasUserShoppingList(true);
                            GlobalValuesManager.getInstance(getContext()).saveShoppingListState(GlobalValuesManager.LIST_NO_CHARGE);
                            GlobalValuesManager.getInstance(getContext()).saveUserShoppingList(shoppingList.toJSON());
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

    private void animateOnListCompletion()
    {
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


}
