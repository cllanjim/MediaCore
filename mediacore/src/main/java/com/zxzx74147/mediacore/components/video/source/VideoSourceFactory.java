package com.zxzx74147.mediacore.components.video.source;

import android.net.Uri;

import java.io.File;

/**
 * Created by zhengxin on 2016/11/25.
 */

public class VideoSourceFactory {
    public static IVideoSource createCameraSource(){
        return new VideoCameraSource();
    }

    public static IVideoSource createMediaSource(File source){
        return new VideoMediaSource(source);
    }

    public static IVideoSource createMediaSource(Uri uri) {
        return new VideoMediaSource(uri);
    }

    public static IVideoSource createImageSource(File source){
        return new VideoImageSource(source);
    }

    public static IVideoSource createImageSource(Uri uri) {
        return new VideoImageSource(uri);
    }
}
