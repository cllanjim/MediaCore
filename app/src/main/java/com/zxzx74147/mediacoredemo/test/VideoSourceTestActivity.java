package com.zxzx74147.mediacoredemo.test;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.zxzx74147.mediacore.components.video.source.VideoCameraSource;
import com.zxzx74147.mediacoredemo.R;
import com.zxzx74147.mediacoredemo.base.BaseActivity;
import com.zxzx74147.mediacoredemo.databinding.ActivityVideoSourceTestBinding;

public class VideoSourceTestActivity extends BaseActivity {

    private ActivityVideoSourceTestBinding mBinding = null;
    private VideoCameraSource mVideoSource = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_source_test);
        mVideoSource = new VideoCameraSource();
        mVideoSource.setRenderSurfaceView(mBinding.preview);
        mVideoSource.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoSource.stop();
    }
}
