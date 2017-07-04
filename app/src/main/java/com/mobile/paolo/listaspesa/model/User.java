package com.mobile.paolo.listaspesa.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by paolo on 04/07/17.
 */

public class User implements Serializable
{
    private String username;
    private String password;
    private String address;

    // This attribute will be used only in lists where is required to select users
    private boolean isChecked;

    // Default constructor
    public User(String username, String password, String address)
    {
        this.username = username;
        this.password = password;
        this.address = address;
    }

    public User(String username, String password, String address, boolean isChecked)
    {
        this.username = username;
        this.password = password;
        this.address = address;
        this.isChecked = isChecked;
    }

    public User(JSONObject jsonUser)
    {
        try {
            this.username = jsonUser.getString("nome");
            this.address = jsonUser.getString("indirizzo");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
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
