package com.mobile.paolo.listaspesa.view.home.template;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.remote.ProductsDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * -- AddTemplateProductsActivity --
 * Lets the user add products to a template.
 * It's called by EditTemplateActivity using startActivityForResult.
 * The result returned is the list of the products checked in this view.
 */

public class AddProductsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    // Widgets
    private Toolbar toolbar;
    private FloatingActionButton confirmAddProductsButton;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private List<Product> allProductsList = new ArrayList<>();

    // Network response handlers
    private NetworkResponseHandler fetchProductsResponseHandler;

    // The list of products to add
    private List<Product> addList = new ArrayList<>();

    // The products already present in the template
    private List<Product> currentProductList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_template_products);

        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        initializeWidgets();

        setupToolbar();

        getCurrentProductListFromExtras();

        setupRecyclerView();

        setupConfirmAddProductButtonListener();

        setupFetchProductsResponseHandler();

        ProductsDatabaseHelper.sendGetAllProductsRequest(null, getApplicationContext(), fetchProductsResponseHandler);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate menu
        getMenuInflater().inflate(R.menu.search_action_menu, menu);

        // Get the search bar
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Setup hint, width and listener
        searchView.setQueryHint(getString(R.string.action_search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<Product> filteredModelList = filter(allProductsList, query);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_slide_up, R.anim.back_slide_down);
    }

    private void initializeWidgets()
    {
        toolbar = (Toolbar) findViewById(R.id.addTemplateProductsToolbar);
        confirmAddProductsButton = (FloatingActionButton) findViewById(R.id.confirmAddProductsButton);

    }

    private void setupToolbar()
    {
        toolbar.setTitle(getString(R.string.add_template_products_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);
    }

    private void getCurrentProductListFromExtras()
    {
        try {
            currentProductList = Product.parseJSONProductList(new JSONArray(getIntent().getExtras().getString("CURRENT_PRODUCTS")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private void setupRecyclerView()
    {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewProducts);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.ADD_MODE);

        // set the adapter object to the RecyclerView
        recyclerView.setAdapter(adapter);
    }

    private void populateProductList(JSONArray jsonProducts)
    {
        try {
            for(int i = 0; i < jsonProducts.length(); i++)
            {
                Product product = Product.fromJSON((JSONObject) jsonProducts.get(i));
                allProductsList.add(product);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        // Show only product not already in the template
        allProductsList.removeAll(currentProductList);
        adapter.replaceAll(allProductsList);
    }

    private void setupConfirmAddProductButtonListener()
    {
        confirmAddProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCheckedProducts();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("RESULT", Product.asJSONProductList(addList).toString());
                setResult(Activity.RESULT_OK, returnIntent);
                //finish();
                onBackPressed();
            }
        });
    }

    private void addCheckedProducts()
    {
        List<Product> productList = adapter.getModelAsCollection();
        for(int i = 0; i < productList.size(); i++)
        {
            if(productList.get(i).isChecked())
            {
                addList.add(productList.get(i));
            }
        }
    }
}
