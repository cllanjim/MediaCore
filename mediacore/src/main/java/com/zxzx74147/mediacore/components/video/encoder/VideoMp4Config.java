package com.zxzx74147.mediacore.components.video.encoder;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class VideoMp4Config {
    public static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding

    public final int width = 480;
    public final int height = 640;
    public final int bitrate = 1000 * 1024;            // 1Mbps
    public final int framerate = 24;               // 24fps
    public final int iframe_interval = 5;          // 5 seconds between I-frames
}
