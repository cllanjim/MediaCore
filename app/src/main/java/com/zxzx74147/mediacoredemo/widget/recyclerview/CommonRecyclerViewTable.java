package com.zxzx74147.mediacoredemo.widget.recyclerview;


import com.chad.library.adapter.base.BaseViewHolder;
import com.zxzx74147.mediacoredemo.data.BaseItemData;

/**
 * Created by zhengxin on 16/8/21.
 */

public interface CommonRecyclerViewTable {
    int[] getLayoutId();

    void convert(BaseViewHolder baseViewHolder, BaseItemData baseItemData);
}
