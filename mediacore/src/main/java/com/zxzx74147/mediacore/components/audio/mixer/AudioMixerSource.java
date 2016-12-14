package com.zxzx74147.mediacore.components.audio.mixer;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.zxzx74147.mediacore.components.audio.data.AudioRawData;
import com.zxzx74147.mediacore.components.audio.source.IAudioRawConsumer;
import com.zxzx74147.mediacore.components.audio.source.IAudioSource;
import com.zxzx74147.mediacore.components.util.StateConfig;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhengxin on 2016/12/14.
 */

public class AudioMixerSource implements IAudioSource {
    private boolean VERBOSE = true;
    private static final int FRAGMENT_LEN = 4096;

    private int mState = StateConfig.STATE_NONE;
    private IAudioRawConsumer mEncoder = null;
    private List<IAudioSource> mAudioSources = null;
    private Thread mMixThread = null;


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
        for (IAudioSource source : mAudioSources) {
            source.prepare();
            source.setLoop(true);
        }
        mAudioSources.get(0).setLoop(false);
        mState = StateConfig.STATE_PREPARED;
    }

    @Override
    public void start() {
        if (mMixThread != null) {
            throw new IllegalStateException("The MixThread state is Running:");
        }
        if (mState != StateConfig.STATE_PREPARED) {
            throw new IllegalStateException("The AudioMixerSource state is :" + mState);
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

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

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
        return null;
    }

    private Runnable mMixRunnable = new Runnable() {
        @Override
        public void run() {
            mState = StateConfig.STATE_RUNNING;

            while (!Thread.interrupted()) {
                IAudioSource mFirstSource = mAudioSources.get(0);
                AudioRawData data = mFirstSource.pumpAudioBuffer(FRAGMENT_LEN);


                if ((data.info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }

        }
    };

}
