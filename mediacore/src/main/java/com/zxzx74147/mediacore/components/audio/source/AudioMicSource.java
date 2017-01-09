package com.zxzx74147.mediacore.components.audio.source;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.zxzx74147.mediacore.ErrorDefine;
import com.zxzx74147.mediacore.components.audio.data.AudioRawData;
import com.zxzx74147.mediacore.components.audio.encoder.AudioConfig;
import com.zxzx74147.mediacore.components.muxer.timestamp.TimeStampGenerator;
import com.zxzx74147.mediacore.components.util.StateConfig;
import com.zxzx74147.mediacore.recorder.IProcessListener;

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
    private IAudioRawConsumer mAudioEncoder = null;
    private IProcessListener mListener = null;
    private MediaFormat mOutputFormat = new MediaFormat();
    private int mState = StateConfig.STATE_NONE;

    @Override
    public void prepare() {
        mBufferSize = AudioRecord.getMinBufferSize(mMicConfig.frequence, mMicConfig.channelConfig, mMicConfig.audioEncoding);
        mOutputFormat = MediaFormat.createAudioFormat(AudioConfig.MIME_TYPE_AUDIO,mMicConfig.frequence, mMicConfig.channelConfig== AudioFormat.CHANNEL_IN_MONO? 1:2 );
        //实例化AudioRecord
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, mMicConfig.frequence, mMicConfig.channelConfig, mMicConfig.audioEncoding, mBufferSize);
        if (mInputBuffer == null) {
            mInputBuffer = new byte[mBufferSize * 2];
        }
        if (mByteBuffer == null) {
            mByteBuffer = ByteBuffer.allocate(mBufferSize * 2);
            if(mAudioEncoder!=null){
                mAudioEncoder.setBufferSize(mBufferSize*2);
            }
        }

        mAudioRecord.startRecording();
        if(mAudioEncoder!=null) {
            mAudioEncoder.setOutputFormat(mOutputFormat);
            mAudioEncoder.prepare();
        }
        mState = StateConfig.STATE_PREPARED;
    }

    @Override
    public void start() {
        if(mAudioEncoder!=null) {
            mAudioEncoder.start();
        }
        mRecordThread = new Thread(mRecordRunnable);
        mRecordThread.setName("Record Thread");
        mRecordThread.start();
        mState = StateConfig.STATE_RUNNING;
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
    public void setAudioEncoder(IAudioRawConsumer encoder) {
        mAudioEncoder = encoder;
        switch (mState){
            case StateConfig.STATE_RUNNING:
                mAudioEncoder.setOutputFormat(mOutputFormat);
                mAudioEncoder.prepare();
                mAudioEncoder.start();
                break;
            case StateConfig.STATE_PREPARED:
                mAudioEncoder.setOutputFormat(mOutputFormat);
                mAudioEncoder.prepare();
                break;

        }



    }

    @Override
    public void pause() {
        mIsRecording = false;
    }

    @Override
    public void resume() {
        mIsRecording = true;
    }

    @Override
    public void release() {

    }

    @Override
    public void setProcessListener(IProcessListener listener) {
        mListener = listener;
    }

    @Override
    public MediaFormat getMediaFormat() {
        return null;
    }

    @Override
    public void setLoop(boolean loop) {

    }

    @Override
    public AudioRawData pumpAudioBuffer(int expectLength) {
        return null;
    }

    @Override
    public void setExpectFormat(MediaFormat format) {

    }

    @Override
    public long getDuration() {
        return -1;
    }


    private Runnable mRecordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

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
                if(mListener!=null){
                    mListener.onError(ErrorDefine.ERROR_AUDIO_UNKNOWN,e.getMessage());
                }
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
