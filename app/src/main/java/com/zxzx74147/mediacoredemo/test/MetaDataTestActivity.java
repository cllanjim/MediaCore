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
//                            Movie movie = MovieCreator.build(path);
//
//                            Movie result = new Movie();
//
//                            for (Track t : movie.getTracks()) {
//                                result.addTrack(t);
//                            }
//
//                            Container out = new DefaultMp4Builder().build(result);
//
//                            FileChannel fc = new RandomAccessFile(path.replace(".mp4", "_bak.mp4"), "rw").getChannel();
//                            out.writeContainer(fc);
//                            fc.close();
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