package com.zxzx74147.mediacoredemo.test;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zxzx74147.mediacore.recorder.IProcessListener;
import com.zxzx74147.mediacore.recorder.MediaRecorder;
import com.zxzx74147.mediacoredemo.R;
import com.zxzx74147.mediacoredemo.base.BaseActivity;
import com.zxzx74147.mediacoredemo.data.IntentData;
import com.zxzx74147.mediacoredemo.databinding.ActivityRecordTestBinding;
import com.zxzx74147.mediacoredemo.utils.ZXActivityJumpHelper;

public class RecordTestActivity extends BaseActivity {

    private static final int MAX_LEN = 1000*30;
    private MediaRecorder mMediaRecorder = null;
    private ActivityRecordTestBinding mBinging = null;
    private boolean mIsRecroding = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinging = DataBindingUtil.setContentView(this, R.layout.activity_record_test);
        mBinging.setHandler(this);
        mMediaRecorder = new MediaRecorder("circle_red" + System.currentTimeMillis() + ".mp4");
        mMediaRecorder.setRecorderListener(mRecordListener);
        mMediaRecorder.setupSurfaceView(mBinging.preview);
        mMediaRecorder.start();
        mBinging.progressBar.setMax(MAX_LEN);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onRecord(View v) {
        if(mIsRecroding){
            mBinging.record.setImageResource(R.drawable.circle_red);
            mMediaRecorder.pause();
            mIsRecroding = false;
        }else{
            mBinging.record.setImageResource(R.drawable.circle_yellow);
            mMediaRecorder.resume();
            mIsRecroding = true;
        }
    }

    public void onDone(View v) {
        mMediaRecorder.stop();
    }

    private IProcessListener mRecordListener = new IProcessListener() {
        @Override
        public void onPreparedDone(int max) {
        }

        @Override
        public void onError(int error, String errorStr) {

        }

        @Override
        public void onProgress(int progress) {
            mBinging.progressBar.setProgress(progress);
            if(progress>=MAX_LEN){
                mMediaRecorder.stop();
            }
        }

        @Override
        public void onComplete(Uri uri) {
            Toast.makeText(RecordTestActivity.this,uri.toString(),Toast.LENGTH_LONG).show();
            IntentData intentData = new IntentData();
            intentData.uri = uri;
            ZXActivityJumpHelper.startActivity(RecordTestActivity.this,VideoEditorTestActivity.class,intentData);
        }
    };
}