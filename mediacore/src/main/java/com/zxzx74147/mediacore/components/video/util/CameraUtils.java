/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zxzx74147.mediacore.components.video.util;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Camera-related utility functions.
 */
public class CameraUtils {
    private static final String TAG = "CameraUtils";

    public static final int FLASH_OFF = 0;
    public static final int FLASH_ON  = 1;


    /**
     * Iterate over supported camera preview sizes to see which one best fits the
     * dimensions of the given view while maintaining the aspect ratio. If none can,
     * be lenient with the aspect ratio.
     *
     * @param w The width of the view.
     * @param h The height of the view.
     * @return Best match camera preview size to fit in the view.
     */
    public static Camera.Size choosePreviewSize(Camera.Parameters parms, int w, int h) {
        // Use a very small tolerance because we want an exact match.
        List<Camera.Size> sizes = parms.getSupportedPreviewSizes();
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;



        ArrayList<ComparedableSize> comparedableSizes = new ArrayList<>();
        for (Camera.Size size : sizes) {
            comparedableSizes.add(new ComparedableSize(size));
        }
        Collections.sort(comparedableSizes);
        optimalSize = comparedableSizes.get(comparedableSizes.size()-1).size;
        Log.i(TAG,"request size:"+w+"x"+h);
        for (ComparedableSize comparedableSize : comparedableSizes) {
            Log.i(TAG,"show size:"+comparedableSize.size.width+"x"+comparedableSize.size.height);
            if(comparedableSize.size.width>=w&&comparedableSize.size.height>=h){
                optimalSize = comparedableSize.size;
                Log.i(TAG,"choose size:"+optimalSize.width+"x"+optimalSize.height);
                break;
            }
        }
        parms.setPreviewSize(optimalSize.width,optimalSize.height);
        return optimalSize;
    }

    private static class ComparedableSize implements Comparable<ComparedableSize>{
        public Camera.Size size;

        public ComparedableSize(Camera.Size size){
            this.size = size;
        }
        @Override
        public int compareTo(ComparedableSize o) {
            return size.width*size.height-o.size.width*o.size.height;
        }
    }

    /**
     * Attempts to find a fixed preview frame rate that matches the desired frame rate.
     *
     * It doesn't seem like there's a great deal of flexibility here.
     *
     * TODO: follow the recipe from http://stackoverflow.com/questions/22639336/#22645327
     *
     * @return The expected frame rate, in thousands of frames per second.
     */
    public static int chooseFixedPreviewFps(Camera.Parameters parms, int desiredThousandFps) {
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            //Log.d(TAG, "entry: " + entry[0] + " - " + entry[1]);
            if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }

        int[] tmp = new int[2];
        parms.getPreviewFpsRange(tmp);
        int guess;
        if (tmp[0] == tmp[1]) {
            guess = tmp[0];
        } else {
            guess = tmp[1] / 2;     // shrug
        }

        Log.d(TAG, "Couldn't find match for " + desiredThousandFps + ", using " + guess);
        return guess;
    }

    public static int chooseAudoFocus(Camera.Parameters parms) {
        List<String> supported = parms.getSupportedFocusModes();
        if(supported.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
            parms.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            return 0;
        }
        return -1;
    }

    public static int getCameraDisplayOrientation(Activity activity,
                                                  int cameraId) {

        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        try {
            android.hardware.Camera.getCameraInfo(cameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
        } catch (Exception e) {//Crash on OPPON5117  Fail to get camera info
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (90 + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (270 - degrees + 360) % 360;
            }
        }
        return result;

    }

    public static void releaseCamera(Camera camera) {
        if(camera==null){
            return;
        }
        try{
            camera.stopPreview();
            camera.release();
        }catch (Exception e){

        }
    }

    public static Camera openCamera(int mCameraId){
        Camera.CameraInfo info = new Camera.CameraInfo();

        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == mCameraId) {
                return Camera.open(i);
            }
        }
        return null;
    }
}
