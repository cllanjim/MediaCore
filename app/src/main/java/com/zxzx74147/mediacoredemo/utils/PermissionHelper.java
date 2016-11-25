package com.zxzx74147.mediacoredemo.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhengxin on 2016/11/24.
 */

public class PermissionHelper {
    public static final int REQUEST_CODE = 1001;

    public static int checkPermission(Activity context, String[] permissions) {
        List<String> unsitisfied = new LinkedList<>();
        for (String permission:permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                unsitisfied.add(permission);
            }
        }
        if(unsitisfied.size()==0){
            return 0;
        }
        ActivityCompat.requestPermissions(context, unsitisfied.toArray(new String[unsitisfied.size()]), REQUEST_CODE);
        return unsitisfied.size();
    }
}
