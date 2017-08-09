package com.mobile.paolo.listaspesa.utility;

import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

/**
 * Created by paolo on 09/08/17.
 */

public class Contextualizer
{
    private static Contextualizer instance;

    // Context variables
    private Boolean isUserLogged;
    private Boolean isUserPartOfAGroup;
    private Boolean hasUserTemplates;

    // Network response handler (used the first time)
    private NetworkResponseHandler isLoggedResponseHandler;
    private NetworkResponseHandler hasGroupResponseHandler;
    private NetworkResponseHandler hasTemplateResponseHandler;

    public static synchronized Contextualizer getInstance()
    {
        if(instance == null)
        {
            instance = new Contextualizer();
        }
        return instance;
    }

    public Boolean isUserLogged() {
        return isUserLogged;
    }

    public void setUserLogged(Boolean userLogged) {
        isUserLogged = userLogged;
    }

    public Boolean isUserPartOfAGroup() {
        return isUserPartOfAGroup;
    }

    public void setUserPartOfAGroup(Boolean userPartOfAGroup) {
        isUserPartOfAGroup = userPartOfAGroup;
    }

    public Boolean hasUserTemplates() {
        return hasUserTemplates;
    }

    public void setHasUserTemplates(Boolean hasUserTemplates) {
        this.hasUserTemplates = hasUserTemplates;
    }
}
