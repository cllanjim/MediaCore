package com.zxzx74147.mediacore.components.util;

import android.media.MediaPlayer;
import android.net.Uri;

import com.zxzx74147.mediacore.MediaCore;

/**
 * Created by zhengxin on 2017/1/3.
 */

public class VideoUtil {

    public static long getDuration(Uri media){
        MediaPlayer mp = MediaPlayer.create(MediaCore.getContext(),media);
        long duration = mp.getDuration();
        mp.release();
        return duration;
    }
}
