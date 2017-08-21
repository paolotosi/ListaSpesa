package com.mobile.paolo.listaspesa.view.home.shoppingList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.local.ProductsLocalDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupermarketMapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener
{
    // List of markets to show
    List<Supermarket> supermarketList = new ArrayList<>();

    // Current grocery list
    List<Product> groceryList;

    // Google Maps attributes
    private GoogleMap googleMap;
    private Map<String, Marker> markerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_map);

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

                    addMarkersToMap();

                    zoomToFitAllMarkers();

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
                ((TextView) infoWindow.findViewById(R.id.marketProductList)).setText(Product.asStringProductList(availableProducts));

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
            String iconText = supermarket.getName() + "\n" + supermarket.getAvailableGroceryListProducts(groceryList).size() + " prodotti su " + groceryList.size();
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

    private void zoomToFitAllMarkers()
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(Supermarket supermarket : supermarketList)
        {
            builder.include(supermarket.getLocationOnMap(getApplicationContext()));
        }

        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 120);
        googleMap.animateCamera(cameraUpdate);
    }


}
