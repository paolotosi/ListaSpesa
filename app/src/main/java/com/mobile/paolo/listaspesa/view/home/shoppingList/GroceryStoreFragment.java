package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

public class GroceryStoreFragment extends android.support.v4.app.Fragment {

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;

    private Toolbar groceryToolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_grocery_store, container, false);

        setHasOptionsMenu(true);

        groceryToolbar =(Toolbar) loadedFragment.findViewById(R.id.groceryToolbar);

        setupToolbar();

        setupRecyclerView(loadedFragment);

        return loadedFragment;
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

        ShoppingList shoppingList = GlobalValuesManager.getInstance(getContext()).getUserShoppingList();
        adapter.replaceAll(shoppingList.getProductList());

    }

    private void setupToolbar()
    {
        groceryToolbar.setTitle("Fai la spesa!");
        groceryToolbar.setTitleTextColor(0xFFFFFFFF);

        // Needed to show the menu action
        ((AppCompatActivity)getActivity()).setSupportActionBar(groceryToolbar);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Load menu
        getActivity().getMenuInflater().inflate(R.menu.grocery_menu, menu);

        // Add listener to confirm menu action
        MenuItem purchaseSelectedItem = menu.findItem(R.id.confirmProductButton);
        purchaseSelectedItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO eliminazione dei prodotti checkati;
                return false;
            }
        });

        // Add listener to confirm menu action
        MenuItem endShopping = menu.findItem(R.id.confirmEndShoppingButton);
        endShopping.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO termine della lista della spesa e gestione dei prodotti non trovati;
                return false;
            }
        });
    }
}
