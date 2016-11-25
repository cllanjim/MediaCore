package com.zxzx74147.mediacore.components.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.zxzx74147.mediacore.components.muxer.timestamp.TimeStampGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zxzx74147 on 2016/11/9.
 */

public class Mp4Muxer {
    private static final String TAG = Mp4Muxer.class.getName();

    private final boolean VERBOSE = true;
    private MediaMuxer mMuxer = null;
    public int mVideoTrackIndex = -1;
    private int mAudioTrackIndex = -1;

    private volatile boolean mVideoFinished = false;
    private volatile boolean mAudioFinished = false;
    private volatile boolean mIsStarted = false;
    private Object mStartLock = new Object();
    private File mDstFile = null;

    public Mp4Muxer() {
    }

    public void setOutputFile(File file) {
        mDstFile = file;
    }

    public void init() {
        try {
            if (mDstFile.exists()) {
                mDstFile.delete();
            }
            mDstFile.createNewFile();
            mMuxer = new MediaMuxer(mDstFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVideoTrackIndex = -1;
        mAudioTrackIndex = -1;
        mVideoFinished = false;
        mAudioFinished = false;
        mIsStarted = false;
    }

    public void addVideoTrack(MediaFormat format) {
        Log.d(TAG, "video start");
        mVideoTrackIndex = mMuxer.addTrack(format);
        if(mVideoTrackIndex<0){
            Log.e(TAG, "audio add error mVideoTrackIndex="+mVideoTrackIndex);
        }
        checkStart();
    }

    public void checkStart() {
        if (mVideoTrackIndex >= 0 && mAudioTrackIndex >= 0) {
            mMuxer.start();
            mIsStarted = true;
            synchronized (mStartLock) {
                mStartLock.notifyAll();
            }
        }

    }

    public void addAudioTrack(MediaFormat format) {
        if(VERBOSE) Log.d(TAG, "audio start");
        mAudioTrackIndex = mMuxer.addTrack(format);
        if(mAudioTrackIndex<0){
            Log.e(TAG, "audio add error mAudioTrackIndex="+mAudioTrackIndex);
        }
        checkStart();
    }

    public void writeVideo(ByteBuffer buffer, MediaCodec.BufferInfo info) {

        if (mVideoFinished) {
            if (VERBOSE) Log.e(TAG, "writeVideo when video finished ");
            return;
        }
        if (!mIsStarted) {
            try {
                synchronized (mStartLock) {
                    mStartLock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (VERBOSE) Log.d(TAG, "writeVideo = " + info.size + "|timestamp=" + info.presentationTimeUs);


        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mVideoFinished = true;
            if (info.presentationTimeUs == 0) {
                info.presentationTimeUs = TimeStampGenerator.sharedInstance().getAudioStamp();
            }
            Log.d(TAG, "video finish");
        }
        mMuxer.writeSampleData(mVideoTrackIndex, buffer, info);
        if (mVideoFinished && mAudioFinished) {
            finish();
        }
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    public void writeAudio(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (mAudioFinished) {
            if (VERBOSE) Log.e(TAG, "writeVideo when audio finished ");
            return;
        }
        if (!mIsStarted) {
            try {
                synchronized (mStartLock) {
                    mStartLock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (VERBOSE) Log.d(TAG, "writeAudio = " + info.size + "|timestamp=" + info.presentationTimeUs);

        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mAudioFinished = true;
            if (info.presentationTimeUs == 0) {
                info.presentationTimeUs = TimeStampGenerator.sharedInstance().getAudioStamp();
            }
            Log.d(TAG, "audio finish");
        }
        mMuxer.writeSampleData(mAudioTrackIndex, buffer, info);
        if (mVideoFinished && mAudioFinished) {
            finish();
        }
    }

    public void finish() {
        Log.d(TAG, "finish");
        if (mIsStarted) {
            mIsStarted = false;
            try {
                mMuxer.release();
            } catch (Exception e) {

            }
        }

    }

    public void reset() {
        try {
            mMuxer.release();
        } catch (Exception e) {

        }
        init();
    }
}