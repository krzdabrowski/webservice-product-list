package com.example.trubul.productlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProduct;

import java.util.List;

/**
 * Created by krzysiek on 6/2/18.
 */

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ProductViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private List<InventoryProduct> mInventoryProductList;
    private List<String> mTagsList;
    private Context mContext;

    RecyclerViewAdapter(Context context, List<InventoryProduct> inventoryProductList) {
        mContext = context;
        mInventoryProductList = inventoryProductList;
    }

    // 1) onCreateViewHolder - create layout object from XML and then ViewHolder; called by LayoutManager when it needs a new view
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false); // adding the inflated view to parent layout = false, is done later
        return new ProductViewHolder(view);
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
            String price = Float.toString((Float) inventoryProductItem.getProperty(10));
            String currentPrice = Float.toString((Float) inventoryProductItem.getProperty(11));

            holder.idTv.setText(idProduct);
            holder.modelTv.setText(model);
            holder.parametersTv.setText(parameters);
            holder.priceTv.setText(price);
            holder.currentPriceTv.setText(currentPrice);
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

    // Update data when new one is downloaded
    void loadNewData(List<InventoryProduct> newProducts, List<String> newTags) {
        mInventoryProductList = newProducts;
        mTagsList = newTags;
        notifyDataSetChanged();  // tell it to "registered observers" (like RecyclerView) = refresh display
    }

    // MOZE SIE PRZYDA DO LONG TAP
    public String getTag(int position) {
        if ((mTagsList != null) && (mTagsList.size() != 0)) {
            return mTagsList.get(position);
        } else {
            return null;
        }
    }

    // ViewHolder - no need to call findViewById() all the time
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView idTv;
        TextView modelTv;
        TextView parametersTv;
        TextView priceTv;
        TextView currentPriceTv;

        ProductViewHolder(View itemView) {
            super(itemView);
            this.idTv = itemView.findViewById(R.id.id_product);
            this.modelTv = itemView.findViewById(R.id.model);
            this.parametersTv = itemView.findViewById(R.id.parameters);
            this.priceTv = itemView.findViewById(R.id.price);
            this.currentPriceTv = itemView.findViewById(R.id.current_price);
        }
    }

}
