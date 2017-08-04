package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paolo on 04/08/17.
 */

public class Template
{
    private String name;
    private Integer groupID;
    private List<Product> productList;

    public Template(String name, Integer groupID, List<Product> productList)
    {
        this.name = name;
        this.groupID = groupID;
        this.productList = productList;
    }

    public Template(String name, Integer groupID, JSONArray jsonProductList)
    {
        this.name = name;
        this.groupID = groupID;
        this.productList = new ArrayList<>();
        for(int i = 0; i < jsonProductList.length(); i++)
        {
            try {
                JSONObject jsonProduct = jsonProductList.getJSONObject(i);
                this.productList.add(i, Product.fromJSON(jsonProduct));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static Template fromJSON(JSONObject jsonTemplate)
    {
        Template template = null;
        try {
            String name = jsonTemplate.getString("name");
            Integer groupID = jsonTemplate.getInt("groupID");
            JSONArray productList = jsonTemplate.getJSONArray("productList");
            template = new Template(name, groupID, productList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return template;
    }


    public JSONObject toJSON()
    {
        JSONObject jsonTemplate = new JSONObject();
        try {
            jsonTemplate.put("name", this.name);
            jsonTemplate.put("groupID", this.groupID.toString());
            JSONArray productList = new JSONArray();
            for(int i = 0; i < this.productList.size(); i++)
            {
                productList.put(i, this.productList.get(i).toJSON());
            }
            jsonTemplate.put("productList", productList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonTemplate;
    }


}
