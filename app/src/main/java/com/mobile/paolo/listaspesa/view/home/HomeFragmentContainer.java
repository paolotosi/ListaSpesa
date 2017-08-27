package com.mobile.paolo.listaspesa.view.home;

import com.mobile.paolo.listaspesa.view.home.group.AddMemberFragment;
import com.mobile.paolo.listaspesa.view.home.group.CreateGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.EmptyGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.ManageGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.ManageGroupProductsFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.CreateShoppingListFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.EmptyShoppingListFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.GroceryStoreFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.ManageShoppingListFragment;
import com.mobile.paolo.listaspesa.view.home.supermarket.CreateSupermarketFragment;
import com.mobile.paolo.listaspesa.view.home.supermarket.EmptySupermarketFragment;
import com.mobile.paolo.listaspesa.view.home.supermarket.ManageSupermarketFragment;
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

    // Group
    private EmptyGroupFragment emptyGroupFragment;
    private ManageGroupFragment manageGroupFragment;
    private CreateGroupFragment createGroupFragment;
    private AddMemberFragment addMemberFragment;
    private ManageGroupProductsFragment manageGroupProductsFragment;

    // Template
    private EmptyTemplateFragment emptyTemplateFragment;
    private ManageTemplateFragment manageTemplateFragment;
    private CreateTemplateFragment createTemplateFragment;

    // Shopping list
    private EmptyShoppingListFragment emptyShoppingListFragment;
    private CreateShoppingListFragment createShoppingListFragment;
    private ManageShoppingListFragment manageShoppingListFragment;
    private GroceryStoreFragment groceryStoreFragment;

    // Supermarket
    private EmptySupermarketFragment emptySupermarketFragment;
    private CreateSupermarketFragment createSupermarketFragment;
    private ManageSupermarketFragment manageSupermarketFragment;

    // Info on fragment stack
    private boolean stackEmpty = true;

    public static synchronized HomeFragmentContainer getInstance()
    {
        if(instance == null)
        {
            instance = new HomeFragmentContainer();
        }
        return instance;
    }

    public void reset()
    {
        instance = null;
    }

    public void setStackEmpty(boolean stackEmpty)
    {
        this.stackEmpty = stackEmpty;
    }

    public boolean isStackEmpty()
    {
        return stackEmpty;
    }

    public EmptyGroupFragment getEmptyGroupFragment() {
        if(this.emptyGroupFragment == null)
        {
            this.emptyGroupFragment = new EmptyGroupFragment();
        }
        return emptyGroupFragment;
    }

    public ManageGroupFragment getManageGroupFragment() {
        if(this.manageGroupFragment == null)
        {
            this.manageGroupFragment = new ManageGroupFragment();
        }
        return manageGroupFragment;
    }

    public ManageGroupProductsFragment getManageGroupProductsFragment() {
        if(this.manageGroupProductsFragment == null)
        {
            this.manageGroupProductsFragment = new ManageGroupProductsFragment();
        }
        return manageGroupProductsFragment;
    }

    public void setManageGroupProductsFragment(ManageGroupProductsFragment manageGroupProductsFragment) {
        this.manageGroupProductsFragment = manageGroupProductsFragment;
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

    public CreateTemplateFragment getCreateTemplateFragment() {
        if(this.createTemplateFragment == null)
        {
            this.createTemplateFragment = new CreateTemplateFragment();
        }
        return createTemplateFragment;
    }

    public void resetCreateTemplateFragment()
    {
        this.createTemplateFragment = null;
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

    public void resetGroceryStoreFragment()
    {
        this.groceryStoreFragment = null;
    }

    public AddMemberFragment getAddMemberFragment() {
        if(this.addMemberFragment == null)
        {
            this.addMemberFragment = new AddMemberFragment();
        }
        return addMemberFragment;
    }

    public EmptySupermarketFragment getEmptySupermarketFragment() {
        if(this.emptySupermarketFragment == null)
        {
            this.emptySupermarketFragment = new EmptySupermarketFragment();
        }
        return emptySupermarketFragment;
    }

    public CreateSupermarketFragment getCreateSupermarketFragment() {
        if(this.createSupermarketFragment == null)
        {
            this.createSupermarketFragment = new CreateSupermarketFragment();
        }
        return createSupermarketFragment;
    }

    public void resetCreateSupermarketFragment()
    {
        this.createSupermarketFragment = null;
    }

    public ManageSupermarketFragment getManageSupermarketFragment() {
        if(this.manageSupermarketFragment == null)
        {
            this.manageSupermarketFragment = new ManageSupermarketFragment();
        }
        return manageSupermarketFragment;
    }
}
