package com.mobile.paolo.listaspesa.model.adapters;

import android.content.Context;
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

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.Supermarket;
import com.mobile.paolo.listaspesa.view.home.HomeActivity;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by paolo on 27/08/17.
 */

/**
 * This file contains two classes: SuperMarketCardViewAdapter and ViewHolder
 * First class contains methods that specifies recycler view cards behaviour, binding and operations
 * Second class is the associated view holder
 */

public class SupermarketCardViewDataAdapter extends SelectableAdapter<SupermarketCardViewDataAdapter.ViewHolder>
{
    // The data to show
    private List<Supermarket> supermarketList;

    private SupermarketCardViewDataAdapter.ViewHolder.ClickListener clickListener;


    public SupermarketCardViewDataAdapter(List<Supermarket> supermarketList, SupermarketCardViewDataAdapter.ViewHolder.ClickListener clickListener)
    {
        this.supermarketList = supermarketList;
        this.clickListener = clickListener;
    }

    @Override
    public SupermarketCardViewDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View itemLayoutView = null;
        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_supermarket_layout, null);
        SupermarketCardViewDataAdapter.ViewHolder viewHolder = new SupermarketCardViewDataAdapter.ViewHolder(itemLayoutView, this.clickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SupermarketCardViewDataAdapter.ViewHolder viewHolder, int position)
    {
        // Set name
        viewHolder.cardSupermarketName.setText(supermarketList.get(position).getName());

        // Set address
        viewHolder.cardSupermarketAddress.setText(supermarketList.get(position).getAddress());

        // Set full list
        if(supermarketList.get(position).getProductList().size() > 0)
        {
            viewHolder.cardSupermarketProducts.setText(supermarketList.get(position).getProductList().get(0).toString());
            for(int i = 1; i < supermarketList.get(position).getProductList().size(); i++)
            {
                viewHolder.cardSupermarketProducts.append("\n" + supermarketList.get(position).getProductList().get(i).toString());
            }
        }
        else
        {
            viewHolder.cardExpandSupermarketProducts.setVisibility(View.GONE);
            viewHolder.cardShowProductsButton.setVisibility(View.GONE);
        }

        // Set the supermarket
        viewHolder.selectedSupermarket = supermarketList.get(position);

        // If the card is selected show a colored transparent overlay
        viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);

        // viewHolder.cardShowProductsButton.setEnabled(!isSelected(position));

    }

    @Override
    public int getItemCount() {
        return supermarketList.size();
    }

    public List<Supermarket> getSupermarketList()
    {
        return this.supermarketList;
    }

    public void removeItem(int position) {
        supermarketList.remove(position);
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
            supermarketList.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public void replaceAll(List<Supermarket> models) {
        for (int i = supermarketList.size() - 1; i >= 0; i--) {
            final Supermarket model = supermarketList.get(i);
            if (!models.contains(model)) {
                supermarketList.remove(model);
            }
        }
        supermarketList.addAll(models);
    }

    // ------------------------------------------------------------------------------------------//

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        // Card widgets
        private CardView cardSupermarket;
        private TextView cardSupermarketName;
        private TextView cardSupermarketAddress;
        private Button cardShowProductsButton;
        private ImageView cardExpandSupermarketProducts;
        private TextView cardSupermarketProducts;
        private View selectedOverlay;

        private SupermarketCardViewDataAdapter.ViewHolder.ClickListener clickListener;

        // Template (it will be used in the EditTemplateActivity)
        private Supermarket selectedSupermarket;

        ViewHolder(View itemLayoutView, SupermarketCardViewDataAdapter.ViewHolder.ClickListener clickListener)
        {
            super(itemLayoutView);

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
            cardSupermarket = (CardView) itemLayoutView.findViewById(R.id.cardSupermarket);
            cardSupermarketName = (TextView) itemLayoutView.findViewById(R.id.supermarketName);
            cardSupermarketAddress = (TextView) itemLayoutView.findViewById(R.id.supermarketAddress);
            cardShowProductsButton = (Button) itemLayoutView.findViewById(R.id.showProductsButton);
            selectedOverlay = itemLayoutView.findViewById(R.id.selectedOverlay);
            cardExpandSupermarketProducts = (ImageView) itemLayoutView.findViewById(R.id.expandSupermarketProducts);
            cardSupermarketProducts = (TextView) itemLayoutView.findViewById(R.id.supermarketProducts);
        }

        private void setupWidgetsListeners()
        {
            // Make the card listen to (long) click events, that will be handled by the listener
            // Note: the listener is ManageTemplateFragment, that is received from outside
            cardSupermarket.setOnClickListener(this);
            cardSupermarket.setOnLongClickListener(this);

            // Show/hide details
            cardExpandSupermarketProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandDetails(v);
                }
            });

            cardShowProductsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clickedView) {
                    expandDetails(clickedView);
                }
            });
        }

        private void expandDetails(View v)
        {
            if(cardSupermarketProducts.getVisibility() == View.GONE)
            {
                // Show details
                TransitionManager.beginDelayedTransition(cardSupermarket);
                cardSupermarketProducts.setVisibility(View.VISIBLE);
                cardExpandSupermarketProducts.setImageDrawable(v.getContext().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp));

            }
            else
            {
                // Hide details
                cardSupermarketProducts.setVisibility(View.GONE);
                cardExpandSupermarketProducts.setImageDrawable(v.getContext().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp));
            }
        }



        private void changeFragment(Context context)
        {
            // Change selected tab in bottom navigation view
            BottomNavigationViewEx homeBottomNavigationView = (BottomNavigationViewEx) ((HomeActivity) context).findViewById(R.id.home_bottom_navigation);
            homeBottomNavigationView.getMenu().getItem(2).setChecked(true);

            // Change fragment: show ManageShoppingListFragment
            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageShoppingListFragment());
            transaction.commit();
        }
    }
}
