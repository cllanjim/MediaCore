package com.zxzx74147.mediacoredemo.test;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.zxzx74147.mediacore.components.util.FileSelectUtil;
import com.zxzx74147.mediacore.components.util.FileUtil;
import com.zxzx74147.mediacore.editor.MediaEditor;
import com.zxzx74147.mediacoredemo.R;
import com.zxzx74147.mediacoredemo.base.BaseActivity;
import com.zxzx74147.mediacoredemo.data.IntentData;
import com.zxzx74147.mediacoredemo.databinding.ActivityVideoEditorTestBinding;

import java.io.IOException;

public class VideoEditorTestActivity extends BaseActivity {

    private ActivityVideoEditorTestBinding mBindig = null;
    private MediaEditor mEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBindig = DataBindingUtil.setContentView(this, R.layout.activity_video_editor_test);
        mBindig.setHandler(this);
        mEditor = new MediaEditor();
        IntentData intentData = getIntentData();
        if(intentData!=null) {
            mBindig.videoView.setVideoURI(intentData.uri);
            mBindig.videoView.start();
        }
    }

    public void onSelect(View v) {
        FileSelectUtil.selectFile(this, "video/mp4", new FileSelectUtil.IFileSelector() {
            @Override
            public void onFileSelect(int resultCode, Intent data) {
                if (resultCode == Activity.RESULT_OK) {
                    IntentData intentData = new IntentData();
                    intentData.uri = data.getData();
                    mBindig.videoView.setVideoURI(intentData.uri);
                    mBindig.videoView.start();
                    return;
                }
            }
        });
    }

    public void startEditor(Uri uri) {
        mEditor.setInputMedia(uri);
        mEditor.setOutputMedia(FileUtil.getFile("edit_" + System.currentTimeMillis() + ".mp4"));
        try {
            mEditor.prepare();
            mEditor.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
