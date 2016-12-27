package com.zxzx74147.mediacoredemo.base;

import android.app.Activity;
import android.content.Intent;

import com.zxzx74147.mediacore.components.util.FileSelectUtil;
import com.zxzx74147.mediacoredemo.data.IntentData;

import static com.zxzx74147.mediacoredemo.utils.ZXActivityJumpHelper.INTENT_DATA;

/**
 * Created by zhengxin on 2016/11/24.
 */

public class BaseActivity extends Activity {

    protected String TAG = this.getClass().getName();
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode / FileSelectUtil.PICK_INTENT==1) {
            FileSelectUtil.dealOnActivityResult(requestCode, resultCode, data);
        }
    }

    protected IntentData getIntentData(){
        IntentData intentData = (IntentData) getIntent().getSerializableExtra(INTENT_DATA);
        if(intentData==null){
            return intentData;
        }

        intentData.uri = getIntent().getData();
        return intentData;
    }
}
