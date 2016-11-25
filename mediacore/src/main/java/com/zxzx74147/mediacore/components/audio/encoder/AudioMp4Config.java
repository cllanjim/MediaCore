package com.zxzx74147.mediacore.components.audio.encoder;

import android.media.MediaCodecInfo;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class AudioMp4Config {

    public static final String MIME_TYPE_AUDIO = "audio/mp4a-latm";    // H.264 Advanced Video Coding
    public static final int OUTPUT_AUDIO_BIT_RATE = 32 * 1024;         //32K
    public static final int OUTPUT_AUDIO_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectHE;
    public static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; //44.1K

    public int outputAudioBitRate = OUTPUT_AUDIO_BIT_RATE;
    public int outputAudioAACProfile = OUTPUT_AUDIO_AAC_PROFILE;
    public int putputAudioSampleRateHz = OUTPUT_AUDIO_SAMPLE_RATE_HZ;

}
