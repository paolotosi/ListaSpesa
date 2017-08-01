package com.mobile.paolo.listaspesa.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paolo on 01/08/17.
 */

public class Group
{
    private int id;
    private String groupName;
    private List<User> members;

    public Group(int id, String groupName, List<User> members)
    {
        this.id = id;
        this.groupName = groupName;
        this.members = members;
    }

    public Group(int id, String groupName, JSONArray members)
    {
        this.id = id;
        this.groupName = groupName;
        this.members = new ArrayList<>();
        for(int i = 0; i<members.length(); i++)
        {
            try {
                JSONObject jsonUser = members.getJSONObject(i);
                String username = jsonUser.getString("username");
                String address = jsonUser.getString("address");
                this.members.add(i, new User(-1, username, null, address));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static Group fromJSON(JSONObject jsonGroup)
    {
        Group group = null;
        try {
            int id = jsonGroup.getInt("groupID");
            String groupName = jsonGroup.getString("groupName");
            JSONArray members = jsonGroup.getJSONArray("members");
            group = new Group(id, groupName, members);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return group;
    }

    public JSONObject toJSON()
    {
        JSONObject jsonGroup = new JSONObject();
        try {
            jsonGroup.put("groupID", this.id);
            jsonGroup.put("groupName", this.groupName);
            JSONArray members = new JSONArray();
            for(int i = 0; i<this.members.size(); i++)
            {
                members.put(i, this.members.get(i).toJSON());
            }
            jsonGroup.put("members", members);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonGroup;
    }

    public int getID()
    {
        return this.id;
    }

    public void setID(int id)
    {
        this.id = id;
    }

    public String getGroupName()
    {
        return this.groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
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
