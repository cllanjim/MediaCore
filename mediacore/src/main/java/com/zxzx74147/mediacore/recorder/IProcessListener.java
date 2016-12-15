package com.zxzx74147.mediacore.recorder;

import android.net.Uri;

/**
 * Created by zhengxin on 2016/11/22.
 */

public interface IProcessListener {

    void onPreparedDone(int max);

    void onError(int error,String errorStr);

    void onProgress(int progress);

    void onComplete(Uri uri);
}
