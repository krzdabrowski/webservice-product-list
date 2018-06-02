package com.example.trubul.productlist;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by krzysiek on 6/2/18.
 */

class GestureListener extends RecyclerView.SimpleOnItemTouchListener {
    private final OnRecyclerClickListener mListener;
    private final GestureDetectorCompat mGestureDetector;

    GestureListener(Context context, final RecyclerView recyclerView, OnRecyclerClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());  // check and return which View was under coordinates(X,Y)

                if (childView != null && mListener != null) {
                    mListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
                }

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (childView != null && mListener != null) {
                    mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }
        });

    }

    // Intercept every touch event that could be possible in RecyclerView
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        if (mGestureDetector != null) {
            return mGestureDetector.onTouchEvent(e);
        } else {
            return false;
        }
    }

    interface OnRecyclerClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
}
