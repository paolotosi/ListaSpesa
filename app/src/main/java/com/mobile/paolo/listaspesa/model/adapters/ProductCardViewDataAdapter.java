package com.mobile.paolo.listaspesa.model.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.Product;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.model.objects.User;

import java.util.List;

/**
 * Created by paolo on 04/08/17.
 */

public class ProductCardViewDataAdapter extends RecyclerView.Adapter<ProductCardViewDataAdapter.ViewHolder>
{
    // The data to show
    private List<Product> productList;

    public ProductCardViewDataAdapter(List<Product> productList)
    {
        this.productList = productList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View itemLayoutView;
        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_product_layout, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        viewHolder.cardProductName.setText(productList.get(position).getName());
        viewHolder.cardProductBrand.setText(productList.get(position).getBrand());
        if(productList.get(position).getDescription().equals("null"))
        {
            viewHolder.cardProductDescription.setText(viewHolder.cardProductDescription.getContext().getString(R.string.no_description_message));
        }
        else
        {
            viewHolder.cardProductDescription.setText(productList.get(position).getDescription());
        }

        viewHolder.cardCheckbox.setChecked(productList.get(position).isChecked());

        // Save the product in the tag field of the checkbox, it'll be used later.
        viewHolder.cardCheckbox.setTag(productList.get(position));

        // When a checkbox is clicked:
        viewHolder.cardCheckbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Retrieve the corresponding product.
                CheckBox checkbox = (CheckBox) v;
                Product product = (Product) checkbox.getTag();

                // Set the 'checked' field of the product both in the checkbox tag field and in the list
                product.setChecked(checkbox.isChecked());
                productList.get(position).setChecked(checkbox.isChecked());

            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView cardProductName;
        private TextView cardProductBrand;
        private TextView cardProductDescription;
        private CheckBox cardCheckbox;

        public ViewHolder(View itemLayoutView)
        {
            super(itemLayoutView);

            cardProductName = (TextView) itemLayoutView.findViewById(R.id.productName);
            cardProductBrand = (TextView) itemLayoutView.findViewById(R.id.productBrand);
            cardProductDescription = (TextView) itemLayoutView.findViewById(R.id.productDescription);
            cardCheckbox = (CheckBox) itemLayoutView.findViewById(R.id.productCheckbox);
        }

    }
}
