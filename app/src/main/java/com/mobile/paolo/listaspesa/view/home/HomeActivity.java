package com.mobile.paolo.listaspesa.view.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.remote.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.ProductsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.ShoppingListDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.SupermarketDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * -- HomeActivity --
 * The central activity in the application.
 * It's based on a bottom bar navigation layout: when the user selects a tab, the
 * view is updated by loading the corresponding fragment.
 * Each tab has different fragments, for example the "Gruppo" tab has a creation and management view.
 * This calls for the necessity of contextualizing the application before setting up the tabs.
 * We need to know:
 * - which user is logged
 * - if he's already part of a group
 * - if his group has already defined some templates
 * This info is gathered from the database as soon as the activity is loaded and saved in
 * SharedPreferences for future uses.
 */

public class HomeActivity extends AppCompatActivity
{
    // Constants
    private static final int SUPERMARKET_TAB = 0;
    private static final int TEMPLATE_TAB = 1;
    private static final int SHOPPING_LIST_TAB = 2;
    private static final int GROUP_TAB = 3;
    
    // The networkResponseHandlers
    private NetworkResponseHandler groupResponseHandler;
    private NetworkResponseHandler templateResponseHandler;
    private NetworkResponseHandler shoppingListResponseHandler;
    private NetworkResponseHandler getProductsNotFoundResponseHandler;
    private NetworkResponseHandler supermarketsResponseHandler;
    private NetworkResponseHandler groupProductsResponseHandler;
    
    // Response codes
    private static final int NETWORK_ERROR = 0;
    private static final int USER_HAS_GROUP = 1;
    private static final int USER_DOESNT_HAVE_GROUP = 2;
    
    // The bottom navigation view
    BottomNavigationViewEx bottomNavigationView;
    
    // GlobalValuesManager
    GlobalValuesManager contextualizer;
    
    private boolean groupRequestFinished = false;
    private boolean templateRequestFinished = false;
    private boolean shoppingListRequestFinished = false;
    private boolean productsNotFoundRequestFinished = true;
    private boolean supermarketRequestFinished = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_home);
        
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        
        // Only to shorten lines
        contextualizer = GlobalValuesManager.getInstance(getApplicationContext());
        
        // From here on out, everything is handled by the bottom navigation listener
        setupBottomNavigationView();
        
        // Determine the context: logged user, group, templates...
        contextualize();
        
    }
    
    private void contextualize()
    {
        // Gets all the info about the user.
        // The group ID is received from the login activity via extras or is loaded from
        // the SharedPreferences (if the user re-opened the app after exiting it without logging out).
        // NOTE: it also contains the request to verify if the group has already defined some templates,
        // a shopping list or has some products left from previous lists.
        // These requests have to be sent only after the first one is finished, because we don't know
        // the groupID beforehand
        
        Bundle extras = getIntent().getExtras();
        // I came here from the login/register activity
        if (extras != null)
        {
            if (extras.containsKey("GROUP_ID"))
            {
                // Check if the user has a group
                if (Integer.parseInt(extras.getString("GROUP_ID")) == -1)
                {
                    // No group
                    GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(false);
                } else
                {
                    // Get the info about the group
                    sendGetGroupDetailsRequest(Integer.parseInt(extras.getString("GROUP_ID")));
                }
            }
        }
        // I came here because the user was already logged
        else
        {
            if (contextualizer.isUserPartOfAGroup())
            {
                sendGetGroupDetailsRequest(GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID());
            }
        }
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, selectFirstFragment());
        transaction.commit();
    }
    
    private void setupBottomNavigationView()
    {
        //Get the BottomNavigationView object
        bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.home_bottom_navigation);
        //Set various aspect of BNV
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.enableItemShiftingMode(false);
        
        //Associating the listener
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem selectedTab)
                    {
                        // Select the fragment to load
                        Fragment selectedFragment = selectCorrectFragment(selectedTab);
                        
                        // If there's a fragment in the stack (e.g. state in which is shown the 'Up'
                        // button require to save the previous fragment), pop it
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        HomeFragmentContainer.getInstance().setStackEmpty(true);
                        
                        // Replace main view with correct fragment
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.home_main_content, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });
        
        // Manually displaying the loading fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, new ProgressBarFragment());
        transaction.commit();
        
        //Used to select an item programmatically
        //bottomNavigationView.getMenu().getItem(2).setChecked(true);
    }
    
    private void setupGroupResponseHandler()
    {
        this.groupResponseHandler = new NetworkResponseHandler()
        {
            @Override
            public void onSuccess(JSONObject response)
            {
                // Debug
                Log.d("GET_GROUP_RESP", response.toString());
                groupRequestFinished = true;
                try
                {
                    int responseCode = response.getInt("success");
                    switch (responseCode)
                    {
                        case NETWORK_ERROR:
                            break;
                        case USER_HAS_GROUP:
                            // Create group from response and updated SharedPreferences
                            GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(true);
                            Log.d("MEMBERS", response.getJSONArray("members").toString());
                            Group group = new Group(response.getInt("groupID"), response.getString("groupName"), response.getJSONArray("members"));
                            GlobalValuesManager.getInstance(getApplicationContext()).saveLoggedUserGroup(group);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(true);
                            // Query for templates, shopping list and products not found only if the user has a group.
                            // The request needs to be sent now, otherwise we don't know the group ID!
                            sendGetGroupProductsRequest();
                            sendGetTemplatesRequest();
                            sendGetShoppingListRequest();
                            sendGetProductsNotFoundRequest();
                            sendGetAllSupermarketsRequest();
                            break;
                        case USER_DOESNT_HAVE_GROUP:
                            GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(false);
                            switchToFirstFragment();
                            break;
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
                
            }
            
            @Override
            public void onError(VolleyError error)
            {
                error.printStackTrace();
            }
        };
    }
    
    // Determine if a user is part of a group; in that case, fetch the group details, including the templates
    private void sendGetGroupDetailsRequest(int groupID)
    {
        setupGroupResponseHandler();
        
        // User loggedUser = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUser();
        
        // The POST parameters.
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(groupID)); // ((Integer) (loggedUser.getID())).toString());
        
        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);
        
        // Print parameters to console for debug purposes.
        Log.d("GET_GROUP_REQ", jsonPostParameters.toString());
        
        GroupsDatabaseHelper.sendGetGroupDetailsRequest(jsonPostParameters, getApplicationContext(), groupResponseHandler);
        
    }
    
    private void sendGetGroupProductsRequest()
    {
        setupGroupProductsResponseHandler();
        
        JSONObject jsonPost = new JSONObject();
        String groupID = String.valueOf(GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID());
        
        try
        {
            jsonPost.put("id", groupID);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        GroupsDatabaseHelper.sendGetGroupProductsRequest(jsonPost, getApplicationContext(), groupProductsResponseHandler);
    }
    
    private void setupGroupProductsResponseHandler()
    {
        this.groupProductsResponseHandler = new NetworkResponseHandler()
        {
            @Override
            public void onSuccess(JSONObject response)
            {
                Log.d("GET_PRODUCTS_RESP", response.toString());
                try
                {
                    if (response.getInt("success") == 1)
                    {
                        // Determine if the group has templates and update the SharedPreferences accordingly
                        JSONArray products = response.getJSONArray("products");
                        GlobalValuesManager.getInstance(getApplicationContext()).saveGroupProducts(products);
                    } else
                    {
                        
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onError(VolleyError error)
            {
                error.printStackTrace();
            }
        };
    }
    
    private void setupTemplateResponseHandler()
    {
        this.templateResponseHandler = new NetworkResponseHandler()
        {
            @Override
            public void onSuccess(JSONObject response)
            {
                try
                {
                    Log.d("GET_TEMPLATES_RESP", response.toString());
                    if (response.getInt("success") == 1)
                    {
                        templateRequestFinished = true;
                        // Determine if the group has templates and update the SharedPreferences accordingly
                        JSONArray templates = response.getJSONArray("templates");
                        if (templates.length() == 0)
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(false);
                        } else
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(true);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveUserTemplates(templates);
                        }
                        if (initializationDone())
                        {
                            switchToFirstFragment();
                        }
                    } else
                    {
                        GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(false);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onError(VolleyError error)
            {
                error.printStackTrace();
            }
        };
    }
    
    private void sendGetTemplatesRequest()
    {
        // Define what to do on response
        setupTemplateResponseHandler();
        
        Integer groupID = -1;
        
        if (GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup() != null)
        {
            groupID = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID();
        }
        
        // The POST parameters
        Map<String, String> params = new HashMap<>();
        params.put("groupID", groupID.toString());
        
        // Encapsulate in JSON
        JSONObject jsonPostParameters = new JSONObject(params);
        
        // Debug
        Log.d("GET_TEMPLATES_REQ", jsonPostParameters.toString());
        
        // Send request
        TemplatesDatabaseHelper.sendGetGroupTemplatesRequest(jsonPostParameters, getApplicationContext(), templateResponseHandler);
        
    }
    
    private void setupShoppingListResponseHandler()
    {
        /*
            Possible outcomes:
                - server error
                - list in charge, taken by me -> show grocery store
                - list in charge, not taken by me -> show list in creation
         */
        this.shoppingListResponseHandler = new NetworkResponseHandler()
        {
            @Override
            public void onSuccess(JSONObject response)
            {
                try
                {
                    Log.d("GET_LIST_RESP", response.toString());
                    shoppingListRequestFinished = true;
                    if (response.getInt("success") == 1)
                    {
                        // List is not taken
                        JSONObject jsonShoppingList = response.getJSONObject("list");
                        ShoppingList shoppingList = ShoppingList.fromJSON(jsonShoppingList);
                        if (shoppingList.getProductList().size() == 0)
                        {
                            // There is no list
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserShoppingList(false);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveShoppingListState(GlobalValuesManager.NO_LIST);
                        } else
                        {
                            // List is present and it's not empty
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserShoppingList(true);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveShoppingListState(GlobalValuesManager.LIST_NO_CHARGE);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveUserShoppingList(shoppingList.toJSON());
                        }
                    } else if (response.getInt("success") == 2)
                    //In this case there is a list taken by someone, I have to evaluate which user took the list
                    {
                        // List is taken
                        if (response.getInt("userID") == GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUser().getID())
                        {
                            // By me
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserShoppingList(true);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveShoppingListState(GlobalValuesManager.LIST_IN_CHARGE_LOGGED_USER);
                        } else
                        {
                            // Not by me
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserShoppingList(true);
                            getUserTookList(response.getInt("userID"));
                            JSONObject jsonShoppingList = response.getJSONObject("list");
                            ShoppingList shoppingList = ShoppingList.fromJSON(jsonShoppingList);
                            if (shoppingList.getProductList().size() > 0)
                            {
                                GlobalValuesManager.getInstance(getApplicationContext()).saveUserShoppingList(shoppingList.toJSON());
                                GlobalValuesManager.getInstance(getApplicationContext()).saveShoppingListState(GlobalValuesManager.LIST_IN_CHARGE_ANOTHER_LIST);
                                
                            } else
                            {
                                GlobalValuesManager.getInstance(getApplicationContext()).saveShoppingListState(GlobalValuesManager.LIST_IN_CHARGE_ANOTHER_USER);
                            }
                        }
                    } else
                    {
                        // Error
                        GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserShoppingList(false);
                        GlobalValuesManager.getInstance(getApplicationContext()).saveShoppingListState(GlobalValuesManager.NO_LIST);
                    }
                    if (initializationDone())
                    {
                        switchToFirstFragment();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onError(VolleyError error)
            {
                error.printStackTrace();
            }
        };
    }
    
    private void sendGetShoppingListRequest()
    {
        // Define what to do on response
        setupShoppingListResponseHandler();
        
        Integer groupID = -1;
        
        if (GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup() != null)
        {
            groupID = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID();
        }
        
        // The POST parameters
        Map<String, String> params = new HashMap<>();
        params.put("groupID", groupID.toString());
        
        // Encapsulate in JSON
        JSONObject jsonPostParameters = new JSONObject(params);
        
        // Debug
        Log.d("GET_LIST_REQ", jsonPostParameters.toString());
        
        // Send request
        ShoppingListDatabaseHelper.sendGetGroupListRequest(jsonPostParameters, getApplicationContext(), shoppingListResponseHandler);
        
    }
    
    private void setupGetProductsNotFoundResponseHandler()
    {
        this.getProductsNotFoundResponseHandler = new NetworkResponseHandler()
        {
            @Override
            public void onSuccess(JSONObject response)
            {
                Log.d("GET_PROD_NOT_FOUND_RESP", response.toString());
                productsNotFoundRequestFinished = true;
                try
                {
                    if (response.getInt("success") == 1)
                    {
                        // -- Success --
                        
                        // Get the products
                        JSONArray jsonProductsNotFound = response.getJSONArray("products_not_found");
                        
                        if (jsonProductsNotFound.length() > 0)
                        {
                            // Save them in cache
                            GlobalValuesManager.getInstance(getApplicationContext()).saveAreThereProductsNotFound(true);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveProductsNotFound(jsonProductsNotFound);
                        } else
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveAreThereProductsNotFound(false);
                        }
                        if (initializationDone())
                        {
                            switchToFirstFragment();
                        }
                    } else
                    {
                        // -- Error --
                        Log.e("GET_PROD_NOT_FOUND_ERR", response.getString("message"));
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onError(VolleyError error)
            {
                error.printStackTrace();
            }
        };
    }
    
    private void sendGetProductsNotFoundRequest()
    {
        // Parameters
        int groupID = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID();
        
        // Encapsulate in JSON
        JSONObject jsonParams = new JSONObject();
        try
        {
            jsonParams.put("groupID", groupID);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        // Debug
        Log.d("GET_PROD_NOT_FOUND_REQ", jsonParams.toString());
        
        // Define what to do on response
        setupGetProductsNotFoundResponseHandler();
        
        // Send request
        ProductsDatabaseHelper.sendGetProductsNotFoundRequest(jsonParams, getApplicationContext(), getProductsNotFoundResponseHandler);
        
    }
    
    private void setupSupermarketResponseHandler()
    {
        this.supermarketsResponseHandler = new NetworkResponseHandler()
        {
            @Override
            public void onSuccess(JSONObject response)
            {
                Log.d("GET_ALL_SUPERMARKET_RES", response.toString());
                supermarketRequestFinished = true;
                try
                {
                    if (response.getInt("success") == 1)
                    {
                        GlobalValuesManager.getInstance(getApplicationContext()).saveSupermarkets(response.getJSONArray("supermarkets"));
                        if (GlobalValuesManager.getInstance(getApplicationContext()).getSupermarkets().size() > 0)
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserSupermarkets(true);
                        }
                    }
                    if (initializationDone())
                    {
                        switchToFirstFragment();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onError(VolleyError error)
            {
                
            }
        };
    }
    
    private void sendGetAllSupermarketsRequest()
    {
        // Debug
        Log.d("GET_ALL_SUPERMARKET_REQ", "Request sent");
        
        // Define what to do on response
        setupSupermarketResponseHandler();
        
        JSONObject toSend = new JSONObject();
        try
        {
            toSend.put("id", String.valueOf(GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID()));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        // Send request
        SupermarketDatabaseHelper.sendGetAllSupermarketsRequest(toSend, getApplicationContext(), supermarketsResponseHandler);
        
    }
    
    // Define which fragment to load based on context
    private Fragment selectCorrectFragment(MenuItem selectedTab)
    {
        Fragment selectedFragment = null;
        switch (selectedTab.getItemId())
        {
            case R.id.tab_supermarket:
                selectedFragment = selectSupermarketFragment();
                break;
            case R.id.tab_templates:
                selectedFragment = selectTemplateFragment();
                break;
            case R.id.tab_list:
                selectedFragment = selectListFragment();
                break;
            case R.id.tab_group:
                selectedFragment = selectGroupFragment();
                break;
        }
        return selectedFragment;
    }
    
    // --- Following methods establish for every fragment set which fragment has to be shown in the app.
    
    private Fragment selectTemplateFragment()
    {
        Fragment selectedFragment;
        if (contextualizer.hasUserTemplates() && !contextualizer.isUserCreatingTemplate())
        {
            // ManageTemplate
            selectedFragment = HomeFragmentContainer.getInstance().getManageTemplateFragment();
        } else if (contextualizer.isUserCreatingTemplate())
        {
            // CreateTemplate
            selectedFragment = HomeFragmentContainer.getInstance().getCreateTemplateFragment();
        } else
        {
            // EmptyTemplate
            selectedFragment = HomeFragmentContainer.getInstance().getEmptyTemplateFragment();
        }
        return selectedFragment;
    }
    
    private Fragment selectGroupFragment()
    {
        Fragment selectedFragment;
        if (contextualizer.isUserPartOfAGroup())
        {
            // ManageGroup
            selectedFragment = HomeFragmentContainer.getInstance().getManageGroupFragment();
        } else if (contextualizer.isUserCreatingGroup())
        {
            // CreateGroup
            selectedFragment = HomeFragmentContainer.getInstance().getCreateGroupFragment();
        } else
        {
            // EmptyGroup
            selectedFragment = HomeFragmentContainer.getInstance().getEmptyGroupFragment();
        }
        return selectedFragment;
    }
    
    private Fragment selectListFragment()
    {
        Fragment selectedFragment;
        Log.d("hasUSerShoppingList", String.valueOf(contextualizer.hasUserShoppingList()));
        if (contextualizer.hasUserShoppingList())
        {
            // -- LIST PRESENT --
            if (contextualizer.getShoppingListState().equalsIgnoreCase(GlobalValuesManager.LIST_NO_CHARGE) || contextualizer.getShoppingListState().equalsIgnoreCase(GlobalValuesManager.EMPTY_LIST))
            {
                // -- NOT TAKEN --
                // ManageShoppingList
                selectedFragment = HomeFragmentContainer.getInstance().getManageShoppingListFragment();
            } else if (contextualizer.getShoppingListState().equalsIgnoreCase(GlobalValuesManager.LIST_IN_CHARGE_LOGGED_USER))
            {
                // -- TAKEN BY ME --
                // GroceryStoreFragment
                selectedFragment = HomeFragmentContainer.getInstance().getGroceryStoreFragment();
            } else if (contextualizer.getShoppingListState().equalsIgnoreCase(GlobalValuesManager.LIST_IN_CHARGE_ANOTHER_USER))
            {
                // -- TAKEN BY SOMEONE ELSE, NO SECOND LIST --
                // EmptyShoppingList: it will be different because the user has a list but it's taken in charge by someone else
                selectedFragment = HomeFragmentContainer.getInstance().getEmptyShoppingListFragment();
            } else
            {
                // -- TAKEN BY SOMEONE ELSE, SECOND LIST PRESENT --
                selectedFragment = HomeFragmentContainer.getInstance().getManageShoppingListFragment();
            }
        } else if (contextualizer.isUserCreatingShoppingList())
        {
            // -- LIST IN CREATION --
            // CreateShoppingList
            selectedFragment = HomeFragmentContainer.getInstance().getCreateShoppingListFragment();
        } else
        {
            // -- NO LIST --
            // EmptyShoppingList
            selectedFragment = HomeFragmentContainer.getInstance().getEmptyShoppingListFragment();
        }
        return selectedFragment;
    }
    
    private String getUserTookList(int id)
    {
        List<User> members = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getMembers();
        String userInCharge = "";
        for (int i = 0; i < members.size(); i++)
        {
            if (GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getMembers().get(i).getID() == id)
            {
                userInCharge = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getMembers().get(i).getUsername();
                GlobalValuesManager.getInstance(getApplicationContext()).saveUserTookList(userInCharge);
            }
        }
        return userInCharge;
    }
    
    private Fragment selectSupermarketFragment()
    {
        Fragment selectedFragment;
        if (!contextualizer.hasUserSupermarkets())
        {
            selectedFragment = HomeFragmentContainer.getInstance().getEmptySupermarketFragment();
        } else if (contextualizer.isUserCreatingSupermarket())
        {
            selectedFragment = HomeFragmentContainer.getInstance().getCreateSupermarketFragment();
        } else
        {
            selectedFragment = HomeFragmentContainer.getInstance().getManageSupermarketFragment();
        }
        return selectedFragment;
    }
    
    
    //This method establish which fragment is shown first to the user in relation to the certain conditons
    private Fragment selectFirstFragment()
    {
        Fragment firstFragment;
        
        // If the user doesn't have a group, show the empty group fragment
        if (!contextualizer.isUserPartOfAGroup())
        {
            firstFragment = HomeFragmentContainer.getInstance().getEmptyGroupFragment();
            bottomNavigationView.getMenu().getItem(GROUP_TAB).setChecked(true);
            return firstFragment;
        }
        
        // If the user has a group, but the group hasn't defined template, show the empty template fragment
        if (!contextualizer.hasUserTemplates())
        {
            firstFragment = HomeFragmentContainer.getInstance().getEmptyTemplateFragment();
            bottomNavigationView.getMenu().getItem(TEMPLATE_TAB).setChecked(true);
            return firstFragment;
        }
        
        // If the user group has some templates, but not a list, show the empty list fragment
        if (!contextualizer.hasUserShoppingList())
        {
            firstFragment = HomeFragmentContainer.getInstance().getEmptyShoppingListFragment();
            bottomNavigationView.getMenu().getItem(SHOPPING_LIST_TAB).setChecked(true);
            return firstFragment;
        }
        
        // The user has a list, return the correct list fragment
        bottomNavigationView.getMenu().getItem(SHOPPING_LIST_TAB).setChecked(true);
        return selectListFragment();
    }
    
    //Needed to know if everything is inizialized
    private boolean initializationDone()
    {
        return groupRequestFinished && templateRequestFinished && shoppingListRequestFinished && supermarketRequestFinished && productsNotFoundRequestFinished;
    }
    
    private void switchToFirstFragment()
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, selectFirstFragment());
        transaction.commit();
    }
    
    
}
