package com.zxzx74147.mediacore.recorder;

import android.view.SurfaceView;

import com.zxzx74147.mediacore.components.audio.encoder.AudioEncoder;
import com.zxzx74147.mediacore.components.audio.source.AudioSourceFactory;
import com.zxzx74147.mediacore.components.audio.source.IAudioSource;
import com.zxzx74147.mediacore.components.muxer.Mp4Muxer;
import com.zxzx74147.mediacore.components.muxer.timestamp.TimeStampGenerator;
import com.zxzx74147.mediacore.components.util.FileUtil;
import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.source.IVideoSource;
import com.zxzx74147.mediacore.components.video.source.VideoSourceFactory;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class MediaRecorder {
    private IAudioSource mAudioSource;
    private IVideoSource mVideoSource;
    private AudioEncoder mAudioEncoder;
    private VideoEncoder mVideoEncoder;
    private Mp4Muxer mMp4Muxer;
    private IProcessListener mRecorderListener = null;


    public MediaRecorder(String outputFileName) {
        mAudioSource = AudioSourceFactory.createMicSource();
        mVideoSource = VideoSourceFactory.createCameraSource();


        mMp4Muxer = new Mp4Muxer();
        mMp4Muxer.setOutputFile(FileUtil.getFile(outputFileName));
        mMp4Muxer.init();

        mAudioEncoder = new AudioEncoder();
        mVideoEncoder = new VideoEncoder();

        mAudioEncoder.setMuxer(mMp4Muxer);
        mVideoEncoder.setMuxer(mMp4Muxer);
        mAudioSource.setAudioEncoder(mAudioEncoder);
        mVideoSource.setVideoEncoder(mVideoEncoder);
    }

    public void setRecorderListener(IProcessListener listener){
        mRecorderListener = listener;
        mAudioSource.setProcessListener(listener);
        mVideoSource.setProcessListener(listener);
        mMp4Muxer.setProcessListener(listener);
    }

    public void start() {
        TimeStampGenerator.sharedInstance().reset();
        try {
            mAudioSource.prepare();
            mAudioSource.start();
            mVideoSource.prepare();
            mVideoSource.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void pause() {
        mVideoSource.pause();
        mAudioSource.pause();
    }

    public void resume() {
        TimeStampGenerator.sharedInstance().start();
        mVideoSource.resume();
        mAudioSource.resume();
    }

    public void stop() {
        mVideoSource.stop();
        mAudioSource.stop();
    }


    public void setupSurfaceView(SurfaceView surfaceview) {
        mVideoSource.setRenderSurfaceView(surfaceview);
    }
}
