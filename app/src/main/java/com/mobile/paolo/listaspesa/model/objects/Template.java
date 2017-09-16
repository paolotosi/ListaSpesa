package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that represents a template.
 * A template is defined by its name, the ID of the group that created it and by a list of products.
 */

public class Template
{
    //Object fields
    private int id;
    private String name;
    private Integer groupID;
    private List<Product> productList;


    // ----- CONSTRUCTORS -----

    //Constructor used in Template creation
    public Template(String name, Integer groupID, List<Product> productList)
    {
        this.name = name;
        this.groupID = groupID;
        this.productList = productList;
    }

    //Constructor when the template is recovered from remote DB and there are operations involving the product list
    public Template(int id, String name, Integer groupID, List<Product> productList)
    {
        this.id = id;
        this.name = name;
        this.groupID = groupID;
        this.productList = productList;
    }

    //Constructor used when the productList is recovered in JSONArray format
    public Template(String name, Integer groupID, JSONArray jsonProductList)
    {
        this.name = name;
        this.groupID = groupID;
        this.productList = new ArrayList<>();
        for (int i = 0; i < jsonProductList.length(); i++)
        {
            try
            {
                JSONObject jsonProduct = jsonProductList.getJSONObject(i);
                this.productList.add(i, Product.fromJSON(jsonProduct));
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    //Same as before but when recovered from remote DB
    public Template(int id, String name, Integer groupID, JSONArray jsonProductList)
    {
        this.id = id;
        this.name = name;
        this.groupID = groupID;
        this.productList = new ArrayList<>();
        for (int i = 0; i < jsonProductList.length(); i++)
        {
            try
            {
                JSONObject jsonProduct = jsonProductList.getJSONObject(i);
                this.productList.add(i, Product.fromJSON(jsonProduct));
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    //Constructor from JSONObject
    public static Template fromJSON(JSONObject jsonTemplate)
    {
        Template template = null;
        try
        {
            int id = jsonTemplate.getInt("id");
            String name = jsonTemplate.getString("name");
            Integer groupID = jsonTemplate.getInt("groupID");
            JSONArray productList = jsonTemplate.getJSONArray("productList");
            template = new Template(id, name, groupID, productList);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return template;
    }


    //JSONObject from standard object
    public JSONObject toJSON()
    {
        JSONObject jsonTemplate = new JSONObject();
        try
        {
            jsonTemplate.put("id", this.id);
            jsonTemplate.put("name", this.name);
            jsonTemplate.put("groupID", this.groupID.toString());
            JSONArray productList = new JSONArray();
            for (int i = 0; i < this.productList.size(); i++)
            {
                productList.put(i, this.productList.get(i).toJSON());
            }
            jsonTemplate.put("productList", productList);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonTemplate;
    }


    // ------- METHODS -------
    public int getID()
    {
        return this.id;
    }

    public void setID(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getGroupID()
    {
        return this.groupID;
    }

    public void setGroupID(Integer groupID)
    {
        this.groupID = groupID;
    }

    public List<Product> getProductList()
    {
        return this.productList;
    }

    public void setProductList(List<Product> productList)
    {
        this.productList = productList;
    }


}
