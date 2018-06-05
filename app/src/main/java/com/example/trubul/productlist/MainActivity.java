package com.example.trubul.productlist;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.IWsdl2CodeEvents;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProduct;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProductList;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.Service1;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        RecyclerViewAdapter.ProductViewHolder.ClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";
    private Service1 mGetService;
    static List<InventoryProduct> mInventoryProductArrayList;
    static RecyclerViewAdapter mRecyclerViewAdapter;
//    static List<String> mTagsArrayList;
    static SparseArray<String> mTagsSparseArray;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

//    private SwipeRefreshLayout mySwipeRefreshLayout;

    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mySwipeRefreshLayout;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.header) RecyclerViewHeader header;
    @BindView(R.id.clear_button) Button clearBtn;
    @BindView(R.id.save_button) Button saveBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        mySwipeRefreshLayout = findViewById(R.id.swipe_refresh);
//        SwipeRefreshLayout mySwipeRefreshLayout = new SwipeRefreshLayout(this);
        mySwipeRefreshLayout.setOnRefreshListener(this);
        // Init ArrayLists<>
        mInventoryProductArrayList = new ArrayList<>();
//        mTagsArrayList = new ArrayList<>();
        mTagsSparseArray = new SparseArray<>();

        // Init RecyclerView, Header and Divider
//        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

//        RecyclerViewHeader header = findViewById(R.id.header);
        header.attachTo(recyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Init Adapter
        mRecyclerViewAdapter = new RecyclerViewAdapter(mInventoryProductArrayList, this);
        recyclerView.setAdapter(mRecyclerViewAdapter);

        // Init IWsdl2CodeEvents and Service1
        final CodeEventsHandler handler = new CodeEventsHandler(this, this);
        IWsdl2CodeEvents getCodeEvents = handler.getGetProductEvents();
        final IWsdl2CodeEvents saveCodeEvents = handler.getSaveProductEvents();

        mGetService = new Service1(getCodeEvents, "http://79.133.199.244/RFIDWebService/service1.asmx?op=GetAllProductList", 180);
        final Service1 saveService = new Service1(saveCodeEvents, "http://79.133.199.244/RFIDWebService/service1.asmx?op=SaveProductSelling", 180);
        getProductsFromWebservice();

        // Init Buttons
//        Button clearBtn = findViewById(R.id.clear_button);
//        Button saveBtn = findViewById(R.id.save_button);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionMode != null) {
                    actionMode.finish();
                    actionModeCallback.onDestroyActionMode(actionMode);
                }
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InventoryProductList productList = mRecyclerViewAdapter.getInventoryProductList();

                if (productList != null) {
                    try {
                        saveService.SaveProductSellingAsync(productList);

                        if (actionMode != null) {
                            actionMode.finish();
                            actionModeCallback.onDestroyActionMode(actionMode);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void getProductsFromWebservice() {
        try {
            mGetService.GetAllProductListAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ItemClickListeners
    @Override
    public void onItemClicked(int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else if (mRecyclerViewAdapter.getTag(position) != null) {
            Toast.makeText(this, "Epc is: " + mRecyclerViewAdapter.getTag(position), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "There's no epc for this product", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = startActionMode(actionModeCallback);
        }
        toggleSelection(position);

        return true;
    }

    // Toggle selected items and deal with it in ActionMode on toolbar
    private void toggleSelection(int position) {
        String wordInflection;
        mRecyclerViewAdapter.toggleSelection(position);
        int count = mRecyclerViewAdapter.getSelectedItemsCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            if (count == 1) {
                wordInflection = "";
            } else if (count >= 2 && count <= 4) {
                wordInflection = "y";
            } else {
                wordInflection = "ów";
            }

            actionMode.setTitle("Wybrano: " + String.valueOf(count) + " produkt" + wordInflection);
            actionMode.invalidate();
        }
    }

    @Override
    public void onRefresh() {
        getProductsFromWebservice();
        mySwipeRefreshLayout.setRefreshing(false);
    }


    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mRecyclerViewAdapter.clearAllSelections();
            actionMode = null;
        }
    }
}
