package com.zxzx74147.mediacore.recorder;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by zhengxin on 2016/11/22.
 */

public abstract class IProcessListener {

    private volatile int mProgress;
    private Handler mHandler = null;
    public IProcessListener(){
        mHandler = new Handler(Looper.getMainLooper());
    }


    public  void preparedDone(final int max){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPreparedDone(max);
            }
        });
    }

    public  void error(final int error,final String errorStr){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onError(error, errorStr);
            }
        });
    }

    public  void progress(final int progress){
        mProgress = progress;
        mHandler.removeCallbacks(mProcessRunnable);
        mHandler.post(mProcessRunnable);
    }

    private Runnable mProcessRunnable = new Runnable() {
        @Override
        public void run() {
            onProgress(mProgress);
        }
    };

    public  void complete(final Uri uri){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onComplete(uri);
            }
        });
    }

    public abstract void onPreparedDone(int max);

    public abstract void onError(int error,String errorStr);

    public abstract void onProgress(int progress);

    public abstract void onComplete(Uri uri);
}
