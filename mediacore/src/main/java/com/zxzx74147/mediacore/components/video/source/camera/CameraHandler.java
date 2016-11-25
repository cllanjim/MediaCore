package com.zxzx74147.mediacore.components.video.source.camera;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.lang.ref.WeakReference;

/**
 * Handler for PlayerCameraThread.  Used for messages sent from the UI thread to the render thread.
 * <p/>
 * The object is created on the render thread, and the various "send" methods are called
 * from the UI thread.
 */
public class CameraHandler extends Handler {
    private static final String TAG = "PlayerRenderHandler";
    private static final int MSG_SURFACE_AVAILABLE = 0;
    private static final int MSG_SURFACE_CHANGED = 1;
    private static final int MSG_SURFACE_DESTROYED = 2;
    private static final int MSG_SHUTDOWN = 3;
    private static final int MSG_FRAME_AVAILABLE = 4;
    private static final int MSG_REDRAW = 5;
    private static final int MSG_ENCODER_AVAILABLE = 6;
    private static final int MSG_SWITCH_CAMERA = 7;
    private static final int MSG_SWITCH_FLASH = 8;
    private static final int MSG_ENCODER_SIZE_CHANGED = 9;
    private static final int MSG_ENCODER_PAUSE = 10;
    private static final int MSG_ENCODER_RESUME = 11;

    // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
    // but no real harm in it.
    private WeakReference<CameraThread> mWeakCameraThread;

    /**
     * Call from render thread.
     */
    public CameraHandler(CameraThread rt) {
        mWeakCameraThread = new WeakReference<CameraThread>(rt);
    }

    /**
     * Sends the "surface available" message.  If the surface was newly created (i.e.
     * this is called from surfaceCreated()), set newSurface to true.  If this is
     * being called during Activity startup for a previously-existing surface, set
     * newSurface to false.
     * <p/>
     * The flag tells the caller whether or not it can expect a renderSurfaceChanged() to
     * arrive very soon.
     * <p/>
     * Call from UI thread.
     */
    public void sendSurfaceAvailable(Surface surface) {
        sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE, 0, 0, surface));
    }

    /**
     * Sends the "surface available" message.  If the surface was newly created (i.e.
     * this is called from surfaceCreated()), set newSurface to true.  If this is
     * being called during Activity startup for a previously-existing surface, set
     * newSurface to false.
     * <p/>
     * The flag tells the caller whether or not it can expect a renderSurfaceChanged() to
     * arrive very soon.
     * <p/>
     * Call from UI thread.
     */
    public void sendEncoderAvailable(Surface surface) {
        sendMessage(obtainMessage(MSG_ENCODER_AVAILABLE,
                surface));
    }

    public void sendEncoderSurfaceChanged( int width,
                                   int height) {
        // ignore format
        sendMessage(obtainMessage(MSG_ENCODER_SIZE_CHANGED, width, height));
    }

    /**
     * Sends the "surface changed" message, forwarding what we got from the SurfaceHolder.
     * <p/>
     * Call from UI thread.
     */
    public void sendSurfaceChanged(@SuppressWarnings("unused") int format, int width,
                                   int height) {
        // ignore format
        sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height));
    }

    /**
     * Sends the "shutdown" message, which tells the render thread to halt.
     * <p/>
     * Call from UI thread.
     */
    public void sendSurfaceDestroyed() {
        sendMessage(obtainMessage(MSG_SURFACE_DESTROYED));
    }

    /**
     * Sends the "shutdown" message, which tells the render thread to halt.
     * <p/>
     * Call from UI thread.
     */
    public void sendShutdown() {
        sendMessage(obtainMessage(MSG_SHUTDOWN));
    }

    /**
     * Sends the "frame available" message.
     * <p/>
     * Call from UI thread.
     */
    public void sendFrameAvailable() {
        sendMessage(obtainMessage(MSG_FRAME_AVAILABLE));
    }

    public void switchCamera() {
        sendMessage(obtainMessage(MSG_SWITCH_CAMERA, 0));
    }

    public void switchFlash(int mode) {
        sendMessage(obtainMessage(MSG_SWITCH_FLASH, mode,0));
    }

    public void sendPauseRecord() {
        sendMessage(obtainMessage(MSG_ENCODER_PAUSE));
    }

    public void sendResumeRecord() {
        sendMessage(obtainMessage(MSG_ENCODER_RESUME));
    }

    /**
     * Sends the "redraw" message.  Forces an immediate redraw.
     * <p/>
     * Call from UI thread.
     */
    public void sendRedraw() {
        sendMessage(obtainMessage(MSG_REDRAW));
    }

    @Override  // runs on PlayerCameraThread
    public void handleMessage(Message msg) {
        int what = msg.what;
        //Log.d(TAG, "PlayerRenderHandler [" + this + "]: what=" + what);

        CameraThread CameraThread = mWeakCameraThread.get();
        if (CameraThread == null) {
            Log.w(TAG, "PlayerRenderHandler.handleMessage: weak ref is null");
            return;
        }

        switch (what) {
            case MSG_SURFACE_AVAILABLE:
                CameraThread.renderSurfaceAvailable((Surface) msg.obj);
                break;
            case MSG_SURFACE_CHANGED:
                CameraThread.renderSurfaceChanged(msg.arg1, msg.arg2);
                break;
            case MSG_SURFACE_DESTROYED:
                CameraThread.renderSurfaceDestroyed();
                break;
            case MSG_SHUTDOWN:
                CameraThread.shutdown();
                break;
            case MSG_FRAME_AVAILABLE:
                CameraThread.frameAvailable();
                break;
            case MSG_REDRAW:
                CameraThread.draw();
                break;
            case MSG_ENCODER_AVAILABLE:
                CameraThread.encoderSurfaceAvailable((Surface) msg.obj);
                break;
            case MSG_ENCODER_SIZE_CHANGED:
                CameraThread.codecSurfaceChanged(msg.arg1, msg.arg2);
                break;
            case MSG_SWITCH_CAMERA:
                CameraThread.switchCamera();
                break;
            case MSG_SWITCH_FLASH:
                CameraThread.switchFlash(msg.arg1);
                break;
            case MSG_ENCODER_PAUSE:
                CameraThread.pauseRecord();
                break;
            case MSG_ENCODER_RESUME:
                CameraThread.resumeRecord();
                break;
            default:
                throw new RuntimeException("unknown message " + what);
        }

    }
}
