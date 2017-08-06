package com.mobile.paolo.listaspesa.model.objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

/**
 * An object that represents a user.
 * A user is defined by a numeric ID, a username, a password and an address.
 * The object also as a field used to select it when the user is shown in lists.
 */

public class User implements Serializable
{
    private int id;
    private String username;
    private String password;
    private String address;

    // This attribute will be used only in lists where is required to select users
    private boolean isChecked;

    // Default constructor
    public User(int id, String username, String password, String address)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.address = address;
    }

    public User(int id, String username, String password, String address, boolean isChecked)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.address = address;
        this.isChecked = isChecked;
    }

    public User(JSONObject jsonUser)
    {
        try {
            this.id = jsonUser.getInt("id");
            this.username = jsonUser.getString("username");
            this.address = jsonUser.getString("address");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON()
    {
        HashMap<String, String> values = new HashMap<>();
        values.put("id", ((Integer) id).toString());
        values.put("username", username);
        values.put("address", address);
        return new JSONObject(values);
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

}
