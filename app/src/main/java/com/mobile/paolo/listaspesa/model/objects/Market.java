package com.mobile.paolo.listaspesa.model.objects;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by paolo on 20/08/17.
 */

public class Market
{
    private String name;
    private String address;
    // The list of products that can be found in this market
    private List<Product> productList;
    private LatLng cachedAbsolutePosition;

    public Market(String name, String address, List<Product> productList)
    {
        this.name = name;
        this.address = address;
        this.productList = productList;
        this.cachedAbsolutePosition = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocationOnMap(Context context)
    {
        if(this.cachedAbsolutePosition == null)
        {
            // Determine absolute position from address only the first time
            Geocoder geocoder = new Geocoder(context, Locale.ITALY);
            List<Address> addresses = new ArrayList<>();
            try {
                addresses = geocoder.getFromLocationName(this.address, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addresses.size() > 0) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                cachedAbsolutePosition = new LatLng(latitude, longitude);
            }
        }
        return cachedAbsolutePosition;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public List<Product> getAvailableGroceryListProducts(List<Product> groceryList)
    {
        List<Product> availableProducts = new ArrayList<>();
        for(Product groceryProduct : groceryList)
        {
            for(Product marketProduct : productList)
            {
                if(groceryProduct.getName().equalsIgnoreCase(marketProduct.getName()))
                {
                    availableProducts.add(groceryProduct);
                }
            }
        }
        return availableProducts;
    }

    public static Market fromJSON(JSONObject jsonMarket)
    {
        Market market = null;
        try {
            String name = jsonMarket.getString("name");
            String address = jsonMarket.getString("address");
            List<Product> productList = Product.parseJSONProductList(jsonMarket.getJSONArray("productList"));
            market = new Market(name, address, productList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return market;
    }

    public JSONObject toJSON()
    {
        JSONObject jsonMarket = new JSONObject();
        try {
            jsonMarket.put("name", this.name);
            jsonMarket.put("address", this.address);
            jsonMarket.put("productList", Product.asJSONProductList(this.productList));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonMarket;
    }
}