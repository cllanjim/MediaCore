package com.zxzx74147.mediacore.components.video.source;

import android.view.SurfaceView;

import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;

/**
 * Created by zhengxin on 2016/11/21.
 */

public interface IVideoSource {
    void prepare();

    void start();

    void stop();

    void setRenderSurfaceView(SurfaceView surface);

    void pause();

    void resume();

    void setVideoEncoder(VideoEncoder mVideoEncoder);
}
