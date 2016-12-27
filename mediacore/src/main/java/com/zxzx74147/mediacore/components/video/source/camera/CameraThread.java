package com.zxzx74147.mediacore.components.video.source.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.zxzx74147.mediacore.components.muxer.timestamp.TimeStampGenerator;
import com.zxzx74147.mediacore.components.video.filter.base.MagicSurfaceInputFilter;
import com.zxzx74147.mediacore.components.video.filter.base.gpuimage.GPUImageFilter;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterFactory;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterType;
import com.zxzx74147.mediacore.components.video.source.camera.gles.EglCore;
import com.zxzx74147.mediacore.components.video.source.camera.gles.GlUtil;
import com.zxzx74147.mediacore.components.video.source.camera.gles.WindowSurface;
import com.zxzx74147.mediacore.components.video.util.CameraUtils;
import com.zxzx74147.mediacore.components.video.util.OpenGlUtils;
import com.zxzx74147.mediacore.components.video.util.Rotation;
import com.zxzx74147.mediacore.components.video.util.TextureRotationUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by zhengxin on 2016/11/22.
 */
public class CameraThread extends Thread implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = CameraThread.class.getName();

    private Object mStartLock = new Object();
    private boolean mReady = false;
    private CameraHandler mCameraHandler = null;
    private MainHandler mMainHandler = null;

    private boolean hasShow = false;

    private SurfaceTexture mCameraTexture;
    private Camera mCamera = null;
    private CameraConfig mCameraConfig = new CameraConfig();
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int mCameraPreviewWidth, mCameraPreviewHeight;

    private EglCore mEglCore;
    private WindowSurface mRenderWindowSurface;
    private int mRenderWidth, mRenderHeight;
    private int mRenderWindowWidth, mRenderWindowHeight;

    private WindowSurface mCodecWindowSurface;
    private int mCodecWidth, mCodecHeight;

    private int textureId = OpenGlUtils.NO_TEXTURE;

    private GPUImageFilter mImageFilter;
    private MagicSurfaceInputFilter mSurfaceFilter = null;
    private FloatBuffer mGLCubeBuffer;
    private FloatBuffer mGLTextureBuffer;

    private MagicFilterType mFilterType = MagicFilterType.NONE;

    private boolean mIsRcording = false;


    public CameraThread(MainHandler handler) {
        mMainHandler = handler;

        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_ROTATED_90.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);
        mGLTextureBuffer.clear();
        float[] textureCords = TextureRotationUtil.getRotation(Rotation.fromInt(90), false, true);
        mGLTextureBuffer.put(textureCords).position(0);
    }

    @Override
    public void run() {
        Looper.prepare();
        try {
            mCameraHandler = new CameraHandler(this);

            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();    // signal waitUntilReady()
            }
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
            prepareTexture();
            try {
                openCamera(mCameraConfig.cameraReqWidth, mCameraConfig.cameraReqHeight, mCameraConfig.cameraReqFps);
            } catch (RuntimeException e) {
                if (mMainHandler != null) {
                    mMainHandler.sendEmptyMessage(MainHandler.MSG_SEND_OPEN_CAMERA_FAIL);
                }
            }
            Looper.loop();
        } catch (Exception e) {
            if (mMainHandler != null) {
                mMainHandler.sendEmptyMessage(MainHandler.MSG_SEND_UNKNOWN_FAIL);
            }
            e.printStackTrace();
        } finally {
            Log.d(TAG, "looper quit");
            release();
            synchronized (mStartLock) {
                mReady = false;
            }
        }
    }


    /**
     * Waits until the render thread is ready to receive messages.
     * <p/>
     * Call from the UI thread.
     */
    public void waitUntilReady() {
        synchronized (mStartLock) {
            while (!mReady) {
                try {
                    mStartLock.wait();
                } catch (InterruptedException ie) { /* not expected */ }
            }
        }
    }

    public CameraHandler getHandler() {
        return mCameraHandler;
    }

    public void shutdown() {
        Looper.myLooper().quit();
    }

    public void openCamera(int desiredWidth, int desiredHeight, int desiredFps) {
        if (mCamera != null) {
            CameraUtils.releaseCamera(mCamera);
            mCamera = null;
//            throw new RuntimeException("camera already initialized");
        }
        mCamera = CameraUtils.openCamera(mCameraId);

        Camera.Parameters parms = mCamera.getParameters();

        CameraUtils.choosePreviewSize(parms, desiredWidth, desiredHeight);

        CameraUtils.chooseFixedPreviewFps(parms, desiredFps * 1000);
        parms.setRecordingHint(true);
        CameraUtils.chooseAudoFocus(parms);
        mCamera.setParameters(parms);
        Camera.Size mCameraPreviewSize = parms.getPreviewSize();
//        mCamera.setDisplayOrientation(90);
        mCameraPreviewWidth = mCameraPreviewSize.height;
        mCameraPreviewHeight = mCameraPreviewSize.width;
    }

    public void switchCamera(int id) {
        if (id == mCameraId) {
            return;
        }
        mCameraId = id;
        releaseCamera();

        openCamera(mCameraConfig.cameraReqWidth, mCameraConfig.cameraReqHeight, mCameraConfig.cameraReqFps);
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            CameraUtils.releaseCamera(mCamera);
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    public void startPreview() {
        try {
            mCamera.setPreviewTexture(mCameraTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resumeRecord() {
        mIsRcording = true;
    }

    public void pauseRecord() {
        mIsRcording = false;
    }

    public void release() {
        releaseCamera();
        releaseGl();
        releaseWindowSurface();
    }

    private void prepareTexture() {

    }


    public void renderSurfaceAvailable(Surface surface) {

        if (mRenderWindowSurface == null) {
            try {
                mRenderWindowSurface = new WindowSurface(mEglCore, surface, false);
                mRenderWindowSurface.makeCurrent();
            } catch (IllegalArgumentException e) {
                if (mMainHandler != null) {
                    mMainHandler.sendEmptyMessage(MainHandler.MSG_SEND_OPEN_INVALID_SURFACE_AVAILABLE);
                }
            } catch (Exception e) {
                if (mMainHandler != null) {
                    mMainHandler.sendEmptyMessage(MainHandler.MSG_SEND_SURFACE_INVALID_CREATE);
                }
            }
        }

        if (mCameraTexture == null) {
            textureId = OpenGlUtils.getExternalOESTextureID();
            mCameraTexture = new SurfaceTexture(textureId);

            mRenderWindowWidth = mRenderWindowSurface.getWidth();
            mRenderWindowHeight = mRenderWindowSurface.getHeight();
            mRenderWidth = mRenderWindowWidth;
            mRenderHeight = mRenderWindowHeight;
            mCameraTexture.setOnFrameAvailableListener(this);

            mImageFilter = MagicFilterFactory.initFilters(mFilterType);
            mImageFilter.init();
            mSurfaceFilter = new MagicSurfaceInputFilter();
            mSurfaceFilter.init();

            mImageFilter.onDisplaySizeChanged(mRenderWidth, mRenderHeight);
            mImageFilter.onInputSizeChanged(mCameraPreviewWidth, mCameraPreviewHeight);
            mSurfaceFilter.onDisplaySizeChanged(mRenderWidth, mRenderHeight);
            mSurfaceFilter.onInputSizeChanged(mCameraPreviewWidth, mCameraPreviewHeight);
        }
        startPreview();
    }

    public void renderSurfaceChanged(int width, int height) {
        mRenderWindowWidth = width;
        mRenderWindowHeight = height;
        updateGeometry();
    }

    public void codecSurfaceChanged(int width, int height) {
        mCodecWidth = width;
        mCodecHeight = height;
        updateGeometry();
    }

    public void renderSurfaceDestroyed() {
        releaseWindowSurface();
    }

    public void encoderSurfaceAvailable(Surface encoderSurface) {
        if (mCodecWindowSurface != null) {
            try {
                mCodecWindowSurface.release();
                mCodecWindowSurface = null;
            } catch (Exception e) {

            }
        }
        try {
            mCodecWindowSurface = new WindowSurface(mEglCore, encoderSurface, true);
        } catch (IllegalArgumentException e) {
            if (mMainHandler != null) {
                mMainHandler.sendEmptyMessage(MainHandler.MSG_SEND_OPEN_INVALID_SURFACE_AVAILABLE);
            }
        } catch (Exception e) {
            if (mMainHandler != null) {
                mMainHandler.sendEmptyMessage(MainHandler.MSG_SEND_SURFACE_INVALID_CREATE);
            }
        }
    }


    private void releaseWindowSurface() {
        if (mRenderWindowSurface != null) {
            mRenderWindowSurface.release();
            mRenderWindowSurface = null;
        }

    }


    private void releaseGl() {
        GlUtil.checkGlError("releaseGl start");

        if (mRenderWindowSurface != null) {
            mRenderWindowSurface.release();
            mRenderWindowSurface = null;
        }
        if (mCodecWindowSurface != null) {
            mCodecWindowSurface.release();
            mCodecWindowSurface = null;
        }

        if (mCameraTexture != null) {
            mCameraTexture.release();
            mCameraTexture = null;
        }
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            //TODO
        }
        GlUtil.checkGlError("releaseGl done");
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }

    public void frameAvailable() {
        if (!hasShow) {
            hasShow = true;
            mMainHandler.sendEmptyMessage(MainHandler.MSG_SEND_PREVIEW_START);
        }
        try {
            mCameraTexture.updateTexImage();
            draw();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    float[] mtx = new float[16];

    public void draw() {

        mCameraTexture.getTransformMatrix(mtx);
        mSurfaceFilter.setTextureTransformMatrix(mtx);
        int id = mSurfaceFilter.onDrawToTexture(textureId);

        if (mRenderWindowSurface != null) {
            mRenderWindowSurface.makeCurrent();
            GLES20.glViewport(0, mRenderWindowHeight - mRenderHeight, mRenderWidth, mRenderHeight);
            mImageFilter.onDrawFrame(id, mGLCubeBuffer, mGLTextureBuffer);
            mRenderWindowSurface.swapBuffers();
        }

        if (mCodecWindowSurface != null && mIsRcording) {
            mCodecWindowSurface.makeCurrent();
            GLES20.glViewport(0, 0, mCodecWidth, mCodecHeight);
            mImageFilter.onDrawFrame(id, mGLCubeBuffer, mGLTextureBuffer);
            mCodecWindowSurface.setPresentationTime(TimeStampGenerator.sharedInstance().getVideoStamp());
            mCodecWindowSurface.swapBuffers();
        }
    }


    public void switchFlash(int state) {
        Camera.Parameters parameters = mCamera.getParameters();
        if (state != 0) {
            if(parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
            }
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//关闭
        }
        mCamera.setParameters(parameters);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mCameraHandler.sendFrameAvailable();
    }

    private void updateGeometry() {
        if (mCameraPreviewWidth == 0 || mCameraPreviewHeight == 0) {
            return;
        }
        if (mRenderHeight == 0 || mRenderWidth == 0) {
            return;
        }
        if (mImageFilter == null) {
            return;
        }
        mRenderHeight = mRenderWindowWidth * mCameraPreviewHeight / mCameraPreviewWidth;
        mRenderWidth = mRenderWindowWidth;
        mImageFilter.onDisplaySizeChanged(mRenderWidth, mRenderHeight);
        mImageFilter.onInputSizeChanged(mCameraPreviewWidth, mCameraPreviewHeight);

        if (mSurfaceFilter == null) {
            return;
        }
        mSurfaceFilter.onDisplaySizeChanged(mCameraPreviewWidth, mCameraPreviewHeight);
        mSurfaceFilter.onInputSizeChanged(mCameraPreviewWidth, mCameraPreviewHeight);
        mSurfaceFilter.initSurfaceFrameBuffer(mCameraPreviewWidth, mCameraPreviewHeight);
    }

    public void changeFilter(MagicFilterType type) {
        mFilterType = type;
        if (mImageFilter != null) {
            mImageFilter.destroy();
            mImageFilter = MagicFilterFactory.initFilters(type);
            mImageFilter.init();
            mImageFilter.onDisplaySizeChanged(mRenderWidth, mRenderHeight);
            mImageFilter.onInputSizeChanged(mCameraPreviewWidth, mCameraPreviewHeight);
        }


    }
}
