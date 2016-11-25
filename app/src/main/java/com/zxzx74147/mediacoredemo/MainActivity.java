package com.zxzx74147.mediacoredemo;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.zxzx74147.mediacoredemo.base.BaseActivity;
import com.zxzx74147.mediacoredemo.databinding.ActivityMainBinding;
import com.zxzx74147.mediacoredemo.test.RecordTestActivity;
import com.zxzx74147.mediacoredemo.test.VideoEditorTestActivity;
import com.zxzx74147.mediacoredemo.test.VideoSourceTestActivity;
import com.zxzx74147.mediacoredemo.utils.PermissionHelper;
import com.zxzx74147.mediacoredemo.utils.ZXActivityJumpHelper;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding mBinding = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        mBinding.setHandler(this);
    }

    public void onVideoSource(View v){
        int result = PermissionHelper.checkPermission(this,new String[]{Manifest.permission.CAMERA
                ,Manifest.permission.RECORD_AUDIO});
        if(result==0) {
            ZXActivityJumpHelper.startActivity(this, VideoSourceTestActivity.class);
        }
    }

    public void onRecord(View v){
        int result = PermissionHelper.checkPermission(this,new String[]{Manifest.permission.CAMERA
                ,Manifest.permission.RECORD_AUDIO});
        if(result==0) {
            ZXActivityJumpHelper.startActivity(this, RecordTestActivity.class);
        }
    }

    public void onVideoEdit(View v){
        int result = PermissionHelper.checkPermission(this,new String[]{Manifest.permission.CAMERA
                ,Manifest.permission.RECORD_AUDIO});
        if(result==0) {
            ZXActivityJumpHelper.startActivity(this, VideoEditorTestActivity.class);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
