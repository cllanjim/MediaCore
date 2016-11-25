package com.zxzx74147.mediacore.editor;

import com.zxzx74147.mediacore.components.audio.encoder.AudioEncoder;
import com.zxzx74147.mediacore.components.audio.source.IAudioSource;
import com.zxzx74147.mediacore.components.muxer.Mp4Muxer;
import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.source.IVideoSource;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class MediaEditor {
    private IAudioSource mAudioSource;
    private IVideoSource mVideoSource;
    private AudioEncoder mAudioEncoder;
    private VideoEncoder mVideoEncoder;
    private Mp4Muxer mMp4Muxer;


}
