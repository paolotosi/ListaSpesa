package com.mobile.paolo.listaspesa.view.home.supermarket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.doctoror.geocoder.Address;
import com.doctoror.geocoder.Geocoder;
import com.doctoror.geocoder.GeocoderException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.remote.SupermarketDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by paolo on 26/08/17.
 */

public class CreateSupermarketFragment extends Fragment
{
    // Widgets
    private Toolbar createSupermarketToolbar;
    private TextInputLayout nameTextInputLayout;
    private TextInputLayout addressTextInputLayout;
    private TextInputEditText nameField;
    private TextInputEditText addressField;
    private MapView supermarketMapView;
    private FloatingActionButton confirmSupermarketCreationButton;
    private ImageView localizeMarket;

    // Google map
    private GoogleMap googleMap;

    // Geocoder AsyncTask
    private AddressResolutionTask addressResolutionTask;

    // NetworkResponseHandler
    NetworkResponseHandler addSupermarketResponseHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View loadedFragment = inflater.inflate(R.layout.fragment_create_supermarket, container, false);

        GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingSupermarket(true);

        initializeWidgets(loadedFragment);

        setupToolbar();

        setupLocalizeMarketButton();

        setupConfirmSupermarketCreationButton();

        loadMapView(savedInstanceState);

        // Otherwise the 'Up' button won't work
        setHasOptionsMenu(true);

        return loadedFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        supermarketMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        supermarketMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        supermarketMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        supermarketMapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingSupermarket(false);
            showManageSupermarketFragment();
        }
        return true;
    }

    private void returnBack()
    {
        // The previous fragment is saved in the stack
        getActivity().onBackPressed();
    }

    private void initializeWidgets(View loadedFragment)
    {
        this.createSupermarketToolbar = (Toolbar) loadedFragment.findViewById(R.id.createSupermarketToolbar);
        this.nameTextInputLayout = (TextInputLayout) loadedFragment.findViewById(R.id.nameTextInputLayout);
        this.addressTextInputLayout = (TextInputLayout) loadedFragment.findViewById(R.id.addressTextInputLayout);
        this.nameField = (TextInputEditText) loadedFragment.findViewById(R.id.nameField);
        this.addressField = (TextInputEditText) loadedFragment.findViewById(R.id.addressField);
        this.supermarketMapView = (MapView) loadedFragment.findViewById(R.id.supermarketMapView);
        this.confirmSupermarketCreationButton = (FloatingActionButton) loadedFragment.findViewById(R.id.confirmSupermarketCreationButton);
        this.localizeMarket = (ImageView) loadedFragment.findViewById(R.id.localizeMarket);
    }

    private void setupToolbar()
    {
        createSupermarketToolbar.setTitle(getString(R.string.add_supermarket_toolbar));
        createSupermarketToolbar.setTitleTextColor(0xFFFFFFFF);

        ((AppCompatActivity)getActivity()).setSupportActionBar(createSupermarketToolbar);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupLocalizeMarketButton()
    {
        this.localizeMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAddressValid())
                {
                    addressResolutionTask = new AddressResolutionTask(addressField.getText().toString());
                    addressResolutionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }

    private void setupConfirmSupermarketCreationButton()
    {
        this.confirmSupermarketCreationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInsertionValid())
                {
                    sendAddSupermarketRequest();
                }
            }
        });
    }

    private boolean isInsertionValid()
    {
        return isNameValid() && isAddressValid();
    }

    private boolean isNameValid()
    {
        boolean isNameValid = true;
        List<Supermarket> supermarketList = GlobalValuesManager.getInstance(getContext()).getSupermarkets();
        if(nameField.getText().toString().isEmpty())
        {
            isNameValid = false;
            nameTextInputLayout.setError(getString(R.string.empty_supermarket_name));
        }
        else
        {
            for(Supermarket supermarket : supermarketList)
            {
                // Check if the user already has a supermarket with that name
                if(supermarket.getName().equalsIgnoreCase(nameField.getText().toString()))
                {
                    isNameValid = false;
                    nameTextInputLayout.setError(getString(R.string.same_supermarket_name));
                }
            }
        }
        if(isNameValid)
        {
            // If isValid here is true, the name is ok
            nameTextInputLayout.setErrorEnabled(false);
        }
        return isNameValid;
    }

    private boolean isAddressValid()
    {
        boolean isAddressValid = true;

        if(addressField.getText().toString().isEmpty())
        {
            isAddressValid = false;
            addressTextInputLayout.setError(getString(R.string.empty_supermarket_address));
        }
        else
        {
            addressTextInputLayout.setErrorEnabled(false);
        }

        return isAddressValid;
    }

    private void setupAddSupermarketResponseHandler()
    {
        this.addSupermarketResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("ADD_SUPERMARKET_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(getContext(), getString(R.string.supermarket_added), Toast.LENGTH_SHORT).show();
                        GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingSupermarket(false);

                        // Save the added supermarket in the cache
                        int id = response.getInt("id");
                        String name = nameField.getText().toString();
                        String address = addressField.getText().toString();

                        GlobalValuesManager.getInstance(getContext()).addSupermarket(new Supermarket(id, name, address, new ArrayList<Product>()));

                        // Go to management fragment
                        showManageSupermarketFragment();
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

    private void sendAddSupermarketRequest()
    {
        // JSON POST parameters
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("groupID", GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID());
            jsonParams.put("name", nameField.getText().toString());
            jsonParams.put("address", addressField.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Debug
        Log.d("ADD_SUPERMARKET_REQ", jsonParams.toString());

        // Define what to do on response
        setupAddSupermarketResponseHandler();

        // Send request
        SupermarketDatabaseHelper.sendAddSupermarketRequest(jsonParams, getContext(), addSupermarketResponseHandler);

    }

    private void showManageSupermarketFragment()
    {
        if(HomeFragmentContainer.getInstance().isStackEmpty())
        {
            // I arrived here from the EmptySupermarket, no fragment is in the stack
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageSupermarketFragment());
            transaction.commit();
        }
        else
        {
            // If I arrived here from ManageSupermarketFragment, pop the fragment from the stack
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            HomeFragmentContainer.getInstance().setStackEmpty(true);
        }
    }

    private void loadMapView(Bundle savedInstanceState)
    {
        supermarketMapView.onCreate(savedInstanceState);
        supermarketMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                CreateSupermarketFragment.this.googleMap = googleMap;
                setupGoogleMap();
            }
        });
    }

    private void setupGoogleMap()
    {
        // Center the camera on Brescia
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(45.5436018, 10.1886594), 10);
        googleMap.animateCamera(cameraUpdate);
    }

    private void moveMap(LatLng newCoordinates)
    {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(newCoordinates, 15);
        googleMap.animateCamera(cameraUpdate);

        // Add a marker
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(newCoordinates));
    }

    private final class AddressResolutionTask extends AsyncTask<Void, Void, Object> {

        private final Geocoder mGeocoder;

        private final String mQuery;

        private AddressResolutionTask(final String query) {
            mGeocoder = new Geocoder(getContext(), Locale.getDefault());
            mQuery = query;
        }

        @Override
        protected Object doInBackground(final Void... params) {
            try {
                return mGeocoder.getFromLocationName(mQuery, 20, true);
            } catch (GeocoderException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(final Object result) {
            if (result instanceof GeocoderException)
            {
                Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            List<Address> resultAddressList = (List<Address>) result;
            if(resultAddressList.size() > 0)
            {
                double latitude = resultAddressList.get(0).getLocation().latitude;
                double longitude = resultAddressList.get(0).getLocation().longitude;
                moveMap(new LatLng(latitude, longitude));
            }
            else
            {
                Toast.makeText(getContext(), getString(R.string.location_resolution_error), Toast.LENGTH_LONG).show();
            }

        }
    }


}
