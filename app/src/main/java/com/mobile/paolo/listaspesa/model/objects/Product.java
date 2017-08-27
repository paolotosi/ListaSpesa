package com.mobile.paolo.listaspesa.model.objects;

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
    private int id;
    private Boolean matrix;

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

    public Product(int id, String name, String brand, String description)
    {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.quantity = 0;
        this.matrix = false;
    }

    public Product(int id, String name, String brand, String description, int quantity)
    {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.quantity = quantity;
        this.matrix = false;
    }

    public static Product fromJSON(JSONObject jsonProduct)
    {
        Product product = null;

        try {

            String name = jsonProduct.getString("name");
            product = new Product(name);
            if(jsonProduct.has("id"))
            {
                int id = jsonProduct.getInt("id");
                product.setID(id);
            }
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
            if(jsonProduct.has("matrix"))
            {
                int matrix = 0;
                if(!jsonProduct.getString("matrix").equalsIgnoreCase("null")) {
                    matrix = jsonProduct.getInt("matrix");
                }
                product.setMatrix(matrix);
            };
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return product;
    }

    public JSONObject toJSON()
    {
        HashMap<String, String> values = new HashMap<>();
        values.put("id", String.valueOf(this.id));
        values.put("name", this.name);
        values.put("brand", this.brand);
        values.put("description", this.description);
        values.put("quantity", ((Integer) this.quantity).toString());
        if(this.matrix == null)
        {
            this.matrix = false;
        }
        if(this.matrix) {
            values.put("matrix", String.valueOf(1));
        }
        else
        {
            values.put("matrix", String.valueOf(0));
        }
        return new JSONObject(values);
    }

    public String getName() {

        return name;
    }

    public Boolean getMatrix()
    {
        return matrix;
    }

    public void setMatrix(int i)
    {
        if(i > 0)
        {
            this.matrix = true;
        }
        else
        {
            this.matrix = false;
        }
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setID(int id)
    {
        this.id = id;
    }

    public int getID()
    {
        return id;
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

    public String toString()
    {
        return this.name + " " + this.brand;
    }

    public static String asStringProductList(List<Product> productList)
    {
        String productListString = "";
        for (Product product : productList)
        {
            productListString += product.toString() + "\n";
        }
        return productListString;
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
