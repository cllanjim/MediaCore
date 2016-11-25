package com.zxzx74147.mediacore.components.video.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.zxzx74147.mediacore.components.muxer.Mp4Muxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.zxzx74147.mediacore.components.util.TimeUtil.TIMEOUT_USEC;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class VideoEncoder {
    private static final String TAG = VideoEncoder.class.getName();
    private boolean VERBOSE = false;
    private volatile MediaCodec mVideoEncoder = null;
    private Thread mEncoderThread = null;
    private Mp4Muxer mMp4Muxer = null;
    private Surface mEncodesurface;
    private VideoMp4Config mConfig = new VideoMp4Config();

    public void prepare() {
        release();

        MediaFormat format = MediaFormat.createVideoFormat(VideoMp4Config.MIME_TYPE, mConfig.width, mConfig.height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mConfig.bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mConfig.framerate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mConfig.iframe_interval);

        mVideoEncoder = null;

        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VideoMp4Config.MIME_TYPE);
            mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncodesurface = mVideoEncoder.createInputSurface();
            mVideoEncoder.start();
        } catch (IOException ioe) {
            throw new RuntimeException("failed init mVideoEncoder", ioe);
        }

    }

    public VideoMp4Config getConfig(){
        return mConfig;
    }

    public void release() {
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }

    public void start() {
        if (mEncoderThread != null) {
            mEncoderThread.interrupt();
            mEncoderThread = null;
        }
        mEncoderThread = new Thread(mEncoderRunnable);
        mEncoderThread.setName("Encoder Thread");
        mEncoderThread.start();
    }

    public void setMuxer(Mp4Muxer muxer) {
        mMp4Muxer = muxer;
    }

    public Surface getEncoderSurface(){
        return mEncodesurface;
    }

    public int drainVideoRawData(boolean endOfStream) {
        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to mVideoEncoder");
            mVideoEncoder.signalEndOfInputStream();
        }
        return 0;
    }

    public Runnable mEncoderRunnable = new Runnable() {
        @Override
        public void run() {

            ByteBuffer[] encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
            MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);

                if (VERBOSE)
                    Log.d(TAG, "index=" + encoderStatus + "|mBufferInfo.time=" + mBufferInfo.presentationTimeUs);

                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an mVideoEncoder
                    encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // should happen before receiving buffers, and should only happen once

                    MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "mVideoEncoder output format changed: " + newFormat);
                    if (mMp4Muxer != null) {
                        mMp4Muxer.addVideoTrack(newFormat);
                    } else {
                        if (VERBOSE) Log.d(TAG, "no Mp4Muxer added" + newFormat);
                    }
                } else if (encoderStatus < 0) {
                    Log.w(TAG, "unexpected result from mVideoEncoder.dequeueOutputBuffer: " +
                            encoderStatus);
                    // let's ignore it
                } else {

                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                                " was null");
                    }

                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                        mBufferInfo.size = 0;
                    }

                    if (mMp4Muxer != null) {
                        mMp4Muxer.writeVideo(encodedData, mBufferInfo);
                    }
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " audio bytes to muxer");

                    mVideoEncoder.releaseOutputBuffer(encoderStatus, false);

                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                        break;
                    }
                }
            }
        }
    };
}