package com.zxzx74147.mediacore.components.video.source;

import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.encoder.VideoMp4Config;
import com.zxzx74147.mediacore.components.video.filter.IChangeFilter;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterType;
import com.zxzx74147.mediacore.components.video.source.camera.CameraHandler;
import com.zxzx74147.mediacore.components.video.source.camera.CameraThread;
import com.zxzx74147.mediacore.components.video.source.camera.MainHandler;
import com.zxzx74147.mediacore.recorder.IProcessListener;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class VideoCameraSource implements IVideoSource ,IChangeFilter{
    private static final String TAG = VideoCameraSource.class.getName();


    private SurfaceView mSurfaceView = null;
    private CameraThread mCameraThread = null;
    private VideoEncoder mVideoEncoder = null;
    private MagicFilterType mMagicFilterType = null;
    private IProcessListener mListener = null;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    public void prepare() {

    }

    @Override
    public void start() {
        mCameraThread = new CameraThread(mMainHandler);
        mCameraThread.start();
        mCameraThread.waitUntilReady();
        setVideoEncoder(mVideoEncoder);
        setFilter(mMagicFilterType);
        setCameraId(mCameraId);
    }

    @Override
    public void stop() {
        if (mCameraThread != null) {
            CameraHandler rh = mCameraThread.getHandler();
            rh.sendShutdown();
        }
        if (mVideoEncoder != null) {
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
                mVideoEncoder.prepare(new VideoMp4Config());
                mVideoEncoder.start();
                rh.sendEncoderAvailable(mVideoEncoder.getEncoderSurface());
            }
        });
    }

    @Override
    public void setProcessListener(IProcessListener listener) {
        mListener = listener;
    }

    @Override
    public void setFilter(MagicFilterType type) {
        if (type == null) {
            return;
        }
        if (mCameraThread != null) {
            CameraHandler rh = mCameraThread.getHandler();
            rh.sendFilterChanged(type);
        }
        mMagicFilterType = type;

    }

    public void setFlashMode(int flashMode){
        if (mCameraThread != null) {
            CameraHandler rh = mCameraThread.getHandler();
            rh.switchFlash(flashMode);
        }
    }

    public void setCameraId(int id){
        if (mCameraThread != null) {
            CameraHandler rh = mCameraThread.getHandler();
            rh.switchCamera(id);
        }else{
            mCameraId = id;
        }
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
