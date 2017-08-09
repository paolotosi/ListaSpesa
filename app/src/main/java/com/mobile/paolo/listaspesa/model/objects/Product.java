package com.mobile.paolo.listaspesa.model.objects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * An object that represents a product.
 * A product is defined by its name, its brand and (eventually) its description.
 */

public class Product
{
    private final static String BASE = "baseProduct";
    private final static String SHOPLIST = "shopListProduct";

    private String name;
    private String brand;
    private String description;
    private int quantity;

    // This attribute will be used only in lists where is required to select products
    private boolean isChecked;

    public Product(String name, String brand, String description)
    {
        this.name = name;
        this.brand = brand;
        this.description = description;
    }

    public Product(String name, int quantity)
    {
        this.name = name;
        this.quantity = quantity;
    }

    //Flag specifies which constructor has to be chosen
    public static Product fromJSON(JSONObject jsonProduct, String flag)
    {
        Product product = null;

        if(BASE.equalsIgnoreCase(flag))
        {
            product = baseProduct(jsonProduct);
        }
        if(SHOPLIST.equalsIgnoreCase(flag))
        {
            product = shoppingListProduct(jsonProduct);
        }


        return product;
    }

    private static Product baseProduct(JSONObject jsonProduct)
    {
        Product product = null;
        try {
            String name = jsonProduct.getString("name");
            String brand = jsonProduct.getString("brand");
            String description = jsonProduct.getString("description");
            product = new Product(name, brand, description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return product;
    }

    private static Product shoppingListProduct(JSONObject jsonProduct)
    {
        Product product = null;
        try {
            String name = jsonProduct.getString("name");
            int quantity = jsonProduct.getInt("quantity");
            product = new Product(name, quantity);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return product;
    }

    public JSONObject toJSON()
    {
        HashMap<String, String> values = new HashMap<>();
        values.put("name", this.name);
        values.put("brand", this.brand);
        values.put("description", this.description);
        return new JSONObject(values);
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked()
    {
        return isChecked;
    }

    public void setChecked(boolean checked)
    {
        this.isChecked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (!name.equals(product.name)) return false;
        if (!brand.equals(product.brand)) return false;
        return description != null ? description.equals(product.description) : product.description == null;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + brand.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
