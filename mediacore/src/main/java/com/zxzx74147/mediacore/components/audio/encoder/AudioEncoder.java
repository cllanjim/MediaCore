package com.zxzx74147.mediacore.components.audio.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.zxzx74147.mediacore.ErrorDefine;
import com.zxzx74147.mediacore.components.audio.source.IAudioRawConsumer;
import com.zxzx74147.mediacore.components.muxer.Mp4Muxer;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.zxzx74147.mediacore.components.audio.encoder.AudioConfig.MIME_TYPE_AUDIO;
import static com.zxzx74147.mediacore.components.util.TimeUtil.TIMEOUT_USEC;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class AudioEncoder implements IAudioRawConsumer {
    private static final String TAG = AudioEncoder.class.getName();
    private boolean VERBOSE = false;
    private volatile MediaCodec mAudioEncoder = null;
    private Thread mEncoderThread = null;
    private Mp4Muxer mMp4Muxer = null;
    private long mLastTime = 0;
    private int bufferSize = 1024 * 128;
    private IProcessListener mListener = null;
    private MediaFormat mOutputFormat = null;


    @Override
    public void prepare() {
        release();
        if (mOutputFormat == null) {
            throw new IllegalStateException("mOutputFormat is unknown");
        }
        try {

            int samplerate = mOutputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channel = mOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            mOutputFormat = MediaFormat.createAudioFormat(
                    MIME_TYPE_AUDIO, samplerate, channel);

            mOutputFormat.setInteger(MediaFormat.KEY_BIT_RATE, AudioConfig.OUTPUT_AUDIO_BIT_RATE);
            mOutputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, AudioConfig.OUTPUT_AUDIO_AAC_PROFILE);
            mOutputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize * 2);
            mAudioEncoder = MediaCodec.createEncoderByType(AudioConfig.MIME_TYPE_AUDIO);
            mAudioEncoder.configure(mOutputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioEncoder.start();

        } catch (IOException ioe) {
            mListener.error(ErrorDefine.ERROR_AUDIO_ENCODER_INIT_ERROR,ioe.getMessage());
            throw new RuntimeException("failed init mVideoEncoder", ioe);
        }

    }

    public void setOutputFormat(MediaFormat format) {
        mOutputFormat = format;
    }

    @Override
    public void setProcessListener(IProcessListener listener) {
        mListener = listener;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void release() {
        if(mEncoderThread!=null){
            mEncoderThread.interrupt();
            mEncoderThread = null;
        }

    }

    @Override
    public void start() {
        if (mMp4Muxer == null) {
            throw new IllegalStateException("Muxer is not set");
        }
        if (mEncoderThread != null) {
            throw new IllegalStateException("AudioEncoder has started");
        }
        mEncoderThread = new Thread(mEncoderRunnable);
        mEncoderThread.setName("Encoder Thread");
        mEncoderThread.start();
    }

    public void setMuxer(Mp4Muxer muxer) {
        mMp4Muxer = muxer;
    }

    @Override
    public int drainAudioRawData(boolean endOfStream, ByteBuffer inputBuffer, MediaCodec.BufferInfo info) {
        int inputIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputIndex >= 0) {
            if (endOfStream) {
                if (VERBOSE) Log.d(TAG, "sending EOS to drainAudioEncoder");
                mAudioEncoder.queueInputBuffer(inputIndex, 0, 0, info.presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                ByteBuffer buffer = mAudioEncoder.getInputBuffers()[inputIndex];
                buffer.position(0);
                buffer.put(inputBuffer);
                buffer.flip();
                mAudioEncoder.queueInputBuffer(inputIndex, 0, info.size, info.presentationTimeUs, 0);
            }
        } else {
            if (VERBOSE) Log.d(TAG, "audio encode input buffer not available");
        }
        return inputIndex;
    }

    public Runnable mEncoderRunnable = new Runnable() {
        @Override
        public void run() {

            ByteBuffer[] encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
            MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                int encoderStatus = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                if(Thread.interrupted()){
                    break;
                }
                if (VERBOSE)
                    Log.d(TAG, "index=" + encoderStatus + "|mBufferInfo.time=" + mBufferInfo.presentationTimeUs);

                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an mAudioEncoder
                    encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // should happen before receiving buffers, and should only happen once

                    MediaFormat newFormat = mAudioEncoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "mAudioEncoder output format changed: " + newFormat);
                    if (mMp4Muxer != null) {
                        mMp4Muxer.addAudioTrack(newFormat);
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
                        if (mBufferInfo.size > 0) {
                            if (mBufferInfo.presentationTimeUs < mLastTime) {
                                mBufferInfo.presentationTimeUs = mLastTime + 10000;
                            }
                            mLastTime = mBufferInfo.presentationTimeUs;
                        }
                        mMp4Muxer.writeAudio(encodedData, mBufferInfo);
                    }
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " audio bytes to muxer");

                    mAudioEncoder.releaseOutputBuffer(encoderStatus, false);

                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                        break;
                    }
                }
            }
            if (mAudioEncoder != null) {
                mAudioEncoder.stop();
                mAudioEncoder.release();
                mAudioEncoder = null;
            }
        }
    };
}
