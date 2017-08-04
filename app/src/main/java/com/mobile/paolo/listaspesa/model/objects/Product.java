package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by paolo on 04/08/17.
 */

public class Product
{
    private String name;
    private String brand;
    private String description;

    public Product(String name, String brand, String description)
    {
        this.name = name;
        this.brand = brand;
        this.description = description;
    }

    public static Product fromJSON(JSONObject jsonProduct)
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

    public JSONObject toJSON()
    {
        HashMap<String, String> values = new HashMap<>();
        values.put("name", this.name);
        values.put("brand", this.brand);
        values.put("description", this.description);
        return new JSONObject(values);
    }
}
