package com.zxzx74147.mediacore.recorder;

import android.net.Uri;

/**
 * Created by zhengxin on 2016/11/22.
 */

public interface IRecorderListener {

    void onPreparedDone();

    void onError(int error,String errorStr);

    void onProgress(long duration);

    void onComplete(Uri uri);
}
