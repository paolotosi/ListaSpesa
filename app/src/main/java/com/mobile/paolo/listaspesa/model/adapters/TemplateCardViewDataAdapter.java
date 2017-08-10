package com.mobile.paolo.listaspesa.model.adapters;

import android.content.Intent;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.view.home.template.EditTemplateActivity;

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

        // Set full list
        viewHolder.cardTemplateDetails.setText(templateList.get(position).getProductList().get(0).getName());
        for(int i = 1; i < templateList.get(position).getProductList().size(); i++)
        {
            viewHolder.cardTemplateDetails.append("\n" + templateList.get(position).getProductList().get(i).getName());
        }

        // Set the template
        viewHolder.selectedTemplate = templateList.get(position);
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        // Card widgets
        private TextView cardTemplateName;
        private TextView cardProductsSnippet;
        private Button cardUseTemplateButton;
        private Button cardEditTemplateButton;
        private ImageView cardExpandTemplateDetails;
        private TextView cardTemplateDetails;

        // Template (it will be used in the EditTemplateActivity)
        private Template selectedTemplate;

        ViewHolder(View itemLayoutView)
        {
            super(itemLayoutView);
            initializeWidgets(itemLayoutView);
            setupWidgetsListeners();


        }

        private void initializeWidgets(View itemLayoutView)
        {
            cardTemplateName = (TextView) itemLayoutView.findViewById(R.id.templateName);
            cardProductsSnippet = (TextView) itemLayoutView.findViewById(R.id.productsSnippet);
            cardUseTemplateButton = (Button) itemLayoutView.findViewById(R.id.useTemplateButton);
            cardEditTemplateButton = (Button) itemLayoutView.findViewById(R.id.editTemplateButton);
            cardExpandTemplateDetails = (ImageView) itemLayoutView.findViewById(R.id.expandTemplateDetails);
            cardTemplateDetails = (TextView) itemLayoutView.findViewById(R.id.templateDetails);
        }

        private void setupWidgetsListeners()
        {
            // Show/hide details
            cardExpandTemplateDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cardTemplateDetails.getVisibility() == View.GONE)
                    {
                        cardTemplateDetails.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        cardTemplateDetails.setVisibility(View.GONE);
                    }
                }
            });

            // Edit button

            cardEditTemplateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clickedView) {
                    Intent intent = new Intent(clickedView.getContext(), EditTemplateActivity.class);
                    intent.putExtra("TEMPLATE", selectedTemplate.toJSON().toString());
                    clickedView.getContext().startActivity(intent);
                }
            });
        }

    }
}
