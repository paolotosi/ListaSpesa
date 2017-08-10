package com.mobile.paolo.listaspesa.utility;

import com.mobile.paolo.listaspesa.view.home.group.CreateGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.EmptyGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.ManageGroupFragment;
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

    public static synchronized HomeFragmentContainer getInstance()
    {
        if(instance == null)
        {
            instance = new HomeFragmentContainer();
        }
        return instance;
    }

    public EmptyGroupFragment getEmptyGroupFragment() {
        return emptyGroupFragment;
    }

    public void setEmptyGroupFragment(EmptyGroupFragment emptyGroupFragment) {
        this.emptyGroupFragment = emptyGroupFragment;
    }

    public ManageGroupFragment getManageGroupFragment() {
        return manageGroupFragment;
    }

    public void setManageGroupFragment(ManageGroupFragment manageGroupFragment) {
        this.manageGroupFragment = manageGroupFragment;
    }

    public CreateGroupFragment getCreateGroupFragment() {
        return createGroupFragment;
    }

    public void setCreateGroupFragment(CreateGroupFragment createGroupFragment) {
        this.createGroupFragment = createGroupFragment;
    }

    public EmptyTemplateFragment getEmptyTemplateFragment() {
        return emptyTemplateFragment;
    }

    public void setEmptyTemplateFragment(EmptyTemplateFragment emptyTemplateFragment) {
        this.emptyTemplateFragment = emptyTemplateFragment;
    }

    public ManageTemplateFragment getManageTemplateFragment() {
        return manageTemplateFragment;
    }

    public void setManageTemplateFragment(ManageTemplateFragment manageTemplateFragment) {
        this.manageTemplateFragment = manageTemplateFragment;
    }

    public CreateTemplateFragment getCreateTemplateFragment() {
        return createTemplateFragment;
    }

    public void setCreateTemplateFragment(CreateTemplateFragment createTemplateFragment) {
        this.createTemplateFragment = createTemplateFragment;
    }
}
