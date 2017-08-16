package com.mobile.paolo.listaspesa.utility;

import com.mobile.paolo.listaspesa.view.home.group.CreateGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.EmptyGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.ManageGroupFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.CreateShoppingListFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.EmptyShoppingListFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.GroceryStoreFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.ManageShoppingListFragment;
import com.mobile.paolo.listaspesa.view.home.template.CreateTemplateFragment;
import com.mobile.paolo.listaspesa.view.home.template.EmptyTemplateFragment;
import com.mobile.paolo.listaspesa.view.home.template.ManageTemplateFragment;

/**
 * This class contains pointers to the various HomeActivity fragments.
 * It's used to avoid creating multiple instances of every fragment when the user navigates the app.
 */

public class HomeFragmentContainer
{
    private static HomeFragmentContainer instance;

    private EmptyGroupFragment emptyGroupFragment;
    private ManageGroupFragment manageGroupFragment;
    private CreateGroupFragment createGroupFragment;

    private EmptyTemplateFragment emptyTemplateFragment;
    private ManageTemplateFragment manageTemplateFragment;
    private CreateTemplateFragment createTemplateFragment;

    private EmptyShoppingListFragment emptyShoppingListFragment;
    private CreateShoppingListFragment createShoppingListFragment;
    private ManageShoppingListFragment manageShoppingListFragment;
    private GroceryStoreFragment groceryStoreFragment;

    public static synchronized HomeFragmentContainer getInstance()
    {
        if(instance == null)
        {
            instance = new HomeFragmentContainer();
        }
        return instance;
    }

    public EmptyGroupFragment getEmptyGroupFragment() {
        if(this.emptyGroupFragment == null)
        {
            this.emptyGroupFragment = new EmptyGroupFragment();
        }
        return emptyGroupFragment;
    }

    public void setEmptyGroupFragment(EmptyGroupFragment emptyGroupFragment) {
        this.emptyGroupFragment = emptyGroupFragment;
    }

    public ManageGroupFragment getManageGroupFragment() {
        if(this.manageGroupFragment == null)
        {
            this.manageGroupFragment = new ManageGroupFragment();
        }
        return manageGroupFragment;
    }

    public void setManageGroupFragment(ManageGroupFragment manageGroupFragment) {
        this.manageGroupFragment = manageGroupFragment;
    }

    public CreateGroupFragment getCreateGroupFragment() {
        if(this.createGroupFragment == null)
        {
            this.createGroupFragment = new CreateGroupFragment();
        }
        return createGroupFragment;
    }


    public EmptyTemplateFragment getEmptyTemplateFragment() {
        if(this.emptyTemplateFragment == null)
        {
            this.emptyTemplateFragment = new EmptyTemplateFragment();
        }
        return emptyTemplateFragment;
    }


    public ManageTemplateFragment getManageTemplateFragment() {
        if(this.manageTemplateFragment == null)
        {
            this.manageTemplateFragment = new ManageTemplateFragment();
        }
        return manageTemplateFragment;
    }

    public void setManageTemplateFragment(ManageTemplateFragment manageTemplateFragment) {
        this.manageTemplateFragment = manageTemplateFragment;
    }

    public CreateTemplateFragment getCreateTemplateFragment() {
        if(this.createTemplateFragment == null)
        {
            this.createTemplateFragment = new CreateTemplateFragment();
        }
        return createTemplateFragment;
    }

    public void setCreateTemplateFragment(CreateTemplateFragment createTemplateFragment) {
        this.createTemplateFragment = createTemplateFragment;
    }

    public EmptyShoppingListFragment getEmptyShoppingListFragment() {
        if(this.emptyShoppingListFragment == null)
        {
            this.emptyShoppingListFragment = new EmptyShoppingListFragment();
        }
        return emptyShoppingListFragment;
    }

    public CreateShoppingListFragment getCreateShoppingListFragment() {
        if(this.createShoppingListFragment == null)
        {
            this.createShoppingListFragment = new CreateShoppingListFragment();
        }
        return createShoppingListFragment;
    }

    public ManageShoppingListFragment getManageShoppingListFragment() {
        if(this.manageShoppingListFragment == null)
        {
            this.manageShoppingListFragment = new ManageShoppingListFragment();
        }
        return manageShoppingListFragment;
    }

    public GroceryStoreFragment getGroceryStoreFragment() {
        if(this.groceryStoreFragment == null)
        {
            this.groceryStoreFragment = new GroceryStoreFragment();
        }
        return groceryStoreFragment;
    }
}
