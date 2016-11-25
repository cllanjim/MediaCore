package com.zxzx74147.mediacore.components.video.source;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.source.camera.CameraHandler;
import com.zxzx74147.mediacore.components.video.source.camera.CameraThread;
import com.zxzx74147.mediacore.components.video.source.camera.MainHandler;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class VideoCameraSource implements IVideoSource {
    private static final String TAG = VideoCameraSource.class.getName();


    private SurfaceView mSurfaceView = null;
    private CameraThread mCameraThread = null;
    private VideoEncoder mVideoEncoder = null;

    @Override
    public void prepare() {

    }

    @Override
    public void start() {
        mCameraThread = new CameraThread(mMainHandler);
        mCameraThread.start();
        mCameraThread.waitUntilReady();
        setVideoEncoder(mVideoEncoder);
    }

    @Override
    public void stop() {
        if (mCameraThread != null) {
            CameraHandler rh = mCameraThread.getHandler();
            rh.sendShutdown();
        }
        if(mVideoEncoder!=null){
            mVideoEncoder.drainVideoRawData(true);
        }
    }

    public void setRenderSurfaceView(SurfaceView surface) {
        if (mSurfaceView != null) {
            mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
        }
        mSurfaceView = surface;
        if (mSurfaceView == null) {
            return;
        }
        mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
    }

    @Override
    public void pause() {
        if (mCameraThread != null) {
            CameraHandler rh = mCameraThread.getHandler();
            rh.sendPauseRecord();
        }
    }

    @Override
    public void resume() {
        if (mCameraThread != null) {
            CameraHandler rh = mCameraThread.getHandler();
            rh.sendResumeRecord();
        }
    }

    @Override
    public void setVideoEncoder(VideoEncoder encoder) {
        mVideoEncoder = encoder;
        if (mVideoEncoder == null) {
            return;
        }
        if (mCameraThread == null) {
            return;
        }
        final CameraHandler rh = mCameraThread.getHandler();
        rh.post(new Runnable() {
            @Override
            public void run() {
                mVideoEncoder.prepare();
                mVideoEncoder.start();
                rh.sendEncoderAvailable(mVideoEncoder.getEncoderSurface());
                rh.sendEncoderSurfaceChanged(mVideoEncoder.getConfig().width,
                        mVideoEncoder.getConfig().height);
            }
        });
    }



    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            CameraHandler handler = mCameraThread.getHandler();
            if (handler != null) {
                handler.sendSurfaceAvailable(holder.getSurface());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            CameraHandler handler = mCameraThread.getHandler();
            if (handler != null) {
                handler.sendSurfaceChanged(format, width, height);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            CameraHandler handler = mCameraThread.getHandler();
            if (handler != null) {
                handler.sendSurfaceDestroyed();
            }
        }
    };

    private MainHandler mMainHandler = new MainHandler();

}