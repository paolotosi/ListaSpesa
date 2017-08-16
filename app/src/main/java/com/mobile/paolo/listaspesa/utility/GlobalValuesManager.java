package com.mobile.paolo.listaspesa.utility;

import android.content.Context;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.model.objects.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class manages the access to a small number of global variables, such ad the logged user.
 */

public class GlobalValuesManager
{
    private static GlobalValuesManager instance;
    private SharedPreferencesManager sharedPreferencesManager;
    private Context context;

    public static final String NO_LIST = "NO_LIST";
    public static final String LIST_NO_CHARGE = "LIST_NO_CHARGE";
    public static final String LIST_IN_CHARGE_LOGGED_USER = "LIST_IN_CHARGE_LOGGED_USER";
    public static final String LIST_IN_CHARGE_ANOTHER_USER = "LIST_IN_CHARGE_ANOTHER_USER";

    private GlobalValuesManager(Context context)
    {
        this.sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        this.context = context;
    }

    public static synchronized GlobalValuesManager getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new GlobalValuesManager(context);
        }
        return instance;
    }

    /*
        ------------------
        USER METHODS
        ------------------
     */

    public void saveLoggedUser(User loggedUser)
    {
        sharedPreferencesManager.writeString(context.getResources().getString(R.string.logged_user), loggedUser.toJSON().toString());
    }

    public User getLoggedUser()
    {
        String jsonLoggedUser = sharedPreferencesManager.readString(context.getResources().getString(R.string.logged_user));
        User loggedUser = null;
        try {
            loggedUser = new User(new JSONObject(jsonLoggedUser));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loggedUser;
    }

    /*
        ------------------
        GROUP METHODS
        ------------------
     */

    public void saveIsUserCreatingGroup(boolean isUserCreatingGroup)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.is_user_creating_group), isUserCreatingGroup);
    }

    public boolean isUserCreatingGroup()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.is_user_creating_group));
    }

    public void saveIsUserPartOfAGroup(boolean isUserPartOfAGroup)
    {
        sharedPreferencesManager.writeBoolean(context.getResources().getString(R.string.is_user_part_of_a_group), isUserPartOfAGroup);
    }

    public boolean isUserPartOfAGroup()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.is_user_part_of_a_group));
    }

    public void saveLoggedUserGroup(Group group)
    {
        sharedPreferencesManager.writeString(context.getResources().getString(R.string.logged_user_group), group.toJSON().toString());
    }

    public Group getLoggedUserGroup()
    {
        String jsonLoggedUserGroup = sharedPreferencesManager.readString(context.getResources().getString(R.string.logged_user_group));
        Group loggedUserGroup = null;
        try {
            loggedUserGroup = Group.fromJSON(new JSONObject(jsonLoggedUserGroup));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loggedUserGroup;
    }

    /*
        ------------------
        TEMPLATE METHODS
        ------------------
     */

    public void saveIsUserCreatingTemplate(boolean isUserCreatingTemplate)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.is_user_creating_template), isUserCreatingTemplate);
    }

    public boolean isUserCreatingTemplate()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.is_user_creating_template));
    }

    public void saveHasUserTemplates(boolean hasUserTemplates)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.has_user_templates), hasUserTemplates);
    }

    public boolean hasUserTemplates()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.has_user_templates));
    }


    public void saveUserTemplates(List<Template> templateList)
    {
        JSONArray jsonTemplateList = new JSONArray();
        for(int i = 0; i < templateList.size(); i++)
        {
            try {
                jsonTemplateList.put(i, templateList.get(i).toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferencesManager.writeString(context.getString(R.string.logged_user_templates), jsonTemplateList.toString());
    }

    public void saveUserTemplates(JSONArray jsonTemplateList)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.logged_user_templates), jsonTemplateList.toString());
    }

    public void addTemplate(Template template)
    {
        List<Template> userTemplates = getUserTemplates();
        userTemplates.add(template);
        saveUserTemplates(userTemplates);
    }

    public void removeTemplate(Integer templateID)
    {
        List<Template> templateList = getUserTemplates();
        for(int i = templateList.size()-1; i >= 0; i--)
        {
            if(templateList.get(i).getID() == templateID)
            {
                templateList.remove(templateList.get(i));
            }
        }
        saveUserTemplates(templateList);
    }

    public void removeTemplates(List<Integer> templateIDs)
    {
        for(int i = 0; i < templateIDs.size(); i++)
        {
            removeTemplate(templateIDs.get(i));
        }
    }

    public void changeTemplateName(int templateID, String newName)
    {
        List<Template> userTemplates = getUserTemplates();
        for(int i = 0; i < userTemplates.size(); i++)
        {
            if(userTemplates.get(i).getID() == templateID)
            {
                userTemplates.get(i).setName(newName);
            }
        }
        saveUserTemplates(userTemplates);
    }

    public void addTemplateProducts(int templateID, Collection<Product> addList)
    {
        List<Template> userTemplates = getUserTemplates();
        for(int i = 0; i < userTemplates.size(); i++)
        {
            if(userTemplates.get(i).getID() == templateID)
            {
                // Add only products that aren't already present

                // addList \ currentProductsList
                addList.removeAll(userTemplates.get(i).getProductList());

                userTemplates.get(i).getProductList().addAll(addList);
            }
        }
        saveUserTemplates(userTemplates);
    }

    public void removeTemplateProducts(int templateID, Collection<Product> deleteList)
    {
        List<Template> userTemplates = getUserTemplates();
        for(int i = 0; i < userTemplates.size(); i++)
        {
            if(userTemplates.get(i).getID() == templateID)
            {
                userTemplates.get(i).getProductList().removeAll(deleteList);
            }
        }
        saveUserTemplates(userTemplates);
    }

    public List<Template> getUserTemplates()
    {
        List<Template> templateList = new ArrayList<>();
        try {
            JSONArray jsonTemplateList = new JSONArray(sharedPreferencesManager.readString(context.getString(R.string.logged_user_templates)));
            for(int i = 0; i < jsonTemplateList.length(); i++)
            {
                templateList.add(Template.fromJSON(jsonTemplateList.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return templateList;
    }

    public Template getTemplateByID(int templateID)
    {
        List<Template> templateList = getUserTemplates();
        for(int i = 0; i < templateList.size(); i++)
        {
            if(templateList.get(i).getID() == templateID)
            {
                return templateList.get(i);
            }
        }
        return null;
    }

    /*
        ------------------
        SHOPPING LIST METHODS
        ------------------
     */

    public void saveIsUserCreatingShoppingList(boolean isUserCreatingList)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.is_user_creating_list), isUserCreatingList);
    }

    public boolean isUserCreatingShoppingList()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.is_user_creating_list));
    }

    public void saveHasUserShoppingList(boolean hasUserList)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.has_user_list), hasUserList);
    }

    public boolean hasUserShoppingList()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.has_user_list));
    }

    public void saveHasUserShoppingListInCharge(String hasUserListInCharge)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.has_user_list_charge), hasUserListInCharge);
    }

    public String hasUserShoppingListInCharge()
    {
        return sharedPreferencesManager.readString(context.getString(R.string.has_user_list_charge));
    }

    public void saveUserShoppingList(JSONObject jsonShoppingList)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.logged_user_list), jsonShoppingList.toString());
    }

    public void saveUserShoppingList(JSONArray jsonShoppingList)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.logged_user_list), jsonShoppingList.toString());
    }

    public ShoppingList getUserShoppingList()
    {
        ShoppingList shoppingList = null;
        try {
            shoppingList = ShoppingList.fromJSON(new JSONObject(sharedPreferencesManager.readString(context.getString(R.string.logged_user_list))));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return shoppingList;
    }

    public void saveUserShoppingList(List<ShoppingList> shoppingList)
    {
        JSONArray jsonShoppingList = new JSONArray();
        for(int i = 0; i < shoppingList.size(); i++)
        {
            try {
                jsonShoppingList.put(i, shoppingList.get(i).toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferencesManager.writeString(context.getString(R.string.logged_user_list), jsonShoppingList.toString());
    }

    public void updateShoppingList(Collection<Product> newProductList)
    {
        ShoppingList shoppingList = getUserShoppingList();
        shoppingList.setProductList(new ArrayList<Product>(newProductList));
        saveUserShoppingList(shoppingList.toJSON());
    }

    public void addProductsToShoppingList(Collection<Product> addList)
    {
        ShoppingList shoppingList = getUserShoppingList();

        // Remove products already present from addList
        addList.removeAll(shoppingList.getProductList());

        // Add the addList to the product list
        shoppingList.getProductList().addAll(addList);

        saveUserShoppingList(shoppingList.toJSON());
    }

    public void removeProductsFromShoppingList(Collection<Product> deleteList)
    {
        ShoppingList shoppingList = getUserShoppingList();

        shoppingList.getProductList().removeAll(deleteList);

        saveUserShoppingList(shoppingList.toJSON());
    }

    public Boolean getShoppingListState()
    {
        ShoppingList shoppingList= getUserShoppingList();
        return shoppingList.getState();
    }

    public void setShoppingListState(Boolean newState)
    {
        ShoppingList shoppingList = getUserShoppingList();
        shoppingList.setState(newState);
    }

//    public ShoppingList getUserShoppingList()
//    {
//        ShoppingList shoppingList = null;
//        int groupID = getLoggedUserGroup().getID();
//        try {
//            JSONArray shoppingListArray = new JSONArray(sharedPreferencesManager.readString(context.getString(R.string.logged_user_list)));
//            JSONObject jsonShoppingList = new JSONObject();
//            jsonShoppingList.put("groupID", String.valueOf(groupID));
//            jsonShoppingList.put("list", shoppingListArray);
//            shoppingList = ShoppingList.fromJSON(jsonShoppingList);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return shoppingList;
//    }

}
