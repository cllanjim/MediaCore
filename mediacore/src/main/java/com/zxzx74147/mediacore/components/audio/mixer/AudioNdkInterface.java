package com.zxzx74147.mediacore.components.audio.mixer;

/**
 * Created by zhengxin on 2016/12/12.
 */

public class AudioNdkInterface {

    static  {
        System.loadLibrary("audio_interface" );
    }

    public static native int pcm_convert(byte[] pbyteInBuffer, int dwInLength, int dwInSampleRate,
                                  int dwChannal, byte[] pbyteOutBuffer, int dwOutSampleRate);

}
