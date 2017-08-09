package com.mobile.paolo.listaspesa.model.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.Template;

import java.util.List;

/**
 * Created by paolo on 04/08/17.
 */

public class TemplateCardViewDataAdapter extends RecyclerView.Adapter<TemplateCardViewDataAdapter.ViewHolder>
{
    // The data to show
    private List<Template> templateList;

    public TemplateCardViewDataAdapter(List<Template> templateList)
    {
        this.templateList = templateList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View itemLayoutView = null;
        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_layout, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position)
    {
        // Set name
        viewHolder.cardTemplateName.setText(templateList.get(position).getName());

        // Snippet = first 3 products. The first one:
        String productsSnippet =  templateList.get(position).getProductList().get(0).getName();

        for(int i = 1; i < Math.min(3, templateList.get(position).getProductList().size()); i++)
        {
            productsSnippet += ", " + templateList.get(position).getProductList().get(i).getName();
        }
        productsSnippet += "...";
        viewHolder.cardProductsSnippet.setText(productsSnippet);
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView cardTemplateName;
        public TextView cardProductsSnippet;
        public ViewHolder(View itemLayoutView)
        {
            super(itemLayoutView);

            cardTemplateName = (TextView) itemLayoutView.findViewById(R.id.templateName);
            cardProductsSnippet = (TextView) itemLayoutView.findViewById(R.id.productsSnippet);
        }

    }
}
