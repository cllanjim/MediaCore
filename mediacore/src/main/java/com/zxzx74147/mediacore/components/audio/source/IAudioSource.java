package com.zxzx74147.mediacore.components.audio.source;

import com.zxzx74147.mediacore.components.audio.encoder.AudioEncoder;

import java.io.IOException;

/**
 * Created by zhengxin on 2016/11/21.
 */

public interface IAudioSource {
    void prepare() throws IOException;

    void start();

    void stop();

    int getBufferSize();

    void setAudioEncoder(AudioEncoder encoder);

    void pause();

    void resume();
}
