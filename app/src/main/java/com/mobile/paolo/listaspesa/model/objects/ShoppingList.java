package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONArray;
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


}
