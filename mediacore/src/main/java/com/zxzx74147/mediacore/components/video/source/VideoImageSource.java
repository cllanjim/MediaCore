package com.zxzx74147.mediacore.components.video.source;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

import com.zxzx74147.mediacore.MediaCore;
import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.encoder.VideoMp4Config;
import com.zxzx74147.mediacore.components.video.filter.IChangeFilter;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterType;
import com.zxzx74147.mediacore.components.video.source.media.CodecOutputSurface;
import com.zxzx74147.mediacore.components.video.util.OpenGlUtils;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class VideoImageSource implements IVideoSource, IChangeFilter {
    private static final String TAG = VideoImageSource.class.getName();
    private static boolean VERBOSE = true;
    private static final int INV = 300;
    private Handler mHandler = null;
    private File mFile = null;
    private Uri mUri = null;
    private int mVideoTrack = -1;
    private Thread mExtractThread = null;
    private VideoEncoder mEncoder = null;
    private CodecOutputSurface outputSurface = null;
    private long mMeidaDuration = 0;
    private int decodeCount = 0;
    private IProcessListener mListener = null;
    private MagicFilterType mFilterType = null;
    private Bitmap mImage = null;
    private int mImageTextureId;
    private boolean mFinish = false;
    private int mTimeStampCount = 0;


    public VideoImageSource(File mediaFile) {
        mFile = mediaFile;
        if (mFile == null || !mFile.exists()) {
            throw new IllegalArgumentException("media file is not exist" + (mFile != null ? mFile.toString() : ""));
        }
        mImage = BitmapFactory.decodeFile(mFile.getAbsolutePath());
        mHandler = new Handler(Looper.getMainLooper());
    }

    public VideoImageSource(Uri uri) {
        mUri = uri;
        if (mUri == null) {
            throw new IllegalArgumentException("media file is not exist" + (mUri != null ? mUri.toString() : ""));
        }
        try {
            InputStream image_stream = MediaCore.getContext().getContentResolver().openInputStream(mUri);
            mImage =  BitmapFactory.decodeStream(image_stream );
            image_stream.close();
            int videoWidth = mImage.getWidth();
            int videoHeight = mImage.getHeight();
            while(videoHeight>1000||videoWidth>1000){
                videoHeight/=2;
                videoWidth/=2;
            }
            videoHeight-=videoHeight%16;
            videoWidth-=videoWidth%16;
            mImage = Bitmap.createScaledBitmap(mImage,videoWidth,videoHeight,true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void prepare() throws IOException {
        if (mEncoder == null) {
            throw new IllegalArgumentException("Video encoder is null! ");
        }
    }

    @Override
    public void start() {
        mExtractThread = new Thread(mExtractRunnable);
        mExtractThread.setName("Video extractor Runnable");
        mExtractThread.start();
    }

    @Override
    public void stop() {
        mFinish = true;
    }

    @Override
    public void setRenderSurfaceView(SurfaceView surface) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void setVideoEncoder(VideoEncoder videoEncoder) {
        mEncoder = videoEncoder;
    }

    @Override
    public void setProcessListener(IProcessListener listener) {
        mListener = listener;
    }

    public void setDuration(long i){
        mMeidaDuration = i;
    }

    public Runnable mExtractRunnable = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.preparedDone((int) mMeidaDuration);
            }
            int videoWidth = mImage.getWidth();
            int videoHeight = mImage.getHeight();



            VideoMp4Config config = new VideoMp4Config();
            config.width = videoWidth;
            config.height = videoHeight;
            config.framerate = 1000/INV;
            config.iframe_interval = 10;
            mEncoder.prepare(config);
            outputSurface = new CodecOutputSurface(config.width, config.height, mEncoder.getEncoderSurface());
            outputSurface.setRevert();
            if (mImage == null) {
                throw new IllegalArgumentException("Video source is null! ");
            }
            mImageTextureId = OpenGlUtils.loadTexture(mImage, OpenGlUtils.NO_TEXTURE);

            setFilter(mFilterType);
            mEncoder.start();
            outputSurface.setVideoWH(videoWidth, videoHeight);


            while (!Thread.interrupted()) {

                if (!mFinish) {
                    outputSurface.makeCurrent(1);
                    outputSurface.drawStaticImage(mImageTextureId);
                    outputSurface.setPresentationTime(mTimeStampCount*1000);
                    mTimeStampCount+=INV;
                    if (VERBOSE)
                        Log.d(TAG, "timestamp=" + mTimeStampCount);
                    mEncoder.drainVideoRawData(false);
                    outputSurface.swapBuffers();
                    decodeCount++;
                    if(mMeidaDuration<=mTimeStampCount){
                        if (VERBOSE) {
                            Log.d(TAG, "draw image done");
                        }
                        break;
                    }
                }

            }
            mEncoder.drainVideoRawData(true);
        }
    };

    @Override
    public void setFilter(MagicFilterType type) {
        mFilterType = type;
        if (outputSurface != null) {
            outputSurface.setFilter(mFilterType);
        }
    }
}
