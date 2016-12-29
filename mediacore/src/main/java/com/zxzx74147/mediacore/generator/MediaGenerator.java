package com.zxzx74147.mediacore.generator;

import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;

import com.zxzx74147.mediacore.MediaCore;
import com.zxzx74147.mediacore.components.audio.encoder.AudioEncoder;
import com.zxzx74147.mediacore.components.audio.source.AudioSourceFactory;
import com.zxzx74147.mediacore.components.audio.source.IAudioSource;
import com.zxzx74147.mediacore.components.muxer.Mp4Muxer;
import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.filter.IChangeFilter;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterType;
import com.zxzx74147.mediacore.components.video.source.IVideoSource;
import com.zxzx74147.mediacore.components.video.source.VideoImageSource;
import com.zxzx74147.mediacore.components.video.source.VideoSourceFactory;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhengxin on 2016/11/22.
 */

public class MediaGenerator implements IChangeFilter {
    private static final String TAG = MediaGenerator.class.getName();
    private boolean VERBOSE = true;
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

    public MediaGenerator() {

    }

    public void setInputMedia(File input) {
        mInput = input;

    }

    public void setInputMedia(Uri input) {
        mInputUri = input;
    }

    public void setInputMixMedia(Uri input) {
        mMixInputUri = input;
    }

    public void setInputMixMediaFile(File input) {
        mMixInput = input;
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
            mVideoSource = VideoSourceFactory.createImageSource(mInput);
        } else if (mInputUri != null) {
            mVideoSource = VideoSourceFactory.createImageSource(mInputUri);
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (mMixInput != null) {
            mAudioSource = AudioSourceFactory.createMediaSource(mMixInput);
            retriever.setDataSource(mMixInput.getPath());
        } else if (mMixInputUri != null) {
            mAudioSource = AudioSourceFactory.createMediaSource(mMixInputUri);
            retriever.setDataSource(MediaCore.getContext(),mMixInputUri);
        }else if(mMixInputFileDescriptor!=null){
            mAudioSource = AudioSourceFactory.createMediaSource(mMixInputFileDescriptor);
            retriever.setDataSource(mMixInputFileDescriptor.getFileDescriptor(),mMixInputFileDescriptor.getStartOffset(),mMixInputFileDescriptor.getLength());
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

        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        ((VideoImageSource)mVideoSource).setDuration(Long.valueOf(duration));
        retriever.release(); //释放

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

    public static long getRecordLong(String filePath) {
        MediaPlayer mp = null;
        try {
            mp = new MediaPlayer();
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int duration = mp.getDuration();
        if(mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
        return duration;
    }
}
