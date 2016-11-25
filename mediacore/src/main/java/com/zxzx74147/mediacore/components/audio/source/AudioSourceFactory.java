package com.zxzx74147.mediacore.components.audio.source;

import java.io.File;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class AudioSourceFactory {
    public static IAudioSource createMicSource(){
        return new AudioMicSource();
    }

    public static IAudioSource createMediaSource(File mediaFile){
        return new AudioMediaSource(mediaFile);
    }
}
