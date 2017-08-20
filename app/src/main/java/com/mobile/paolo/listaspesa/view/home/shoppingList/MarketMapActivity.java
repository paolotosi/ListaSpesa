package com.mobile.paolo.listaspesa.view.home.shoppingList;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.local.ProductsLocalDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Market;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketMapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener
{
    // List of markets to show
    List<Market> marketList = new ArrayList<>();

    // Current grocery list
    List<Product> groceryList;

    // Google Maps attributes
    private GoogleMap googleMap;
    private Map<String, Marker> markerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_map);

        // Dummy test product lists
        List<Product> productList1 = GlobalValuesManager.getInstance(getApplicationContext()).getUserTemplates().get(0).getProductList();
        List<Product> productList2 = GlobalValuesManager.getInstance(getApplicationContext()).getUserTemplates().get(1).getProductList();
        List<Product> productList3 = GlobalValuesManager.getInstance(getApplicationContext()).getUserTemplates().get(2).getProductList();

        // Dummy test markets
        Market auchanConcesio = new Market("Auchan", "Via Europa, 8, 25062 Concesio BS", productList1);
        Market frecciaRossa = new Market("Freccia Rossa", "Viale Italia, 31, 25126 Brescia BS", productList2);
        Market euroSpar = new Market("Eurospar", "Via I. Vivanti, 19, 25133 Brescia BS", productList3);

        // TODO: When the markets will be read from the db, initialize the list from SharedPreferences
        marketList.add(auchanConcesio);
        marketList.add(frecciaRossa);
        marketList.add(euroSpar);

        readProductsFromLocalDatabase();

        // Get the map asynchronously
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MarketMapActivity.this.googleMap = googleMap;

                if (MarketMapActivity.this.googleMap != null)
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

                Market selectedMarket = getMarketFromSelectedMarker(marker);

                View infoWindow = getLayoutInflater().inflate(R.layout.card_market_layout, null);

                ((TextView) infoWindow.findViewById(R.id.marketName)).setText(selectedMarket.getName());
                ((TextView) infoWindow.findViewById(R.id.marketAddress)).setText(selectedMarket.getAddress());

                // Get available products
                List<Product> availableProducts = selectedMarket.getAvailableGroceryListProducts(groceryList);
                ((TextView) infoWindow.findViewById(R.id.marketProductList)).setText(Product.asStringProductList(availableProducts));

                return infoWindow;
            }
        });
        }

    private void addMarkersToMap()
    {
        for(Market market : marketList)
        {
            IconGenerator iconGenerator = new IconGenerator(this);
            String iconText = market.getName() + "\n" + market.getAvailableGroceryListProducts(groceryList).size() + " prodotti su " + groceryList.size();
            Bitmap bitmap = iconGenerator.makeIcon(iconText);

            // For each market, create a marker with name and position
            Marker mapMarker = googleMap.addMarker(new MarkerOptions()
                    .position(market.getLocationOnMap(getApplicationContext()))
                    .title(market.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

            // Save the created marker in an HashMap indexed by market name
            markerMap.put(market.getName(), mapMarker);
        }
    }

    private Market getMarketFromSelectedMarker(Marker selectedMarker)
    {
        Market selectedMarket = null;
        String marketName = selectedMarker.getTitle();
        for(Market market : marketList)
        {
            if(market.getName().equalsIgnoreCase(marketName))
            {
                selectedMarket = market;
            }
        }
        return selectedMarket;
    }

    private void zoomToFitAllMarkers()
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(Market market : marketList)
        {
            builder.include(market.getLocationOnMap(getApplicationContext()));
        }

        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 120);
        googleMap.animateCamera(cameraUpdate);
    }


}
