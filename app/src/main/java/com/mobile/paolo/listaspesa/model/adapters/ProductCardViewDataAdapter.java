package com.mobile.paolo.listaspesa.model.adapters;

import android.databinding.ViewDataBinding;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutBinding;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutEditBinding;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutShoppingListBinding;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created by paolo on 04/08/17.
 */

public class ProductCardViewDataAdapter extends RecyclerView.Adapter<ProductCardViewDataAdapter.ViewHolder>
{
    // The data to show
    private SortedList<Product> sortedList;
    private static Comparator<Product> alphabeticalComparator;

    // Used in edit mode to store the products to delete
    private List<Product> deleteList = new ArrayList<>();

    // Mode: add or edit
    private int mode;
    public static final int ADD_MODE = 1;
    public static final int EDIT_MODE = 2;
    public static final int LIST_MODE = 3;

    public ProductCardViewDataAdapter(int mode)
    {
        this.mode = mode;
        setupProductComparator();
        setupSortedList();
        insertDummyProducts();
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
            case LIST_MODE: productBinding = CardProductLayoutShoppingListBinding.inflate(layoutInflater, parent, false); break;
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

    // Needed to make the list fill the height
    private void insertDummyProducts()
    {
        sortedList.add(new Product("Dummy1", "a", "a"));
        sortedList.add(new Product("Dummy2", "b", "b"));
        sortedList.add(new Product("Dummy3", "c", "c"));
        sortedList.add(new Product("Dummy4", "d", "d"));
        sortedList.add(new Product("Dummy5", "e", "e"));
    }

    public SortedList<Product> getModel()
    {
        return sortedList;
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

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        // The binding class is created automatically by the framework after XML binding
        // Since we don't know beforehand which binding was used (add, edit, or list),
        // we need to use the superclass
        private ViewDataBinding binding;

        ViewHolder(ViewDataBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
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
            else
            {
                ((CardProductLayoutShoppingListBinding) binding).setProduct(product);
                bindingType = LIST_MODE;
            }
            binding.executePendingBindings();
            return bindingType;
        }
    }
}
