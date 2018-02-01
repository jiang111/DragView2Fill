package com.jyuesong.dragview2fill;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jyuesong.dragview2fill.dragview.DragViewGroup;
import com.jyuesong.dragview2fill.dragview.RecyclerViewHelper;

public class MainActivity extends AppCompatActivity implements RecyclerViewHelper.SizeChangedCallBack {

    DragViewGroup mDragViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDragViewGroup = findViewById(R.id.fast_new_lianxi_view);
        mDragViewGroup.registerSizeChangeCallBack(this);
    }

    @Override
    public void size(int count) {

    }


}
