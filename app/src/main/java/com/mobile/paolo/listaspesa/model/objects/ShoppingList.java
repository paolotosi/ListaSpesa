package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by revoc on 08/08/2017.
 */

/**
 * An object that represents a shopping list.
 */

public class ShoppingList
{
    //Object field
    private int groupID;
    private List<Product> productList = new ArrayList<>();
    private boolean taken;
    
    // --- CONSTRUCTORS ---
    //Constructor when list is derived by template
    public ShoppingList(Template template)
    {
        this.groupID = template.getGroupID();
        this.productList.addAll(template.getProductList());
        initializeProductsQuantity();
        this.taken = false;
    }
    
    //List derived from template but with not found products from last shopping
    public ShoppingList(Template template, Collection<Product> productsNotFound)
    {
        this.groupID = template.getGroupID();
        this.productList.addAll(template.getProductList());
        initializeProductsQuantity();
        for (Product productNotFound : productsNotFound)
        {
            boolean match = false;
            for (Product productInList : getProductList())
            {
                // If a product not found is already present in the list, sum the quantities
                if (productNotFound.getName().equalsIgnoreCase(productInList.getName()))
                {
                    match = true;
                    productInList.setQuantity(productInList.getQuantity() + productNotFound.getQuantity());
                }
            }
            if (!match)
            {
                // Else, add it in the list
                this.productList.add(productNotFound);
            }
        }
        this.taken = false;
    }
    
    //Private constructor used in this class
    private ShoppingList(int groupID, List<Product> productList)
    {
        this.groupID = groupID;
        this.productList = productList;
        this.taken = false;
    }
    
    //Empty constructor
    public ShoppingList()
    {
        this.productList = new ArrayList<>();
    }
    
    public JSONObject toJSON()
    {
        JSONObject jsonList = new JSONObject();
        try
        {
            jsonList.put("groupID", String.valueOf(this.groupID));
            jsonList.put("productList", Product.asJSONProductList(this.productList));
            jsonList.put("taken", this.taken);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonList;
    }
    
    //Constructor from JSON
    public static ShoppingList fromJSON(JSONObject jsonList)
    {
        ShoppingList list = null;
        try
        {
            int groupID = jsonList.getInt("groupID");
            JSONArray jsonProductList = jsonList.getJSONArray("productList");
            List<Product> productList = Product.parseJSONProductList(jsonProductList);
            list = new ShoppingList(groupID, productList);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return list;
    }
    
    // --- METHODS ---
    private void initializeProductsQuantity()
    {
        // Templates don't have quantities, when the list is created we set it to 1
        for (Product product : getProductList())
        {
            product.setQuantity(1);
        }
    }
    
    public int getGroupID()
    {
        return groupID;
    }
    
    public List<Product> getProductList()
    {
        return productList;
    }
    
    public void setProductList(List<Product> productList)
    {
        this.productList = productList;
    }
    
    public void setTaken(boolean isTaken)
    {
        this.taken = isTaken;
    }
    
}
