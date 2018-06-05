package com.example.trubul.productlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by krzysiek
 * On 6/2/18.
 */

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ProductViewHolder> {
    @SuppressWarnings("unused")
    private static final String TAG = "RecyclerViewAdapter";
    private List<InventoryProduct> mInventoryProductList;
    private SparseArray<String> mTagsSparseArray;

    private SparseBooleanArray mSelectedItems;
    private ProductViewHolder.ClickListener mClickListener;
    private static boolean isEmptyRow = true;


    RecyclerViewAdapter(List<InventoryProduct> inventoryProductList, ProductViewHolder.ClickListener clickListener) {
        mInventoryProductList = inventoryProductList;
        mSelectedItems = new SparseBooleanArray();
        mClickListener = clickListener;
    }

    // 0) Update data when new one is downloaded
    void loadNewData(List<InventoryProduct> newProducts, SparseArray<String> newTags) {
        mInventoryProductList = newProducts;
        mTagsSparseArray = newTags;
        notifyDataSetChanged();  // tell it to registered observers (like RecyclerView) to refresh display
    }

    // 1) onCreateViewHolder - create layout object from XML and then ViewHolder; called by LayoutManager when it needs a new view
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ProductViewHolder(view, mClickListener);
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
            isEmptyRow = false;
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
        if ((mTagsSparseArray != null) && (mTagsSparseArray.size() != 0)) {
            return mTagsSparseArray.get(position);
        } else {
            return null;
        }
    }

    InventoryProductList getInventoryProductList() {
        InventoryProductList productList = null;

        if (getSelectedItemsCount() > 0) {
            productList = new InventoryProductList();
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

    // ViewHolder - no need to call findViewById() all the time and implement OnClickListeners
    static class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.id_product) TextView idTv;
        @BindView(R.id.model) TextView modelTv;
        @BindView(R.id.parameters) TextView parametersTv;
        @BindView(R.id.price) TextView priceTv;
        @BindView(R.id.current_price) TextView currentPriceTv;
        @BindView(R.id.selected_overlay) View selectedOverlay;

        private ClickListener clickListener;

        public interface ClickListener {
            void onItemClicked(int position);
            boolean onItemLongClicked(int position);
        }

        ProductViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            clickListener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null && !isEmptyRow) {
                clickListener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (clickListener != null && !isEmptyRow) {
                return clickListener.onItemLongClicked(getLayoutPosition());
            }
            return false;
        }
    }
}