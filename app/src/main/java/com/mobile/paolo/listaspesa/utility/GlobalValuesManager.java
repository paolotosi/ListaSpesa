package com.mobile.paolo.listaspesa.utility;

import android.content.Context;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.model.objects.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This class manages the access to a small number of global variables, such ad the logged user.
 */

public class GlobalValuesManager
{
    private static GlobalValuesManager instance;
    private SharedPreferencesManager sharedPreferencesManager;
    private Context context;

    //ShoppingList state variables
    public static final String NO_LIST = "NO_LIST"; //there is no shopping list for the group
    public static final String LIST_NO_CHARGE = "LIST_NO_CHARGE"; //there is only one list for this group and no one has taken it in charge
    public static final String EMPTY_LIST = "LIST_EMPTY"; //there is a list but it was created as an empty list (not from a template)
    public static final String LIST_IN_CHARGE_LOGGED_USER = "LIST_IN_CHARGE_LOGGED_USER"; //Logged user has taken in charge the list; there isn't another list 'in preparazione'
    public static final String LIST_IN_CHARGE_ANOTHER_USER = "LIST_IN_CHARGE_ANOTHER_USER"; //Group list has taken in charge by another user, as above there is no list 'in preparazione'
    public static final String LIST_IN_CHARGE_ANOTHER_LIST = "LIST_IN_CHARGE_ANOTHER_LIST"; //List is in taken in charge by another user but logged user has initialized another list

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

    public void saveIsUserLogged(boolean isUserLogged)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.is_user_logged), isUserLogged);
    }

    public boolean isUserLogged()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.is_user_logged));
    }


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
        if(!hasUserTemplates())
        {
            return templateList;
        }
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

    public void saveShoppingListState(String state)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.shopping_list_state), state);
    }

    public String getShoppingListState()
    {
        return sharedPreferencesManager.readString(context.getString(R.string.shopping_list_state));
    }

    public void saveUserShoppingList(JSONObject jsonShoppingList)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.logged_user_list), jsonShoppingList.toString());
    }

    public ShoppingList getUserShoppingList()
    {
        ShoppingList shoppingList = new ShoppingList();
        try {
            shoppingList = ShoppingList.fromJSON(new JSONObject(sharedPreferencesManager.readString(context.getString(R.string.logged_user_list))));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return shoppingList;
    }

    public void deleteShoppingList()
    {
        sharedPreferencesManager.writeString(context.getString(R.string.logged_user_list), "");
    }

    public void updateShoppingListProducts(Collection<Product> newProductList)
    {
        ShoppingList shoppingList = getUserShoppingList();
        shoppingList.setProductList(new ArrayList<>(newProductList));
        saveUserShoppingList(shoppingList.toJSON());
    }

    public void removeProductFromShoppingList(Integer productID)
    {
        List<Product> productList = getUserShoppingList().getProductList();
        for(int i = productList.size()-1; i >= 0; i--)
        {
            if(productList.get(i).getID() == productID)
            {
                productList.remove(productList.get(i));
            }
        }

        updateShoppingListProducts(productList);
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

    public Boolean isShoppingListTaken()
    {
        ShoppingList shoppingList= getUserShoppingList();
        return shoppingList.isTaken();
    }

    public void saveShoppingListTaken(boolean newState)
    {
        ShoppingList shoppingList = getUserShoppingList();
        shoppingList.setTaken(newState);
    }

    public void saveUserTookList(String userToSave)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.user_in_charge), userToSave);
    }

    public String getUserTookList()
    {
        return sharedPreferencesManager.readString(context.getString(R.string.user_in_charge));
    }

    public boolean areThereProductsNotFound()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.are_there_products_not_found));
    }

    public void saveAreThereProductsNotFound(boolean areThereProductsNotFound)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.are_there_products_not_found), areThereProductsNotFound);
    }

    public List<Product> getProductsNotFound()
    {
        List<Product> productsNotFound = new ArrayList<>();
        try {
            productsNotFound = Product.parseJSONProductList(new JSONArray(sharedPreferencesManager.readString(context.getString(R.string.products_not_found))));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return productsNotFound;
    }

    public void saveProductsNotFound(JSONArray jsonProductsNotFound)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.products_not_found), jsonProductsNotFound.toString());
    }

    /*
        ------------------
        SUPERMARKET METHODS
        ------------------
     */

    public void saveHasUserSupermarkets(boolean hasUserSupermarkets)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.has_user_supermarkets), hasUserSupermarkets);
    }

    public boolean hasUserSupermarkets()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.has_user_supermarkets));
    }

    public void saveIsUserCreatingSupermarket(boolean isUserCreatingSupermarket)
    {
        sharedPreferencesManager.writeBoolean(context.getString(R.string.is_user_creating_supermarkets), isUserCreatingSupermarket);
    }

    public boolean isUserCreatingSupermarket()
    {
        return sharedPreferencesManager.readBoolean(context.getString(R.string.is_user_creating_supermarkets));
    }

    public void addSupermarket(Supermarket supermarket)
    {
        List<Supermarket> supermarketList = getSupermarkets();
        supermarketList.add(supermarket);
        saveSupermarkets(Supermarket.asJSONSupermarketList(supermarketList));
    }

    public void deleteSupermarket(int supermarketID)
    {
        List<Supermarket> supermarketList = getSupermarkets();
        for(int i = supermarketList.size() - 1; i >= 0; i--)
        {
            if(supermarketList.get(i).getID() == supermarketID)
            {
                supermarketList.remove(supermarketList.get(i));
            }
        }
        saveSupermarkets(Supermarket.asJSONSupermarketList(supermarketList));
    }

    public void deleteSupermarkets(List<Integer> supermarketIDs)
    {
        for(int i = 0; i < supermarketIDs.size(); i++)
        {
            deleteSupermarket(supermarketIDs.get(i));
        }
    }

    public void saveSupermarkets(JSONArray supermarketList)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.supermarket_list), supermarketList.toString());
    }

    public List<Supermarket> getSupermarkets()
    {
        List<Supermarket> supermarketList = new ArrayList<>();
        try {
            supermarketList = Supermarket.parseJSONSupermarketList(new JSONArray(sharedPreferencesManager.readString(context.getString(R.string.supermarket_list))));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return supermarketList;
    }

    public void updateSupermarketsProducts(List<Product> productList, int supermarketID)
    {
        List<Supermarket> supermarketList = getSupermarkets();
        for(Supermarket supermarket : supermarketList)
        {
            // Using sets to avoid duplicates
            Collection<Product> supermarketProductSet = new HashSet<>(supermarket.getProductList());

            if(supermarket.getID() == supermarketID)
            {
                supermarketProductSet.addAll(productList);
                supermarket.setProductList(new ArrayList<>(supermarketProductSet));
            }
        }
        saveSupermarkets(Supermarket.asJSONSupermarketList(supermarketList));
    }


    /*
        ------------------
        PRODUCTS MANAGEMENT METHODS
        ------------------
     */

    public void saveGroupProducts(JSONArray jsonProducts)
    {
        sharedPreferencesManager.writeString(context.getString(R.string.group_products) ,jsonProducts.toString());
    }



    public List<Product> getGroupProducts()
    {
        List<Product> products = new ArrayList<>();

        try {
            products = Product.parseJSONProductList(new JSONArray(sharedPreferencesManager.readString(context.getString(R.string.group_products))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return products;
    }

    public void removeProduct(Integer productID)
    {
        List<Product> productList = getGroupProducts();
        for(int i = productList.size()-1; i >= 0; i--)
        {
            if(productList.get(i).getID() == productID)
            {
                productList.remove(productList.get(i));
            }
        }

        JSONArray products = Product.asJSONProductList(productList);
        saveGroupProducts(products);
    }


}
