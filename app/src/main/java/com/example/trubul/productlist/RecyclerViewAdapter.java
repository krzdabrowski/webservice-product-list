package com.example.trubul.productlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProduct;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProductList;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.VectorInventoryProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by krzysiek on 6/2/18.
 */

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ProductViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private List<InventoryProduct> mInventoryProductList;
    private List<String> mTagsList;

    private SparseBooleanArray mSelectedItems;
    private ProductViewHolder.ClickListener clickListener;


    RecyclerViewAdapter(List<InventoryProduct> inventoryProductList, ProductViewHolder.ClickListener clickListener) {
        mInventoryProductList = inventoryProductList;
        mSelectedItems = new SparseBooleanArray();

        this.clickListener = clickListener;
    }

    // 0) Update data when new one is downloaded
    void loadNewData(List<InventoryProduct> newProducts, List<String> newTags) {
        mInventoryProductList = newProducts;
        mTagsList = newTags;
        notifyDataSetChanged();  // tell it to "registered observers" (like RecyclerView) to refresh display
    }

    // 1) onCreateViewHolder - create layout object from XML and then ViewHolder; called by LayoutManager when it needs a new view
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ProductViewHolder(view, clickListener);
    }

    // 2) onBindViewHolder - fill single element with data; called by LayoutManager when it wants new data to be stored in a ViewHolder to display it
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        if ((mInventoryProductList == null) || (mInventoryProductList.size() == 0)) {
            holder.idTv.setText(R.string.not_available_yet);
            holder.modelTv.setText(R.string.not_available_yet);
            holder.parametersTv.setText(R.string.not_available_yet);
            holder.priceTv.setText(R.string.not_available_yet);
            holder.currentPriceTv.setText(R.string.not_available_yet);
        } else {
            InventoryProduct inventoryProductItem = mInventoryProductList.get(position);

            String idProduct = Integer.toString((Integer) inventoryProductItem.getProperty(0));
            String model = (String) inventoryProductItem.getProperty(7);
            String parameters = inventoryProductItem.getProperty(2) + " " + inventoryProductItem.getProperty(4) + " " + inventoryProductItem.getProperty(6);
            String price = String.format(Locale.GERMAN, "%.2f", (Float) inventoryProductItem.getProperty(10)) + "zł";
            String currentPrice = String.format(Locale.GERMAN, "%.2f", (Float) inventoryProductItem.getProperty(11)) + "zł";

            holder.idTv.setText(idProduct);
            holder.modelTv.setText(model);
            holder.parametersTv.setText(parameters);
            holder.priceTv.setText(price);
            holder.currentPriceTv.setText(currentPrice);
            holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    // 3) getItemCount - number of all elements
    @Override
    public int getItemCount() {
        if ((mInventoryProductList != null) && (mInventoryProductList.size() != 0)) {
            return mInventoryProductList.size();
        } else {
            return 1;  // 1 = placeholder
        }
    }


    String getTag(int position) {
        if ((mTagsList != null) && (mTagsList.size() != 0)) {
            return mTagsList.get(position);
        } else {
            return null;
        }
    }

    InventoryProductList getInventoryProductList() {
        InventoryProductList productList = new InventoryProductList();

        if (getSelectedItemsCount() > 0) {
            VectorInventoryProduct vectorInventoryProduct = new VectorInventoryProduct();
            List<Integer> selection = getSelectedItemsPosition();

            for (Integer i: selection) {
                vectorInventoryProduct.addElement(mInventoryProductList.get(i));
            }

            productList.setProperty(0, vectorInventoryProduct);
        }

        return productList;
    }

    // Helper methods to deal with item selection
    private boolean isSelected(int position) {  // Returns if selected item row is selected
        return getSelectedItemsPosition().contains(position);
    }

    void toggleSelection(int position) {  // Toggle the selection of item row at given position
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    int getSelectedItemsCount() {  // Returns number of selected items
        return mSelectedItems.size();
    }

    private List<Integer> getSelectedItemsPosition() {  // Returns positions of selected elements
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            items.add(mSelectedItems.keyAt(i));
        }

        return items;
    }

    void clearAllSelections() {  // Do its thing
        List<Integer> selection = getSelectedItemsPosition();
        mSelectedItems.clear();
        for (Integer i: selection) {
            notifyItemChanged(i);
        }
    }

    // ViewHolder - no need to call findViewById() all the time and manage OnClickListeners
    static class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView idTv;
        private TextView modelTv;
        private TextView parametersTv;
        private TextView priceTv;
        private TextView currentPriceTv;
        private View selectedOverlay;
        private ClickListener clickListener;

        public interface ClickListener {
            void onItemClicked(int position);
            boolean onItemLongClicked(int position);
        }

        ProductViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            idTv = itemView.findViewById(R.id.id_product);
            modelTv = itemView.findViewById(R.id.model);
            parametersTv = itemView.findViewById(R.id.parameters);
            priceTv = itemView.findViewById(R.id.price);
            currentPriceTv = itemView.findViewById(R.id.current_price);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);

            clickListener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (clickListener != null) {
                return clickListener.onItemLongClicked(getLayoutPosition());
            }
            return false;
        }
    }
}