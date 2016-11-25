package com.zxzx74147.mediacore.components.util;

import android.os.Environment;

import com.zxzx74147.mediacore.MediaCore;

import java.io.File;

/**
 * Created by zhengxin on 2016/11/2.
 */

public class FileUtil {

    private static final String PATH_SD_CARD = MediaCore.getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
    public static File getFile(String path){
        String dst = PATH_SD_CARD+ File.separator+path;
        File file =  new File(dst);
        if(!file.exists()){
            file.getParentFile().mkdirs();
        }
        return file;
    }
}
