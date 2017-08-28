package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.doctoror.geocoder.Address;
import com.doctoror.geocoder.Geocoder;
import com.doctoror.geocoder.GeocoderException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.local.ProductsLocalDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SupermarketMapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener
{
    // Widgets
    private Toolbar supermarketMapToolbar;
    private FloatingActionButton zoomOutButton;

    // List of markets to show
    private List<Supermarket> supermarketList = new ArrayList<>();

    // Current grocery list
    private List<Product> groceryList;

    // Google Maps attributes
    private GoogleMap googleMap;
    private Map<String, Marker> markerMap = new HashMap<>();

    // Map bounds to show all supermarkets
    private LatLngBounds mapBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_map);

        setupToolbar();

        setupZoomOutButton();

        supermarketList = GlobalValuesManager.getInstance(getApplicationContext()).getSupermarkets();

        readProductsFromLocalDatabase();

        // Get the map asynchronously
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                SupermarketMapActivity.this.googleMap = googleMap;

                if (SupermarketMapActivity.this.googleMap != null)
                {
                    setupMap();

                    // addMarkersToMap();

                    // zoomToFitAllMarkers();
                    AddressResolutionTask addressResolutionTask = new SupermarketMapActivity.AddressResolutionTask();
                    addressResolutionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        // Return selected supermarket to calling activity
        Intent returnIntent = new Intent();
        returnIntent.putExtra("RESULT", getSupermarketFromSelectedMarker(marker).toJSON().toString());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void setupToolbar()
    {
        supermarketMapToolbar = (Toolbar) findViewById(R.id.supermarketMapToolbar);
        supermarketMapToolbar.setTitle("Seleziona un supermercato");
        supermarketMapToolbar.setTitleTextColor(getColor(R.color.white));
        supermarketMapToolbar.setBackgroundColor(getColor(R.color.colorPrimary));
    }

    private void setupZoomOutButton()
    {
        zoomOutButton = (FloatingActionButton) findViewById(R.id.zoomOutButton);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToFitAllMarkers();
            }
        });
    }

    private void readProductsFromLocalDatabase()
    {
        ProductsLocalDatabaseHelper localDatabaseHelper = ProductsLocalDatabaseHelper.getInstance(getApplicationContext());
        localDatabaseHelper.open();
        groceryList = localDatabaseHelper.getAllProducts();
        localDatabaseHelper.close();
    }

    private void setupMap()
    {
        // Change the googleMap type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Customize info window
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Supermarket selectedSupermarket = getSupermarketFromSelectedMarker(marker);

                View infoWindow = getLayoutInflater().inflate(R.layout.card_market_layout, null);

                ((TextView) infoWindow.findViewById(R.id.marketName)).setText(selectedSupermarket.getName());
                ((TextView) infoWindow.findViewById(R.id.marketAddress)).setText(selectedSupermarket.getAddress());

                // Get available products
                List<Product> availableProducts = selectedSupermarket.getAvailableGroceryListProducts(groceryList);
                if(availableProducts.size() > 0)
                {
                    ((TextView) infoWindow.findViewById(R.id.marketProductList)).setText(Product.asStringProductList(availableProducts));
                }
                else
                {
                    infoWindow.findViewById(R.id.marketProductList).setVisibility(View.GONE);
                }

                Toast.makeText(getApplicationContext(), "Tocca la finestra delle informazioni per selezionare il supermercato", Toast.LENGTH_SHORT).show();

                return infoWindow;
            }
        });

        // Listen to info window click events
        googleMap.setOnInfoWindowClickListener(this);
    }

    private void addMarkersToMap()
    {
        for(Supermarket supermarket : supermarketList)
        {
            IconGenerator iconGenerator = new IconGenerator(this);
            String iconText = supermarket.getName() + "\n" + supermarket.getAvailableGroceryListProducts(groceryList).size() + "/" + groceryList.size() + " prodotti della lista";
            Bitmap bitmap = iconGenerator.makeIcon(iconText);

            // For each supermarket, create a marker with name and position
            Marker mapMarker = googleMap.addMarker(new MarkerOptions()
                    .position(supermarket.getLocationOnMap(getApplicationContext()))
                    .title(supermarket.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

            // Save the created marker in an HashMap indexed by supermarket name
            markerMap.put(supermarket.getName(), mapMarker);
        }
    }

    private Supermarket getSupermarketFromSelectedMarker(Marker selectedMarker)
    {
        Supermarket selectedSupermarket = null;
        String marketName = selectedMarker.getTitle();
        for(Supermarket supermarket : supermarketList)
        {
            if(supermarket.getName().equalsIgnoreCase(marketName))
            {
                selectedSupermarket = supermarket;
            }
        }
        return selectedSupermarket;
    }

    private void setupMapBounds(List<LatLng> coordinates)
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(LatLng latLng : coordinates)
        {
            builder.include(latLng);
        }

        mapBounds = builder.build();
    }

    private void zoomToFitAllMarkers()
    {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(mapBounds, 250);
        googleMap.animateCamera(cameraUpdate);
    }

    private final class AddressResolutionTask extends AsyncTask<Void, Void, Object> {

        private final Geocoder mGeocoder;

        private List<Address> bounds = new ArrayList<>();

        private AddressResolutionTask() {
            mGeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        }

        @Override
        protected Object doInBackground(final Void... params) {
            try {
                for(Supermarket supermarket : supermarketList)
                {
                    bounds.add(mGeocoder.getFromLocationName(supermarket.getAddress(), 1, true).get(0));
                }
                return bounds;
            } catch (GeocoderException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(final Object result) {
            if (result instanceof GeocoderException)
            {
                Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            List<Address> resultAddressList = (List<Address>) result;
            if(resultAddressList.size() > 0)
            {
                List<LatLng> coordinates = new ArrayList<>();
                for(int i = 0; i < resultAddressList.size(); i++)
                {
                    double latitude = resultAddressList.get(i).getLocation().latitude;
                    double longitude = resultAddressList.get(i).getLocation().longitude;
                    supermarketList.get(i).setCachedAbsolutePosition(new LatLng(latitude, longitude));
                    coordinates.add(new LatLng(latitude, longitude));

                }
                setupMapBounds(coordinates);

                zoomToFitAllMarkers();

                addMarkersToMap();
            }
            else
            {
                Toast.makeText(getApplicationContext(), getString(R.string.location_resolution_error), Toast.LENGTH_LONG).show();
            }

        }
    }


}
