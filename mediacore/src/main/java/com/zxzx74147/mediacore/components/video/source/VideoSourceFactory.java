package com.zxzx74147.mediacore.components.video.source;

/**
 * Created by zhengxin on 2016/11/25.
 */

public class VideoSourceFactory {
    public static IVideoSource createCameraSource(){
        return new VideoCameraSource();
    }
}
