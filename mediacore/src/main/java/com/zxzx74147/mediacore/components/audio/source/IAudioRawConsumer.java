package com.zxzx74147.mediacore.components.audio.source;

import android.media.MediaCodec;

import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.nio.ByteBuffer;

/**
 * Created by zhengxin on 2016/12/14.
 */

public interface IAudioRawConsumer {

    void prepare();

    void setProcessListener(IProcessListener listener);


    void setBufferSize(int bufferSize);

    void release();

    void start();


    int drainAudioRawData(boolean endOfStream, ByteBuffer inputBuffer, MediaCodec.BufferInfo info);
}
