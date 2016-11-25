package com.zxzx74147.mediacoredemo.base;

import android.app.Activity;
import android.content.Intent;

import com.zxzx74147.mediacore.components.util.FileSelectUtil;

/**
 * Created by zhengxin on 2016/11/24.
 */

public class BaseActivity extends Activity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode / FileSelectUtil.PICK_INTENT==1) {
            FileSelectUtil.dealOnActivityResult(requestCode, resultCode, data);
        }
    }
}
