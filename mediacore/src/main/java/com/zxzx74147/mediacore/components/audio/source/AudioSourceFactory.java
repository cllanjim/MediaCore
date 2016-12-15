package com.zxzx74147.mediacore.components.audio.source;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;

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

    public static IAudioSource createMediaSource(Uri mediaUri){
        return new AudioMediaSource(mediaUri);
    }

    public static IAudioSource createMediaSource(AssetFileDescriptor mMixInputFileDescriptor) {
        return new AudioMediaSource(mMixInputFileDescriptor);
    }
}
