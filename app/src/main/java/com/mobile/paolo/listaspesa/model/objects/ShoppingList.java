package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Created by revoc on 08/08/2017.
 */

public class ShoppingList extends Template
{
    /**Variable state provides the state of the list between "in preparazione"
     * and "in acquisto"
     */
    private boolean state;
    private HashMap<String, String> details;
    private final static String SHOPLIST = "shopListProduct";

    public ShoppingList(String name, Integer groupID, List<Product> productList)
    {
        super(name, groupID, productList);
        this.state = false;
        this.details = new HashMap<>();
    }



    public void addDetails(String fieldName, String field)
    {
        details.put(fieldName, field);
    }

    public void addDetails(String fieldName, int quantity)
    {
        String qAsString = String.valueOf(quantity);
        details.put(fieldName, qAsString);
    }

    public static ShoppingList fromJSON(JSONObject jsonList)
    {
        ShoppingList list = null;
        try {
            String name = "Shopping List";
            Integer groupID = jsonList.getInt("groupID");
            JSONArray productList = jsonList.getJSONArray("list");
            List<Product> shoppingList = new ArrayList<>();
            for(int i = 0; i < productList.length(); i++)
            {
                JSONObject jsonProduct = productList.getJSONObject(i);
                Product product = Product.fromJSON(jsonProduct, SHOPLIST);
                shoppingList.add(product);
            }

            list = new ShoppingList(name, groupID, shoppingList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }


}
