package com.mobile.paolo.listaspesa.model.adapters;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.databinding.CardProductLayoutBinding;
import com.mobile.paolo.listaspesa.model.objects.Product;

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

    public ProductCardViewDataAdapter()
    {
        setupProductComparator();
        setupSortedList();
        insertDummyProducts();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CardProductLayoutBinding productBinding = CardProductLayoutBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(productBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position)
    {
        // Automatic binding defined via XML
        Product product = sortedList.get(position);
        viewHolder.bind(product);

        // Custom logic for product description
        if(sortedList.get(position).getDescription() != null)
        {
        if(sortedList.get(position).getDescription().equals("null"))
        {
            viewHolder.binding.productDescription.setText(viewHolder.binding.productDescription.getContext().getString(R.string.no_description_message));
        }
        }

        // Since the RecyclerView reuses elements, we need a way to remember which products where checked
        viewHolder.binding.productCheckbox.setChecked(sortedList.get(position).isChecked());

        // Save the product in the tag field of the checkbox, it'll be used later.
        viewHolder.binding.productCheckbox.setTag(sortedList.get(position));

        // When a checkbox is clicked:
        viewHolder.binding.productCheckbox.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public int getItemCount() {
        return sortedList.size();
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

    public void add(List<Product> models) {
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

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        // The following class is created automatically by the framework after XML binding
        private final CardProductLayoutBinding binding;

        ViewHolder(CardProductLayoutBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product)
        {
            binding.setProduct(product);
            binding.executePendingBindings();
        }
    }
}
