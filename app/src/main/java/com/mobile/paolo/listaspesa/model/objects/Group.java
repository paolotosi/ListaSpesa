package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that represents a group.
 * A group is defined by an ID, a name and a list of members.
 */

public class Group
{
    //Object fields
    private int id;
    private String name;
    private List<User> members;
    
    // --- CONSTRUCTORS ---
    
    //Standard constructor
    public Group(int id, String groupName, List<User> members)
    {
        this.id = id;
        this.name = groupName;
        this.members = members;
    }
    
    //Constructor with member list recived as JSONArray
    public Group(int id, String groupName, JSONArray members)
    {
        this.id = id;
        this.name = groupName;
        this.members = new ArrayList<>();
        for (int i = 0; i < members.length(); i++)
        {
            try
            {
                JSONObject jsonUser = members.getJSONObject(i);
                int userID = jsonUser.getInt("id");
                String username = jsonUser.getString("username");
                String address = jsonUser.getString("address");
                this.members.add(i, new User(userID, username, null, address));
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    //Building group from JSONObject
    public static Group fromJSON(JSONObject jsonGroup)
    {
        Group group = null;
        try
        {
            int id = jsonGroup.getInt("groupID");
            String groupName = jsonGroup.getString("groupName");
            JSONArray members = jsonGroup.getJSONArray("members");
            group = new Group(id, groupName, members);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return group;
    }
    
    //Building a JSONObject containing group
    public JSONObject toJSON()
    {
        JSONObject jsonGroup = new JSONObject();
        try
        {
            jsonGroup.put("groupID", this.id);
            jsonGroup.put("groupName", this.name);
            JSONArray members = new JSONArray();
            for (int i = 0; i < this.members.size(); i++)
            {
                members.put(i, this.members.get(i).toJSON());
            }
            jsonGroup.put("members", members);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonGroup;
    }
    
    // ---- METHODS ----
    public int getID()
    {
        return this.id;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setName(String groupName)
    {
        this.name = groupName;
    }
    
    public List<User> getMembers()
    {
        return this.members;
    }
    
    public void setMembers(List<User> members)
    {
        this.members = members;
    }
}
