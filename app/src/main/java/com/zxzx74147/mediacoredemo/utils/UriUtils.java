package com.zxzx74147.mediacoredemo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

/**
 * Created by zhengxin on 2016/12/22.
 */

public class UriUtils {
    private static int SDK_VERSION = Build.VERSION.SDK_INT;

    public static String getRealPathFromURI(Context context, Uri uri) {
        if (SDK_VERSION >= 19) {
            return getRealPathFromURI_API19(context, uri);
        } else if (SDK_VERSION >= 11) {
            return getRealPathFromURI_API11to18(context, uri);
        } else {
            return getRealPathFromURI_BelowAPI11(context, uri);
        }
    }

    @SuppressLint("NewApi")
    private static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        if(uri.toString().startsWith("content://com.estrongs.files")){
            return uri.toString().replace("content://com.estrongs.files","");
        }

        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        String content = wholeID.split(":")[0];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = null;
        if (content.endsWith("video")) {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
        } else if (content.endsWith("image")) {
            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
        } else if (content.endsWith("audio")) {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
        }


        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    private static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    private static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
