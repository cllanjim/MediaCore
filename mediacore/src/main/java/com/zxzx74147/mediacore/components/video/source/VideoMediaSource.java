package com.zxzx74147.mediacore.components.video.source;

import android.media.MediaExtractor;
import android.view.SurfaceView;

import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class VideoMediaSource implements IVideoSource {

    private MediaExtractor mExtractor = null;

    public void setupExtractor(MediaExtractor extractor) {
        mExtractor = null;
    }

    @Override
    public void prepare() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setRenderSurfaceView(SurfaceView surface) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void setVideoEncoder(VideoEncoder mVideoEncoder) {

    }

}
