package com.zxzx74147.mediacore.components.audio.mixer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.zxzx74147.mediacore.components.audio.data.AudioRawData;
import com.zxzx74147.mediacore.components.audio.source.IAudioRawConsumer;
import com.zxzx74147.mediacore.components.audio.source.IAudioSource;
import com.zxzx74147.mediacore.components.util.StateConfig;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhengxin on 2016/12/14.
 */

public class AudioMixerSource implements IAudioSource {
    private boolean VERBOSE = false;
    private static final String TAG = AudioMixerSource.class.getName();
    private static final int FRAGMENT_LEN = 4096;

    private int mState = StateConfig.STATE_NONE;
    private IAudioRawConsumer mEncoder = null;
    private List<IAudioSource> mAudioSources = null;
    private Thread mMixThread = null;
    private MediaFormat mOutputFormat = null;

    private ByteBuffer mMixBuffer = ByteBuffer.allocate(FRAGMENT_LEN * 2);
    private IProcessListener mListener;


    public AudioMixerSource() {
        mAudioSources = new LinkedList<>();
    }

    public void addAudioSource(IAudioSource source) {
        if (mState != StateConfig.STATE_NONE) {
            throw new IllegalStateException("The AudioMixerSource state is :" + mState);
        }
        if (mAudioSources.contains(source)) {
            throw new IllegalArgumentException("The AudioSource has added in:" + source.toString());
        }
        mAudioSources.add(source);
    }

    @Override
    public void prepare() throws IOException {
        if (mAudioSources.size() < 1) {
            throw new IllegalArgumentException("The AudioSource number is:" + mAudioSources.size());
        }
        if(mOutputFormat==null) {
            mOutputFormat = new MediaFormat();
            mOutputFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
            mOutputFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        }
        for (IAudioSource source : mAudioSources) {
            source.setExpectFormat(mOutputFormat);
            source.prepare();
            source.setLoop(true);
        }
        mAudioSources.get(0).setLoop(false);
        mState = StateConfig.STATE_PREPARED;
        if (mEncoder != null) {
            mEncoder.setOutputFormat(mOutputFormat);
            mEncoder.prepare();
        }
    }

    @Override
    public void start() {
        if (mMixThread != null) {
            throw new IllegalStateException("The MixThread state is Running:");
        }
        if (mState != StateConfig.STATE_PREPARED) {
            throw new IllegalStateException("The AudioMixerSource state is :" + mState);
        }
        if (mEncoder != null) {
            mEncoder.start();
        }
        mMixThread = new Thread(mMixRunnable);
        mMixThread.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void setAudioEncoder(IAudioRawConsumer encoder) {
        mEncoder = encoder;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void release() {
        for (IAudioSource source : mAudioSources) {
            source.release();
        }
    }

    @Override
    public void setProcessListener(IProcessListener listener) {

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
        // TODO not use now
        return null;
    }

    @Override
    public void setExpectFormat(MediaFormat format) {
        mOutputFormat = format;
    }

    private Runnable mMixRunnable = new Runnable() {
        @Override
        public void run() {
            mState = StateConfig.STATE_RUNNING;

            while (!Thread.interrupted()) {
                IAudioSource firstSource = mAudioSources.get(0);
                AudioRawData firstData = firstSource.pumpAudioBuffer(FRAGMENT_LEN);
                IAudioSource secondSource = mAudioSources.get(1);
                AudioRawData secondData = secondSource.pumpAudioBuffer(FRAGMENT_LEN);
                int len = AudioNdkInterface.mix(firstData.data, firstData.info.size, firstData.info.size, secondData.data, secondData.info.size);

                if(VERBOSE){
                    Log.i(TAG,String.format("mix source1 len = %d |source2 len= %d|result len= %d",firstData.info.size, secondData.info.size,len));
                }
                mMixBuffer.clear();
                mMixBuffer.put(firstData.data, 0, len);
                mMixBuffer.flip();
                firstData.info.offset = 0;
                firstData.info.size = len;
                mEncoder.drainAudioRawData(false, mMixBuffer, firstData.info);

                if ((firstData.info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    mEncoder.drainAudioRawData(true, null, firstData.info);
                    break;
                }
            }
            release();

        }
    };

}
