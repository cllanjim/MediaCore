package com.zxzx74147.mediacore.components.audio.source;

import android.media.AudioFormat;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class AudioMicConfig {
    public static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100;

    public int frequence = OUTPUT_AUDIO_SAMPLE_RATE_HZ; //录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
    public int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
}
