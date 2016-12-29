package com.zxzx74147.mediacore.components.audio.source;

import android.media.MediaFormat;

import com.zxzx74147.mediacore.components.audio.data.AudioRawData;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.IOException;

/**
 * Created by zhengxin on 2016/11/21.
 */

public interface IAudioSource {
    void prepare() throws IOException;

    void start();

    void stop();

    int getBufferSize();

    void setAudioEncoder(IAudioRawConsumer encoder);

    void pause();

    void resume();

    void release();

    void setProcessListener(IProcessListener listener);

    MediaFormat getMediaFormat();

    void setLoop(boolean loop);

    AudioRawData pumpAudioBuffer(int expectLength);

     void setExpectFormat(MediaFormat format);
    long getDuration();

}
