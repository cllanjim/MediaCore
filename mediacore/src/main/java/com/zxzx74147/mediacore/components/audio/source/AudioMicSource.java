package com.zxzx74147.mediacore.components.audio.source;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.util.Log;

import com.zxzx74147.mediacore.components.audio.encoder.AudioEncoder;
import com.zxzx74147.mediacore.components.muxer.timestamp.TimeStampGenerator;

import java.nio.ByteBuffer;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class AudioMicSource implements IAudioSource {
    private static final String TAG = AudioMicSource.class.getName();
    private static final boolean VERBOSE = false;
    private AudioMicConfig mMicConfig = new AudioMicConfig();
    private AudioRecord mAudioRecord = null;

    private ByteBuffer mByteBuffer = null;
    private byte[] mInputBuffer = null;
    private Thread mRecordThread = null;
    private MediaCodec.BufferInfo mEncodeInfo = new MediaCodec.BufferInfo();
    private volatile boolean mIsRecording = false;
    private volatile boolean mIsStop = false;
    private int mBufferSize = 0;
    private AudioEncoder mAudioEncoder = null;


    @Override
    public void prepare() {
        mBufferSize = AudioRecord.getMinBufferSize(mMicConfig.frequence, mMicConfig.channelConfig, mMicConfig.audioEncoding);
        //实例化AudioRecord
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, mMicConfig.frequence, mMicConfig.channelConfig, mMicConfig.audioEncoding, mBufferSize);
        if (mInputBuffer == null) {
            mInputBuffer = new byte[mBufferSize * 2];
        }
        if (mByteBuffer == null) {
            mByteBuffer = ByteBuffer.allocate(mBufferSize * 2);
        }
        mAudioRecord.startRecording();
    }

    @Override
    public void start() {
        mRecordThread = new Thread(mRecordRunnable);
        mRecordThread.setName("Record Thread");
        mRecordThread.start();
    }

    @Override
    public void stop() {
        mIsStop = true;
    }

    @Override
    public int getBufferSize() {
        return mBufferSize;
    }

    @Override
    public void setAudioEncoder(AudioEncoder encoder) {
        mAudioEncoder = encoder;
        mAudioEncoder.prepare();
        mAudioEncoder.start();
    }

    @Override
    public void pause() {
        mIsRecording = false;
    }

    @Override
    public void resume() {
        mIsRecording = true;
    }

    private Runnable mRecordRunnable = new Runnable() {
        @Override
        public void run() {
            try {

                while (true) {
                    //从bufferSize中读取字节，返回读取的short个数
                    int bufferReadResult = mAudioRecord.read(mInputBuffer, 0, mInputBuffer.length);
                    if (bufferReadResult < 0) {
                        Log.e(TAG, "error bufferReadResult=" + bufferReadResult);
                        break;
                    }
                    if (mAudioEncoder != null) {

                        if (mIsRecording) {
                            mByteBuffer.position(0);
                            mByteBuffer.put(mInputBuffer, 0, bufferReadResult);
                            mByteBuffer.flip();
                            mEncodeInfo.offset = 0;
                            mEncodeInfo.size = bufferReadResult;
                            mEncodeInfo.presentationTimeUs = TimeStampGenerator.sharedInstance().getAudioStamp();
                            mAudioEncoder.drainAudioRawData(false, mByteBuffer, mEncodeInfo);
                        }

                        if (mIsStop) {
                            mEncodeInfo.flags |= MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                            mEncodeInfo.size = 0;
                            mEncodeInfo.presentationTimeUs = TimeStampGenerator.sharedInstance().getAudioStamp();
                            mAudioEncoder.drainAudioRawData(true, mByteBuffer, mEncodeInfo);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (mAudioRecord != null) {
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;
                }
                if (VERBOSE) Log.d(TAG, "record stop!");
            }

        }
    };
}