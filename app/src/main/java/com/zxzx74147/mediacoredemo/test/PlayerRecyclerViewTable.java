package com.zxzx74147.mediacoredemo.test;


import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.view.View;

import com.chad.library.adapter.base.BaseViewHolder;
import com.zxzx74147.mediacore.components.video.filter.data.FilterData;
import com.zxzx74147.mediacoredemo.R;
import com.zxzx74147.mediacoredemo.data.BaseItemData;
import com.zxzx74147.mediacoredemo.databinding.LayoutFilterThumbBinding;
import com.zxzx74147.mediacoredemo.widget.recyclerview.CommonRecyclerViewTable;

/**
 * Created by zhengxin on 2016/11/1.
 */

public class PlayerRecyclerViewTable implements CommonRecyclerViewTable {
    private int[] mTable = new int[]{
            R.layout.layout_filter_thumb
    };

    private View.OnClickListener mListener = null;
    @Override
    public int[] getLayoutId() {
        return mTable;
    }

    @Override
    public void convert(BaseViewHolder baseViewHolder, BaseItemData baseItemData) {

        switch (baseItemData.getItemType()) {
            case R.layout.layout_filter_thumb:
                LayoutFilterThumbBinding itemFilterBinding = DataBindingUtil.bind(baseViewHolder.convertView);
                itemFilterBinding.thumb.setImageResource(((FilterData) baseItemData.data).mResourceId);
                FilterData data = (FilterData) baseItemData.data;
                itemFilterBinding.setItem(data);
                itemFilterBinding.getRoot().setTag(R.id.tag_holder,itemFilterBinding);
                itemFilterBinding.getRoot().setTag(R.id.tag_data,baseItemData.data);
                itemFilterBinding.getRoot().setOnClickListener(mListener);
                if(data.isSelected) {
                    itemFilterBinding.thumb.setBorderColor(Color.GREEN);
                }else{
                    itemFilterBinding.thumb.setBorderColor(0);
                }
                break;
        }
    }

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }
}