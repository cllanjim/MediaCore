package com.zxzx74147.mediacore.components.video.encoder;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class VideoMp4Config {
    public static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding

    public int width = 480;
    public int height = 640;
    public int bitrate = (int) (width*height/0.4);            // 1Mbps
    public int framerate = 24;               // 24fps
    public int iframe_interval = 5;          // 5 seconds between I-frames
}
