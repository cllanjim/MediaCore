package com.zxzx74147.mediacore.components.util;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.SparseArray;

/**
 * Created by zhengxin on 2016/10/26.
 */

public class FileSelectUtil {
    public static int PICK_INTENT = 10000;
    public static int COUNT = 1;
    public interface IFileSelector{
        void onFileSelect(int resultCode, Intent data);
    }

    private static SparseArray<IFileSelector> mSelectors = new SparseArray<>(10);

    public static void selectFile(Activity activity, String type, IFileSelector fileSelector){
        int id = PICK_INTENT+COUNT++;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select "+type), id);
        mSelectors.put(id,fileSelector);
    }

    public static void dealOnActivityResult(int requestCode, int resultCode, Intent data){
        IFileSelector fileSelector = mSelectors.get(requestCode);
        if(fileSelector==null){
            return;
        }
        fileSelector.onFileSelect(resultCode, data);
    }
}
