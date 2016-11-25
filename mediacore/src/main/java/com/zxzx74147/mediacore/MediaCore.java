package com.zxzx74147.mediacore;


import android.content.Context;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class MediaCore {
    private MediaCore(){

    }

    private static Context mContext = null;

    public static void init(Context context){
        mContext = context;
    }

    public static  Context getContext(){
        return mContext;
    }



}
