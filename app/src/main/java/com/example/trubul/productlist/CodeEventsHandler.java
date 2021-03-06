package com.example.trubul.productlist;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.IWsdl2CodeEvents;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProduct;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.InventoryProductList;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.TagList;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.Tags;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.VectorInventoryProduct;
import com.example.trubul.productlist.AndroidKsoap.com.Wsdl2Code.WebServices.Service1.VectorTags;

/**
 * Created by krzysiek
 * On 6/2/18.
 */

class CodeEventsHandler {

    private static final String TAG = "CodeEventsHandler";
    enum Codes {STARTED, FINISHED, FINISHED_WITH_EX, ENDED_REQUEST}
    private Codes mDownloadCode = null;
    private final Activity mActivity;

    private IWsdl2CodeEvents getProductEvents;
    private IWsdl2CodeEvents saveProductEvents;

    IWsdl2CodeEvents getGetProductEvents() {
        return getProductEvents;
    }
    IWsdl2CodeEvents getSaveProductEvents() {
        return saveProductEvents;
    }


    CodeEventsHandler(Context ctx, Activity activity) {
        final Context context = ctx;
        mActivity = activity;

        getProductEvents = new IWsdl2CodeEvents() {
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
                    MainActivity.mInventoryProductArrayList.add(singleProduct);

                    // Get listTags
                    TagList tagList = (TagList) singleProduct.getProperty(17);
                    VectorTags vectorTags = (VectorTags) tagList.getProperty(0);  // Vector<Tags>
                    int vectorTagLength = vectorTags.getPropertyCount();
                    Log.d(TAG, "Wsdl2CodeFinished: vectorTagLength is: " + vectorTagLength);

                    if (vectorTagLength != 0) {
                        Tags singleTag = (Tags) vectorTags.getProperty(0);
                        String epc = (String) singleTag.getProperty(1);
                        if (epc != null) {
                            MainActivity.mTagsSparseArray.append(i, epc);  // SparseArray<String>
                        }
                    }
                }

                Log.d(TAG, "Wsdl2CodeFinished: mInventoryProductArrayList length is: " + MainActivity.mInventoryProductArrayList.size());
                Log.d(TAG, "Wsdl2CodeFinished: mTagsArrayList length is: " + MainActivity.mTagsSparseArray.size());

                MainActivity.mRecyclerViewAdapter.loadNewData(MainActivity.mInventoryProductArrayList, MainActivity.mTagsSparseArray);
            }

            @Override
            public void Wsdl2CodeFinishedWithException(final Exception ex) {
                mDownloadCode = Codes.FINISHED_WITH_EX;
//                Log.i(TAG, "Wsdl2CodeStartedRequest got exception with code: " + mDownloadCode + " -> " + ex);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Error: " + ex, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void Wsdl2CodeEndedRequest() {
                mDownloadCode = Codes.ENDED_REQUEST;
                Log.i(TAG, "Wsdl2CodeStartedRequest is in onPostExecute() with code: " + mDownloadCode);
            }
        };


        saveProductEvents = new IWsdl2CodeEvents() {
            @Override
            public void Wsdl2CodeStartedRequest() {
                mDownloadCode = Codes.STARTED;
                Log.i(TAG, "Wsdl2CodeStartedRequest is in onPreExecute() with code: " + mDownloadCode);
            }

            @Override
            public void Wsdl2CodeFinished(String methodName, Object Data) {
                mDownloadCode = Codes.FINISHED;
                Log.i(TAG, "Wsdl2CodeStartedRequest is in onPostExecute() with not-null data and code: " + mDownloadCode);

                final String result = (String) Data;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void Wsdl2CodeFinishedWithException(final Exception ex) {
                mDownloadCode = Codes.FINISHED_WITH_EX;
//                Log.i(TAG, "Wsdl2CodeStartedRequest got exception with code: " + mDownloadCode + " -> " + ex);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Error: " + ex, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void Wsdl2CodeEndedRequest() {
                mDownloadCode = Codes.ENDED_REQUEST;
                Log.i(TAG, "Wsdl2CodeStartedRequest is in onPostExecute() with code: " + mDownloadCode);
            }
        };
    }
}
