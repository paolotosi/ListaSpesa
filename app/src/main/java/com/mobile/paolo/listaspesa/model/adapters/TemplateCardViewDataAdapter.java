package com.mobile.paolo.listaspesa.model.adapters;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.ShoppingList;
import com.mobile.paolo.listaspesa.model.objects.Template;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.HomeFragmentContainer;
import com.mobile.paolo.listaspesa.view.home.template.EditTemplateActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by paolo on 04/08/17.
 */

public class TemplateCardViewDataAdapter extends SelectableAdapter<TemplateCardViewDataAdapter.ViewHolder>
{
    // The data to show
    private List<Template> templateList;
    private boolean editVisibility;

    private ViewHolder.ClickListener clickListener;


    public TemplateCardViewDataAdapter(List<Template> templateList, ViewHolder.ClickListener clickListener)
    {
        this.templateList = templateList;
        this.editVisibility = true;
        this.clickListener = clickListener;
    }

    public TemplateCardViewDataAdapter(List<Template> templateList, boolean editVisibility)
    {
        this.templateList = templateList;
        this.editVisibility = editVisibility;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View itemLayoutView = null;
        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_layout, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView, this.editVisibility, this.clickListener);

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
        if(templateList.get(position).getProductList().size() > 3)
        {
            productsSnippet += "...";
        }
        viewHolder.cardProductsSnippet.setText(productsSnippet);

        // Set full list
        viewHolder.cardTemplateDetails.setText(templateList.get(position).getProductList().get(0).getName());
        for(int i = 1; i < templateList.get(position).getProductList().size(); i++)
        {
            viewHolder.cardTemplateDetails.append("\n" + templateList.get(position).getProductList().get(i).getName());
        }

        // Set the template
        viewHolder.selectedTemplate = templateList.get(position);

        // If the card is selected show a colored transparent overlay
        viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
        viewHolder.cardEditTemplateButton.setEnabled(!isSelected(position));
        viewHolder.cardUseTemplateButton.setEnabled(!isSelected(position));
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    public List<Template> getTemplateList()
    {
        return this.templateList;
    }

    public void removeItem(int position) {
        templateList.remove(position);
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
            templateList.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public void replaceAll(List<Template> models) {
        for (int i = templateList.size() - 1; i >= 0; i--) {
            final Template model = templateList.get(i);
            if (!models.contains(model)) {
                templateList.remove(model);
            }
        }
        templateList.addAll(models);
    }

    // ------------------------------------------------------------------------------------------//

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        // Card widgets
        private CardView cardTemplate;
        private TextView cardTemplateName;
        private TextView cardProductsSnippet;
        private Button cardUseTemplateButton;
        private Button cardEditTemplateButton;
        private ImageView cardExpandTemplateDetails;
        private TextView cardTemplateDetails;
        private View selectedOverlay;

        private boolean editVisibility;

        private ClickListener clickListener;

        // Template (it will be used in the EditTemplateActivity)
        private Template selectedTemplate;

        ViewHolder(View itemLayoutView, boolean visibility)
        {
            super(itemLayoutView);
            this.editVisibility = visibility;
            initializeWidgets(itemLayoutView);
            setupWidgetsListeners();
        }

        ViewHolder(View itemLayoutView, boolean visibility, ClickListener clickListener)
        {
            super(itemLayoutView);

            this.editVisibility = visibility;
            this.clickListener = clickListener;

            initializeWidgets(itemLayoutView);

            setupWidgetsListeners();
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


        private void initializeWidgets(View itemLayoutView)
        {
            cardTemplate = (CardView) itemLayoutView.findViewById(R.id.cardTemplate);
            cardTemplateName = (TextView) itemLayoutView.findViewById(R.id.templateName);
            cardProductsSnippet = (TextView) itemLayoutView.findViewById(R.id.productsSnippet);
            cardUseTemplateButton = (Button) itemLayoutView.findViewById(R.id.useTemplateButton);
            cardEditTemplateButton = (Button) itemLayoutView.findViewById(R.id.editTemplateButton);
            selectedOverlay = itemLayoutView.findViewById(R.id.selectedOverlay);

            if(!editVisibility)
            {
                cardEditTemplateButton.setVisibility(View.GONE);
            }
            cardExpandTemplateDetails = (ImageView) itemLayoutView.findViewById(R.id.expandTemplateDetails);
            cardTemplateDetails = (TextView) itemLayoutView.findViewById(R.id.templateDetails);
        }

        private void setupWidgetsListeners()
        {
            // Make the card listen to (long) click events, that will be handled by the listener
            // Note: the listener is ManageTemplateFragment, that is received from outside
            cardTemplate.setOnClickListener(this);
            cardTemplate.setOnLongClickListener(this);

            // Show/hide details
            cardExpandTemplateDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cardTemplateDetails.getVisibility() == View.GONE)
                    {
                        // Show details
                        TransitionManager.beginDelayedTransition(cardTemplate);
                        cardTemplateDetails.setVisibility(View.VISIBLE);
                        cardExpandTemplateDetails.setImageDrawable(v.getContext().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp));

                    }
                    else
                    {
                        // Hide details
                        cardTemplateDetails.setVisibility(View.GONE);
                        cardExpandTemplateDetails.setImageDrawable(v.getContext().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp));
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

            // Use template to create list
            cardUseTemplateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clickedView) {

                    // Create list from the selected template and save it in the cache
                    ShoppingList list = ShoppingList.fromJSON(selectedTemplate.toJSON());
                    GlobalValuesManager.getInstance(clickedView.getContext()).saveHasUserShoppingList(true);
                    GlobalValuesManager.getInstance(clickedView.getContext()).saveUserShoppingList(list.toJSON());

                    // Change fragment
                    FragmentTransaction transaction = ((FragmentActivity)clickedView.getContext()).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageShoppingListFragment());
                    transaction.commit();

                }
            });
        }
    }
}
