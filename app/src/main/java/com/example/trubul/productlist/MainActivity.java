package com.example.trubul.productlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.VectorInventoryProduct;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GestureListener.OnRecyclerClickListener {

    private static final String TAG = "MainActivity";
    Codes mDownloadCode = null;
    private List<InventoryProduct> mInventoryProductArrayList;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    IWsdl2CodeEvents events = new IWsdl2CodeEvents() {
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
            }
            Log.d(TAG, "Wsdl2CodeFinished: length is: " + mInventoryProductArrayList.size());

            mRecyclerViewAdapter.loadNewData(mInventoryProductArrayList);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInventoryProductArrayList = new ArrayList<>();


        // Init RecyclerView and Header
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewHeader header = findViewById(R.id.header);
        header.attachTo(recyclerView);


        recyclerView.addOnItemTouchListener(new GestureListener(this, recyclerView, this));
        mRecyclerViewAdapter = new RecyclerViewAdapter(this, mInventoryProductArrayList);
        recyclerView.setAdapter(mRecyclerViewAdapter);


        Service1 service = new Service1(events, "http://79.133.199.244/RFIDWebService/service1.asmx?op=GetAllProductList", 180);
        try {
            service.GetAllProductListAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: starts");
        Toast.makeText(MainActivity.this, "Normal tap at position " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Log.d(TAG, "onItemLongClick: starts");
        Toast.makeText(MainActivity.this, "Long tap at position " + position, Toast.LENGTH_SHORT).show();
    }

    enum Codes {STARTED, FINISHED, FINISHED_WITH_EX, ENDED_REQUEST}

}
