package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * An object that represents a product.
 * A product is defined by its name, its brand and (eventually) its description.
 */

public class Product
{
    private String name;
    private String brand;
    private String description;

    // This attribute will be used only in lists where is required to select products
    private boolean isChecked;

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


}
