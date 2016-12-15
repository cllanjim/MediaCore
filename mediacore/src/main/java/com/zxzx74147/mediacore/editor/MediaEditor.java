package com.zxzx74147.mediacore.editor;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.zxzx74147.mediacore.components.audio.encoder.AudioEncoder;
import com.zxzx74147.mediacore.components.audio.mixer.AudioMixerSource;
import com.zxzx74147.mediacore.components.audio.source.AudioSourceFactory;
import com.zxzx74147.mediacore.components.audio.source.IAudioSource;
import com.zxzx74147.mediacore.components.muxer.Mp4Muxer;
import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.filter.IChangeFilter;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterType;
import com.zxzx74147.mediacore.components.video.source.IVideoSource;
import com.zxzx74147.mediacore.components.video.source.VideoSourceFactory;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class MediaEditor implements IChangeFilter {
    private static final String TAG = MediaEditor.class.getName();
    private boolean VERBOSE = false;
    private IAudioSource mAudioSource;
    private IVideoSource mVideoSource;
    private AudioEncoder mAudioEncoder;
    private VideoEncoder mVideoEncoder;
    private Mp4Muxer mMp4Muxer;
    private MagicFilterType mFilterType = null;

    private File mInput = null;
    private Uri mInputUri = null;
    private File mMixInput = null;
    private Uri mMixInputUri = null;
    private AssetFileDescriptor mMixInputFileDescriptor = null;


    private File mOuput = null;

    private IProcessListener mListener = null;

    public MediaEditor() {

    }

    public void setInputMedia(File input) {
        mInput = input;

    }

    public void setInputMedia(Uri input) {
        mInputUri = input;
    }

    public void setInputMixMedia(Uri input) {
        mInputUri = input;
    }

    public void setInputMixFileDescriptor(AssetFileDescriptor fileDescriptor) {
        mMixInputFileDescriptor = fileDescriptor;
    }

    public void setOutputMedia(File output) {
        mOuput = output;
    }

    public void setListener(IProcessListener listener) {
        mListener = listener;
        if (mAudioEncoder != null) {
            mAudioEncoder.setProcessListener(mListener);
        }
        if (mVideoEncoder != null) {
            mVideoEncoder.setProcessListener(mListener);
        }
        if (mAudioSource != null) {
            mAudioSource.setProcessListener(mListener);
        }
        if (mVideoSource != null) {
            mVideoSource.setProcessListener(mListener);
        }
        if (mMp4Muxer != null) {
            mMp4Muxer.setProcessListener(mListener);
        }
    }

    public void prepare() throws IOException {
        if ((mInput == null && mInputUri == null) || mOuput == null) {
            throw new IllegalArgumentException("media file is not exist" + (mInput != null ? mInput.toString() : ""));
        }

        if (mInput != null) {
            mAudioSource = AudioSourceFactory.createMediaSource(mInput);
            mVideoSource = VideoSourceFactory.createMediaSource(mInput);
        } else if (mInputUri != null) {
            mAudioSource = AudioSourceFactory.createMediaSource(mInputUri);
            mVideoSource = VideoSourceFactory.createMediaSource(mInputUri);
        }

        IAudioSource mMixSource = null;
        if (mMixInput != null) {
            mMixSource = AudioSourceFactory.createMediaSource(mInput);
        } else if (mMixInputUri != null) {
            mMixSource = AudioSourceFactory.createMediaSource(mInputUri);
        }else if(mMixInputFileDescriptor!=null){
            mMixSource = AudioSourceFactory.createMediaSource(mMixInputFileDescriptor);
        }
        if(mMixSource!=null){
            AudioMixerSource temp = new AudioMixerSource();
            temp.addAudioSource(mAudioSource);
            temp.addAudioSource(mMixSource);
            mAudioSource = temp;
        }


        mAudioEncoder = new AudioEncoder();
        mVideoEncoder = new VideoEncoder();

        mMp4Muxer = new Mp4Muxer();
        mMp4Muxer.setOutputFile(mOuput);
        mMp4Muxer.init();
        mAudioEncoder.setMuxer(mMp4Muxer);
        mVideoEncoder.setMuxer(mMp4Muxer);

        mAudioSource.setAudioEncoder(mAudioEncoder);
        mVideoSource.setVideoEncoder(mVideoEncoder);


        mVideoSource.prepare();
        mAudioSource.prepare();
        setFilter(mFilterType);


    }

    public void start() {
        mAudioSource.start();
        mVideoSource.start();
    }

    public void stop() {
        mAudioSource.stop();
        mVideoSource.stop();
    }


    @Override
    public void setFilter(MagicFilterType type) {
        if (mVideoSource != null && mVideoSource instanceof IChangeFilter) {
            ((IChangeFilter) mVideoSource).setFilter(type);
        } else {
            mFilterType = type;
        }
    }
}
