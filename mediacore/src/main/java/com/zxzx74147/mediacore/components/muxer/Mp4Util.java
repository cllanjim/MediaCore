package com.zxzx74147.mediacore.components.muxer;

import net.ypresto.qtfaststart.QtFastStart;

import java.io.File;
import java.io.IOException;

/**
 * Created by zxzx74147 on 2016/11/9.
 */

public class Mp4Util {
    public static String makeMp4Faststart(String path) {
        try {
            String dst = path.replace(".mp4", "_bak1.mp4");
            boolean ret = QtFastStart.fastStart(new File(path), new File(dst));
            if (ret) {
                return dst;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (QtFastStart.MalformedFileException e) {
            e.printStackTrace();
        } catch (QtFastStart.UnsupportedFileException e) {
            e.printStackTrace();
        }
        return path;
    }
}
