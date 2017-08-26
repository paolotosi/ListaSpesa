package com.mobile.paolo.listaspesa.view.home.template;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import com.mobile.paolo.listaspesa.database.remote.ProductsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.ProductCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CreateTemplateFragment extends Fragment implements SearchView.OnQueryTextListener {

    // Widgets
    private TextInputLayout templateNameInputLayout;
    private TextInputEditText templateNameField;
    private FloatingActionButton confirmTemplateCreationButton;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private ProductCardViewDataAdapter adapter;
    private List<Product> productList = new ArrayList<>();

    // Network response handlers
    private NetworkResponseHandler fetchProductsResponseHandler;
    private NetworkResponseHandler createTemplateResponseHandler;

    // The template
    private Template createdTemplate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_create_template, container, false);

        setHasOptionsMenu(true);

        setupToolbar(loadedFragment);

        initializeWidgets(loadedFragment);

        setupRecyclerView(loadedFragment);

        setupConfirmTemplateCreationButtonListener();

        setupFetchProductsResponseHandler();

        JSONObject jsonID = new JSONObject();
        try {
            jsonID.put("id", String.valueOf(GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID()));
            Log.d("GroupID", jsonID.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProductsDatabaseHelper.sendGetAllProductsRequest(jsonID, getContext(), fetchProductsResponseHandler);

        return loadedFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate menu
        getActivity().getMenuInflater().inflate(R.menu.search_action_menu, menu);

        // Get the search bar
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Setup hint, width and listener
        searchView.setQueryHint(getString(R.string.action_search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);

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
        Log.d("setupFetchProducts", "Eccomi");
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
        recyclerView.setHasFixedSize(false);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new ProductCardViewDataAdapter(ProductCardViewDataAdapter.ADD_MODE);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }

    private void populateProductList(JSONArray jsonProducts)
    {
        try {
            for(int i = 0; i < jsonProducts.length(); i++)
            {
                Product product = Product.fromJSON((JSONObject) jsonProducts.get(i));
                productList.add(product);
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

    private void setupCreateTemplateResponseHandler()
    {
        this.createTemplateResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("CREATE_TEMPLATE_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(getContext(), "Template creato con successo", Toast.LENGTH_LONG).show();
                        // Save the created template
                        int templateID = response.getInt("templateID");
                        createdTemplate.setID(templateID);
                        GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingTemplate(false);
                        GlobalValuesManager.getInstance(getContext()).saveHasUserTemplates(true);
                        GlobalValuesManager.getInstance(getContext()).addTemplate(createdTemplate);
                        showManageTemplateFragment();
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

    private void setupConfirmTemplateCreationButtonListener()
    {

        confirmTemplateCreationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean okToSend = true;

                // Add all products in case the list is filtered
                adapter.replaceAll(productList);

                // Get the template name inserted
                String templateName = "";
                if(isInsertionValid())
                {
                    templateName = templateNameField.getText().toString();
                }
                else
                {
                    okToSend = false;
                }

                // Get the products
                SortedList<Product> sortedList = adapter.getModel();
                List<Product> checkedProducts = new ArrayList<>();

                for(int i = 0; i < sortedList.size(); i++)
                {
                    if(sortedList.get(i).isChecked())
                    {
                        Log.d("Product checked", sortedList.get(i).getName());
                        checkedProducts.add(sortedList.get(i));
                    }
                }

                if(checkedProducts.size() < 1)
                {
                    okToSend = false;
                    Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.template_creation_KO_no_products, Snackbar.LENGTH_LONG).show();
                }

                // Send the request
                if(okToSend)
                {
                    sendCreateTemplateRequest(templateName, checkedProducts);
                }
            }
        });
    }

    private void initializeWidgets(View loadedFragment)
    {
        templateNameInputLayout = (TextInputLayout) loadedFragment.findViewById(R.id.templateNameInputLayout);
        templateNameField = (TextInputEditText) loadedFragment.findViewById(R.id.templateNameField);
        confirmTemplateCreationButton = (FloatingActionButton) loadedFragment.findViewById(R.id.confirmTemplateCreationButton);
    }

    private boolean isInsertionValid()
    {
        boolean isValid = true;

        if(templateNameField.getText().toString().isEmpty())
        {
            isValid = false;
            templateNameInputLayout.setError(getString(R.string.template_creation_KO_no_name));
        }
        else
        {
            templateNameInputLayout.setErrorEnabled(false);
        }
        return isValid;
    }

    private void sendCreateTemplateRequest(String templateName, List<Product> checkedProducts)
    {
        // Get the group ID
        int loggedUserGroupID = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID();

        // Create JSON POST request
        JSONObject params = new JSONObject();
        try {
            params.put("templateName", templateName);
            params.put("groupID", ((Integer) loggedUserGroupID).toString());
            JSONArray products = new JSONArray();

            for(int i = 0; i < checkedProducts.size(); i++)
            {
                products.put(i, checkedProducts.get(i).toJSON());
            }
            params.put("products", products);

            // Create a template object for later
            createdTemplate = new Template(templateName, loggedUserGroupID, products);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("TEMPLATE_CREATE", params.toString());

        // Define what to do on server response
        setupCreateTemplateResponseHandler();

        // Send request
        TemplatesDatabaseHelper.sendCreateTemplateRequest(params, getContext(), createTemplateResponseHandler);

    }

    private void showManageTemplateFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageTemplateFragment());
        transaction.commit();
    }


}
