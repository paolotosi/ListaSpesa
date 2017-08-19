package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;

/**
 * Created by revoc on 08/08/2017.
 */

public class ShoppingList
{
    private int groupID;
    private List<Product> productList = new ArrayList<>();
    private boolean taken;

    public ShoppingList(Template template)
    {
        this.groupID = template.getGroupID();
        this.productList.addAll(template.getProductList());
        initializeProductsQuantity();
        this.taken = false;
    }

    public ShoppingList(Template template, Collection<Product> productsNotFound)
    {
        this.groupID = template.getGroupID();
        this.productList.addAll(template.getProductList());
        initializeProductsQuantity();
        for(Product productNotFound : productsNotFound)
        {
            boolean match = false;
            for(Product productInList : getProductList())
            {
                // If a product not found is already present in the list, sum the quantities
                if(productNotFound.getName().equalsIgnoreCase(productInList.getName()))
                {
                    match = true;
                    productInList.setQuantity(productInList.getQuantity() + productNotFound.getQuantity());
                }
            }
            if(!match)
            {
                // Else, add it in the list
                this.productList.add(productNotFound);
            }
        }
        this.taken = false;
    }

    private ShoppingList(int groupID, List<Product> productList)
    {
        this.groupID = groupID;
        this.productList = productList;
        this.taken = false;
    }

    public JSONObject toJSON()
    {
        JSONObject jsonList = new JSONObject();
        try {
            jsonList.put("groupID", String.valueOf(this.groupID));
            jsonList.put("productList", Product.asJSONProductList(this.productList));
            jsonList.put("taken", this.taken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonList;
    }

    public static ShoppingList fromJSON(JSONObject jsonList)
    {
        ShoppingList list = null;
        try {
            int groupID = jsonList.getInt("groupID");
            JSONArray jsonProductList = jsonList.getJSONArray("productList");
            List<Product> productList = Product.parseJSONProductList(jsonProductList);
            list = new ShoppingList(groupID, productList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void initializeProductsQuantity()
    {
        // Templates don't have quantities, when the list is created we set it to 1
        for(Product product : getProductList())
        {
            product.setQuantity(1);
        }
    }

    public int getGroupID() {
        return groupID;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public boolean isTaken()
    {
        return this.taken;
    }

    public void setTaken(boolean isTaken)
    {
        this.taken = isTaken;
    }

}
