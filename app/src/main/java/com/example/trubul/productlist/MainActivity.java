package com.example.trubul.productlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.IWsdl2CodeEvents;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProduct;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProductList;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.Service1;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.TagList;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.Tags;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.VectorInventoryProduct;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.VectorTags;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GestureListener.OnRecyclerClickListener {

    private static final String TAG = "MainActivity";
    private List<InventoryProduct> mInventoryProductArrayList;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private List<String> mTagsArrayList;

    enum Codes {STARTED, FINISHED, FINISHED_WITH_EX, ENDED_REQUEST}
    Codes mDownloadCode = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInventoryProductArrayList = new ArrayList<>();
        mTagsArrayList = new ArrayList<>();

        // Init RecyclerView, Header and Divider
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewHeader header = findViewById(R.id.header);
        header.attachTo(recyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Init ItemTouchListener and Adapter
        recyclerView.addOnItemTouchListener(new GestureListener(this, recyclerView, this));
        mRecyclerViewAdapter = new RecyclerViewAdapter(this, mInventoryProductArrayList);
        recyclerView.setAdapter(mRecyclerViewAdapter);


        Service1 getService = new Service1(getProductEvents, "http://79.133.199.244/RFIDWebService/service1.asmx?op=GetAllProductList", 180);
        Service1 saveService = new Service1(saveProductEvents, "http://79.133.199.244/RFIDWebService/service1.asmx?op=SaveProductSelling", 180);

        try {
            getService.GetAllProductListAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    IWsdl2CodeEvents getProductEvents = new IWsdl2CodeEvents() {
        @Override
        public void Wsdl2CodeStartedRequest() {
            mDownloadCode = Codes.STARTED;
            Log.i(TAG, "Wsdl2CodeStartedRequest is in onPreExecute() with code: " + mDownloadCode);
        }

        @Override
        public void Wsdl2CodeFinished(String methodName, Object Data) {
            mDownloadCode = Codes.FINISHED;
            Log.i(TAG, "Wsdl2CodeStartedRequest is in onPostExecute() with not-null data and code: " + mDownloadCode);

            InventoryProductList rawData = (InventoryProductList) Data;
            VectorInventoryProduct inventoryProductList = (VectorInventoryProduct) rawData.getProperty(0);  // Vector<InventoryProduct>
            int productListLength = inventoryProductList.getPropertyCount();

            for (int i = 0; i < productListLength; i++) {
                InventoryProduct singleProduct = (InventoryProduct) inventoryProductList.getProperty(i);
                mInventoryProductArrayList.add(singleProduct);

                // Get listTags
                TagList tagList = (TagList) singleProduct.getProperty(17);
                VectorTags vectorTags = (VectorTags) tagList.getProperty(0);  // Vector<Tags>
                int vectorTagLength = vectorTags.getPropertyCount();


                if (vectorTagLength != 0) {
                    for (int j = 0; j < vectorTagLength; j++) {
                        Tags singleTag = (Tags) vectorTags.getProperty(j);
                        String epc = (String) singleTag.getProperty(1);
                        if (epc != null) {
                            mTagsArrayList.add(epc);
                        }
                    }
                }
            }

            Log.d(TAG, "Wsdl2CodeFinished: mInventoryProductArrayList length is: " + mInventoryProductArrayList.size());
            Log.d(TAG, "Wsdl2CodeFinished: mTagsArrayList length is: " + mTagsArrayList.size());

            mRecyclerViewAdapter.loadNewData(mInventoryProductArrayList, mTagsArrayList);
        }

        @Override
        public void Wsdl2CodeFinishedWithException(Exception ex) {
            mDownloadCode = Codes.FINISHED_WITH_EX;
            Log.i(TAG, "Wsdl2CodeStartedRequest got exception with code: " + mDownloadCode + " -> " + ex);
        }

        @Override
        public void Wsdl2CodeEndedRequest() {
            mDownloadCode = Codes.ENDED_REQUEST;
            Log.i(TAG, "Wsdl2CodeStartedRequest is in onPostExecute() with code: " + mDownloadCode);
        }
    };

    IWsdl2CodeEvents saveProductEvents = new IWsdl2CodeEvents() {
        @Override
        public void Wsdl2CodeStartedRequest() {

        }

        @Override
        public void Wsdl2CodeFinished(String methodName, Object Data) {

        }

        @Override
        public void Wsdl2CodeFinishedWithException(Exception ex) {

        }

        @Override
        public void Wsdl2CodeEndedRequest() {

        }
    };

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(MainActivity.this, "Epc is: " + mRecyclerViewAdapter.getTag(position), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(MainActivity.this, "Long tap at position " + position, Toast.LENGTH_SHORT).show();
    }
}
