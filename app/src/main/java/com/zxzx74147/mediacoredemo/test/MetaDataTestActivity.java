package com.zxzx74147.mediacoredemo.test;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zxzx74147.mediacore.components.util.FileSelectUtil;
import com.zxzx74147.mediacoredemo.R;
import com.zxzx74147.mediacoredemo.base.BaseActivity;
import com.zxzx74147.mediacoredemo.databinding.ActivityMetadataTestBinding;
import com.zxzx74147.mediacoredemo.utils.UriUtils;
import com.zxzx74147.mediacoredemo.widget.TextureVideoView;

import net.ypresto.qtfaststart.QtFastStart;

import java.io.File;
import java.io.IOException;

public class MetaDataTestActivity extends BaseActivity {


    private ActivityMetadataTestBinding mBinging = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinging = DataBindingUtil.setContentView(this, R.layout.activity_metadata_test);
        mBinging.setHandler(this);
        mBinging.video.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);
        mBinging.video.setDataSource("http://pws.myhug.cn/static/cone/video/csys_2.mp4");
        mBinging.video.play();
    }

    public void onSelect(View v) {
        FileSelectUtil.selectFile(this, "video/*", new FileSelectUtil.IFileSelector() {
            @Override
            public void onFileSelect(int resultCode, Intent data) {
                if (data == null || data.getData() == null) {
                    return;
                }
                final String path = UriUtils.getRealPathFromURI(MetaDataTestActivity.this, data.getData());
                Log.i(TAG, "path=" + path);
//                DataSource dataSource =

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String dst = path.replace(".mp4", "_bak1.mp4");
                            boolean ret = QtFastStart.fastStart(new File(path),new File(dst));
                            Log.i(TAG,"ret="+ret);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (QtFastStart.MalformedFileException e) {
                            e.printStackTrace();
                        } catch (QtFastStart.UnsupportedFileException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}