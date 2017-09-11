package com.mobile.paolo.listaspesa.model.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.remote.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutBinding;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutEditBinding;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutGroceryBinding;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutManageBinding;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutShoppingListBinding;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.group.InsertProductsActivity;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by paolo on 04/08/17.
 */

/**
 * This file contains two classes: ProductCardViewAdapter and ViewHolder
 * First class contains methods that specifies recycler view cards behaviour, binding and operations
 * Second class is the associated view holder
 */

public class ProductCardViewDataAdapter extends SelectableAdapter<ProductCardViewDataAdapter.ViewHolder>
{
    // The data to show
    private SortedList<Product> sortedList;
    private static Comparator<Product> alphabeticalComparator;

    // Used in edit mode to store the products to delete
    private List<Product> deleteList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();

    public Boolean visibility;

    // Mode: add or edit
    private int mode;
    public static final int ADD_MODE = 1;
    public static final int EDIT_MODE = 2;
    public static final int LIST_MODE = 3;
    public static final int GROCERY_MODE = 4;
    public static final int MANAGE_MODE = 5;

    private NetworkResponseHandler deleteProductResponseHandler;

    // ClickListener (received from the outside)
    ViewHolder.ClickListener clickListener;

    public ProductCardViewDataAdapter(int mode)
    {
        this.mode = mode;
        setupProductComparator();
        setupSortedList();
    }

    public ProductCardViewDataAdapter(int mode, ViewHolder.ClickListener clickListener)
    {
        this.mode = mode;
        this.clickListener = clickListener;
        setupProductComparator();
        setupSortedList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding productBinding = null;
        switch (mode)
        {
            case ADD_MODE:  productBinding = CardProductLayoutBinding.inflate(layoutInflater, parent, false); break;
            case EDIT_MODE: productBinding = CardProductLayoutEditBinding.inflate(layoutInflater, parent, false); break;
            case LIST_MODE: productBinding = CardProductLayoutShoppingListBinding.inflate(layoutInflater, parent, false);
                                             return new ViewHolder(productBinding, clickListener);
            case GROCERY_MODE: productBinding = CardProductLayoutGroceryBinding.inflate(layoutInflater, parent, false); break;
            case MANAGE_MODE: productBinding = CardProductLayoutManageBinding.inflate(layoutInflater, parent, false); break;
        }
        return new ViewHolder(productBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position)
    {
        // Automatic binding defined via XML
        Product product = sortedList.get(position);
        int bindingType = viewHolder.bind(product);

        switch (bindingType)
        {
            case ADD_MODE:  setupAddMode(viewHolder, position); break;
            case EDIT_MODE: setupEditMode(viewHolder, position); break;
            case LIST_MODE: setupListMode(viewHolder, position); break;
            case GROCERY_MODE: setupGroceryMode(viewHolder, position); break;
            case MANAGE_MODE: setupManageMode(viewHolder, position); break;
        }
    }

    @Override
    public int getItemCount() {
        return sortedList.size();
    }

    private void setupAddMode(final ViewHolder viewHolder, final int position)
    {
        // Cast superclass binding to add mode binding
        CardProductLayoutBinding binding = (CardProductLayoutBinding) viewHolder.binding;

        // Custom logic for product description
        if(sortedList.get(position).getDescription() != null)
        {
            if(sortedList.get(position).getDescription().equals("null"))
            {
                String noDescriptionText = binding.productDescription.getContext().getString(R.string.no_description_message);
                binding.productDescription.setText(noDescriptionText);
            }
        }

        // Since the RecyclerView reuses elements, we need a way to remember which products where checked
        binding.productCheckbox.setChecked(sortedList.get(position).isChecked());

        // Save the product in the tag field of the checkbox, it'll be used later.
        binding.productCheckbox.setTag(sortedList.get(position));

        // When a checkbox is clicked:
        binding.productCheckbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Retrieve the corresponding product.
                CheckBox checkbox = (CheckBox) v;
                Product product = (Product) checkbox.getTag();

                // Set the 'checked' field of the product both in the checkbox tag field and in the list
                product.setChecked(checkbox.isChecked());
                sortedList.get(viewHolder.getAdapterPosition()).setChecked(checkbox.isChecked());
            }
        });
    }

    private void setupEditMode(final ViewHolder viewHolder, final int position)
    {
        // Cast superclass binding to edit mode binding
        CardProductLayoutEditBinding binding = (CardProductLayoutEditBinding) viewHolder.binding;

        // Custom logic for product description
        if(sortedList.get(position).getDescription() != null)
        {
            if(sortedList.get(position).getDescription().equals("null"))
            {
                String noDescriptionText = binding.productDescription.getContext().getString(R.string.no_description_message);
                binding.productDescription.setText(noDescriptionText);
            }
        }

        binding.deleteProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteList.add(sortedList.get(viewHolder.getAdapterPosition()));
                sortedList.remove(sortedList.get(viewHolder.getAdapterPosition()));
            }
        });
    }

    private void setupListMode(final ViewHolder viewHolder, final int position)
    {
        // Cast superclass binding to list mode binding
        CardProductLayoutShoppingListBinding binding = (CardProductLayoutShoppingListBinding) viewHolder.binding;

        // Custom logic for product description
        if(sortedList.get(position).getDescription() != null)
        {
            if(sortedList.get(position).getDescription().equals("null"))
            {
                String noDescriptionText = binding.productDescription.getContext().getString(R.string.no_description_message);
                binding.productDescription.setText(noDescriptionText);
            }
        }

        // Set quantity
        binding.quantityPicker.setValue(sortedList.get(position).getQuantity());

        // Listen to NumberPicker changes
        binding.quantityPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                sortedList.get(position).setQuantity(newVal);
            }
        });

        // Show transparent overlay if product is selected in action mode
        binding.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupGroceryMode(final ViewHolder viewHolder, final int position)
    {
        // Cast superclass binding to add mode binding
        CardProductLayoutGroceryBinding binding = (CardProductLayoutGroceryBinding) viewHolder.binding;

        // Custom logic for product quantity
        binding.productQuantity.setText(String.valueOf(sortedList.get(position).getQuantity()));

        // Since the RecyclerView reuses elements, we need a way to remember which products where checked
        binding.productCheckbox.setChecked(sortedList.get(position).isChecked());

        // Save the product in the tag field of the checkbox, it'll be used later.
        binding.productCheckbox.setTag(sortedList.get(position));

        // When a checkbox is clicked:
        binding.productCheckbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Retrieve the corresponding product.
                CheckBox checkbox = (CheckBox) v;
                Product product = (Product) checkbox.getTag();

                // Set the 'checked' field of the product both in the checkbox tag field and in the list
                product.setChecked(checkbox.isChecked());
                sortedList.get(viewHolder.getAdapterPosition()).setChecked(checkbox.isChecked());
            }
        });
    }

    private void setupManageMode(final ViewHolder viewHolder, final int position) {
        // Cast superclass binding to edit mode binding
        CardProductLayoutManageBinding binding = (CardProductLayoutManageBinding) viewHolder.binding;

        if(sortedList.get(position).getDescription() != null)
        {
            if(sortedList.get(position).getDescription().equals("null") || sortedList.get(position).getDescription().equalsIgnoreCase(""))
            {
                String noDescriptionText = binding.productDescription.getContext().getString(R.string.no_description_message);
                binding.productDescription.setText(noDescriptionText);
            }
        }

        // Custom logic for product description
        if (sortedList.get(position).getDescription() != null) {
            if (sortedList.get(position).getDescription().equals("null")) {
                String noDescriptionText = binding.productDescription.getContext().getString(R.string.no_description_message);
                binding.productDescription.setText(noDescriptionText);
            }
        }

        Log.d("Matrix", sortedList.get(position).getMatrix().toString());
        if (!sortedList.get(position).getMatrix())
        {
            binding.editProductButton.setVisibility(View.VISIBLE);
            binding.deleteProdButton.setVisibility(View.VISIBLE);

            binding.deleteProdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showDialog(v, viewHolder);
                }
            });

        binding.editProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), InsertProductsActivity.class);
                intent.putExtra("flag", false);
                intent.putExtra("product", sortedList.get(position).toJSON().toString());
                v.getContext().startActivity(intent);
            }
        });
        }
        else
        {
            binding.editProductButton.setVisibility(View.GONE);
            binding.deleteProdButton.setVisibility(View.GONE);
        }
    }

    private void showDialog(final View v,final ViewHolder viewHolder)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext(), R.style.Theme_AppCompat_Light_Dialog);

        dialogBuilder.setMessage(v.getContext().getString(R.string.delete_dialog));
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                v.getContext().getString(R.string.delete_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendDeleteProductRequest(sortedList.get(viewHolder.getAdapterPosition()).getID(), v.getContext(), viewHolder.getAdapterPosition());
                    }
                });

        dialogBuilder.setNegativeButton(
                v.getContext().getString(R.string.cancel_action),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(v.getContext().getColor(R.color.materialRed500));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(v.getContext().getColor(R.color.materialGrey600));
    }

    private void sendDeleteProductRequest(int id, Context context, int position)
    {
        setupDeleteProductResponseHandler(context, position);

        JSONObject jsonPost = new JSONObject();
        try {

            jsonPost.put("productID", String.valueOf(id));
            jsonPost.put("id", String.valueOf(GlobalValuesManager.getInstance(context).getLoggedUserGroup().getID()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("DELETE_PRODUCT_REQ", jsonPost.toString());

        GroupsDatabaseHelper.sendDeleteProductRequest(jsonPost, context, deleteProductResponseHandler);
    }

    private void setupDeleteProductResponseHandler(final Context context,final int position)
    {
        this.deleteProductResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("DELETE_PRODUCT_RESP", response.toString());
                    if(response.getInt("success") == 1)
                    {
                        Toast.makeText(context, "Eliminazione avvenuta", Toast.LENGTH_SHORT).show();
                        GlobalValuesManager.getInstance(context).removeProduct(sortedList.get(position).getID());
                        deleteList.add(sortedList.get(position));
                        sortedList.remove(sortedList.get(position));
                    }
                    else
                    {
                        Toast.makeText(context, "Problemi durante l'eliminazione", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    private void setupSortedList()
    {
        SortedList.Callback<Product> sortedListCallback = new SortedList.Callback<Product>() {
            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public int compare(Product p1, Product p2) {
                return alphabeticalComparator.compare(p1, p2);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Product oldItem, Product newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(Product item1, Product item2) {
                return (item1.getName().equalsIgnoreCase(item2.getName()) && item1.getBrand().equalsIgnoreCase(item2.getBrand()));
            }
        };

        this.sortedList = new SortedList<>(Product.class, sortedListCallback);
    }

    private void setupProductComparator()
    {
        alphabeticalComparator = new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return p1.getName().compareTo(p2.getName());
            }
        };
    }

    public void add(Product product) {
        sortedList.add(product);
    }

    public void remove(Product product) {
        sortedList.remove(product);
    }

    public void add(Collection<Product> models) {
        sortedList.addAll(models);
    }

    public void remove(List<Product> products) {
        sortedList.beginBatchedUpdates();
        for (Product model : products) {
            sortedList.remove(model);
        }
        sortedList.endBatchedUpdates();
    }

    public void replaceAll(List<Product> models) {
        sortedList.beginBatchedUpdates();
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            final Product model = sortedList.get(i);
            if (!models.contains(model)) {
                sortedList.remove(model);
            }
        }
        sortedList.addAll(models);
        sortedList.endBatchedUpdates();
    }

    public void removeItem(int position) {
        sortedList.removeItemAt(position);
        notifyItemRemoved(position);
    }

    public void removeItems(List<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    ++count;
                }

                if (count == 1) {
                    removeItem(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }

                for (int i = 0; i < count; ++i) {
                    positions.remove(0);
                }
            }
        }
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            sortedList.removeItemAt(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public SortedList<Product> getModel()
    {
        return sortedList;
    }

    public List<Integer> getCheckedProductsIndexes()
    {
        List<Integer> selectedProductsIndexes = new ArrayList<>();
        for(int i = 0; i < getModelAsCollection().size(); i++)
        {
            if(getModelAsCollection().get(i).isChecked())
            {
                selectedProductsIndexes.add(i);
            }
        }
        return selectedProductsIndexes;
    }

    public void removeCheckedProducts()
    {
        for(int i = getModelAsCollection().size()-1; i >= 0; i--)
        {
            if(getModelAsCollection().get(i).isChecked())
            {
                sortedList.remove(getModelAsCollection().get(i));
            }
        }
    }

    public List<Product> getCheckedProducts()
    {
        List<Product> productList = new ArrayList<>();
        for(int i = getModelAsCollection().size()-1; i >= 0; i--)
        {
            if(getModelAsCollection().get(i).isChecked())
            {
                productList.add(sortedList.get(i));
            }
        }
        return productList;
    }

    public List<Product> getModelAsCollection()
    {
        List<Product> productList = new ArrayList<>();
        for(int i = 0; i < sortedList.size(); i++)
        {
            productList.add(sortedList.get(i));
        }
        return productList;
    }

    public List<Product> getDeleteList()
    {
        return this.deleteList;
    }

    public void deleteAllProducts()
    {
        sortedList.clear();
    }

    // ------------------------------------------------------------------------------------------//

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        // The binding class is created automatically by the framework after XML binding
        // Since we don't know beforehand which binding was used (add, edit, or list),
        // we need to use the superclass
        private ViewDataBinding binding;

        // Received from the outside
        private ClickListener clickListener;

        ViewHolder(ViewDataBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        ViewHolder(ViewDataBinding binding, ClickListener clickListener)
        {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
        }

        public interface ClickListener
        {
            void onItemClicked(int position);
            boolean onItemLongClicked(int position);
        }

        @Override
        public void onClick(View clickedView) {
            if (clickListener != null)
            {
                clickListener.onItemClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View clickedView) {
            if (clickListener != null)
            {
                return clickListener.onItemLongClicked(getAdapterPosition());
            }
            return false;
        }

        // Set the product (casting needed) and return the binding type
        int bind(Product product)
        {
            int bindingType;
            if(binding instanceof CardProductLayoutBinding)
            {
                ((CardProductLayoutBinding) binding).setProduct(product);
                bindingType = ADD_MODE;
            }
            else if(binding instanceof CardProductLayoutEditBinding)
            {
                ((CardProductLayoutEditBinding) binding).setProduct(product);
                bindingType = EDIT_MODE;
            }
            else if(binding instanceof  CardProductLayoutShoppingListBinding)
            {
                ((CardProductLayoutShoppingListBinding) binding).setProduct(product);
                // Set listeners
                ((CardProductLayoutShoppingListBinding) binding).cardProduct.setOnClickListener(this);
                ((CardProductLayoutShoppingListBinding) binding).cardProduct.setOnLongClickListener(this);
                bindingType = LIST_MODE;
            }
            else if(binding instanceof  CardProductLayoutGroceryBinding)
            {
                ((CardProductLayoutGroceryBinding) binding).setProduct(product);
                bindingType = GROCERY_MODE;
            }
            else
            {
                ((CardProductLayoutManageBinding) binding).setProduct(product);
                bindingType = MANAGE_MODE;
            }
            binding.executePendingBindings();
            return bindingType;
        }
    }
}
