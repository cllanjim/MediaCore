package com.zxzx74147.mediacoredemo.test;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.zxzx74147.mediacore.recorder.MediaRecorder;
import com.zxzx74147.mediacoredemo.R;
import com.zxzx74147.mediacoredemo.base.BaseActivity;
import com.zxzx74147.mediacoredemo.databinding.ActivityRecordTestBinding;

public class RecordTestActivity extends BaseActivity {
    private MediaRecorder mMediaRecorder = null;
    ActivityRecordTestBinding mBinging = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinging = DataBindingUtil.setContentView(this,R.layout.activity_record_test);
        mBinging.setHandler(this);
        mMediaRecorder = new MediaRecorder("record" + System.currentTimeMillis() + ".mp4");
        mMediaRecorder.setupSurfaceView(mBinging.preview);
        mMediaRecorder.start();
        mBinging.record.setOnTouchListener(mOnTouchListener);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mMediaRecorder.resume();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mMediaRecorder.pause();
                    break;
            }
            return true;
        }
    };

    public void onDone(View v){
        mMediaRecorder.stop();
    }
}
