package com.zxzx74147.mediacoredemo;

import android.app.Application;

import com.zxzx74147.mediacore.MediaCore;

/**
 * Created by zhengxin on 2016/11/24.
 */

public class DemoApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        MediaCore.init(this);
    }
}
