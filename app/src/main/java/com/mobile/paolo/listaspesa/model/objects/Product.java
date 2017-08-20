package com.mobile.paolo.listaspesa.model.objects;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * An object that represents a product.
 * A product is defined by its name, its brand and (eventually) its description.
 */

public class Product
{
    private String name;
    private String brand;
    private String description;
    private int quantity;

    // This attribute will be used only in lists where is required to select products
    private boolean isChecked;

    // Used to compare two products
    public static final Comparator<Product> ALPHABETICAL_COMPARATOR = new Comparator<Product>() {
        @Override
        public int compare(Product p1, Product p2) {
            return p1.getName().compareTo(p2.getName());
        }
    };

    public Product(String name)
    {
        this.name = name;
    }

    public Product(String name, String brand, String description)
    {
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.quantity = 0;
    }

    public Product(String name, String brand, String description, int quantity)
    {
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.quantity = quantity;
    }

    public static Product fromJSON(JSONObject jsonProduct)
    {
        Product product = null;

        try {
            String name = jsonProduct.getString("name");
            product = new Product(name);
            if(jsonProduct.has("brand"))
            {
                String brand = jsonProduct.getString("brand");
                product.setBrand(brand);
            }
            if(jsonProduct.has("description"))
            {
                String description = jsonProduct.getString("description");
                product.setDescription(description);
            }
            if(jsonProduct.has("quantity"))
            {
                int quantity = jsonProduct.getInt("quantity");
                product.setQuantity(quantity);
            };
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
        values.put("quantity", ((Integer) this.quantity).toString());
        return new JSONObject(values);
    }

    public String getName() {

        return name;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public int getQuantity()
    {
        return quantity;
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

    public static JSONArray asJSONProductList(List<Product> productList)
    {
        JSONArray jsonProductList = new JSONArray();
        for(int i = 0; i < productList.size(); i++)
        {
            try {
                jsonProductList.put(i, productList.get(i).toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonProductList;
    }

    public static List<Product> parseJSONProductList(JSONArray jsonProductList)
    {
        List<Product> productList = new ArrayList<>();
        for(int i = 0; i < jsonProductList.length(); i++)
        {
            try {
                JSONObject jsonProduct = jsonProductList.getJSONObject(i);
                productList.add(Product.fromJSON(jsonProduct));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return productList;
    }
}
