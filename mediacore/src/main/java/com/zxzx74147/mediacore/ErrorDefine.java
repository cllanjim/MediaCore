package com.zxzx74147.mediacore;

/**
 * Created by zhengxin on 2016/11/28.
 */

public class ErrorDefine {
    private static final int ERROR_AUDIO = 100;
    public static final int ERROR_AUDIO_UNKNOWN = ERROR_AUDIO + 0;
    public static final int ERROR_AUDIO_SOURCE_AUDIORECORD_ERROR = ERROR_AUDIO + 1;
    public static final int ERROR_AUDIO_ENCODER_INIT_ERROR = ERROR_AUDIO + 2;

    private static final int ERROR_VIDEO = 200;
    public static final int ERROR_VIDEO_UNKNOWN = ERROR_VIDEO + 0;
    public static final int ERROR_VIDEO_SOURCE_CAMERA_ERROR = ERROR_VIDEO + 1;
    public static final int ERROR_VIDEO_SURFACE_DRAW_ERROR = ERROR_VIDEO + 2;
    public static final int ERROR_VIDEO_ENCODER_INIT_ERROR = ERROR_VIDEO + 3;

    private static final int ERROR_MUXER = 300;
    public static final int ERROR_MUXER_UNKNOWN = ERROR_MUXER + 0;
    public static final int ERROR_MUXER_LOSE_TRACK = ERROR_MUXER + 1;
    public static final int ERROR_MUXER_STATUS_ERROR = ERROR_MUXER + 2;
    public static final int ERROR_MUXER_FINISH_ERROR = ERROR_MUXER + 3;
}
