package com.zxzx74147.mediacore.components.audio.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import com.zxzx74147.mediacore.MediaCore;
import com.zxzx74147.mediacore.components.audio.data.AudioRawData;
import com.zxzx74147.mediacore.components.audio.encoder.AudioMp4Config;
import com.zxzx74147.mediacore.components.util.StateConfig;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.zxzx74147.mediacore.components.util.TimeUtil.TIMEOUT_USEC;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class AudioMediaSource implements IAudioSource {
    private static final String TAG = AudioMediaSource.class.getName();
    private static final int BUFFER_SIZE = 1024 * 128;
    private boolean VERBOSE = false;


    private MediaExtractor mExtractor = null;
    private File mFile = null;
    private Uri mUri = null;
    private int mAudioTrack = -1;
    private Thread mExtractThread = null;
    private IAudioRawConsumer mEncoder = null;
    private MediaCodec mAudioDecoder = null;
    private IProcessListener mListener = null;
    private MediaFormat mOutputFormat = null;
    private int mMode = StateConfig.PROCESS_MODE_UNKOWN;
    private boolean mLoop = false;
    private boolean mIsOver = false;
//    private byte[] mInput = new byte[BUFFER_SIZE];
//    private byte[] mOutput = new byte[BUFFER_SIZE];
//    private ByteBuffer mOutputByteBuffer =ByteBuffer.allocate(BUFFER_SIZE);

    private byte[] mOutputBuffer = new byte[BUFFER_SIZE];
    private byte[] mResult = new byte[BUFFER_SIZE];
    private MediaCodec.BufferInfo decodeInfo = new MediaCodec.BufferInfo();
    private int mLeftLen = 0;
    private AudioRawData mRawData = new AudioRawData();


    public AudioMediaSource(File mediaFile) {
        mFile = mediaFile;
        if (mFile == null || !mFile.exists()) {
            throw new IllegalArgumentException("media file is not exist" + (mFile != null ? mFile.toString() : ""));
        }
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AudioMediaSource(Uri uri) {
        mUri = uri;
        if (mUri == null) {
            throw new IllegalArgumentException("media file is not exist" + (mUri != null ? mUri.toString() : ""));
        }
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(MediaCore.getContext(), mUri, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void prepare() throws IOException {
        int numTracks = mExtractor.getTrackCount();
        for (int i = 0; i < numTracks; ++i) {
            MediaFormat format = mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                mAudioTrack = i;
                mExtractor.selectTrack(mAudioTrack);
                mAudioDecoder = MediaCodec.createDecoderByType(mime);
                mAudioDecoder.configure(format, null, null, 0);
                mAudioDecoder.start();
                mOutputFormat = format;
                break;
            }
        }
        if (mAudioTrack < 0) {
            throw new IllegalArgumentException("media file does not include audio track! " + (mFile != null ? mFile.toString() : ""));
        }
        if (mEncoder != null) {
            mEncoder.setBufferSize(BUFFER_SIZE);
            mEncoder.prepare();

        }

    }

    @Override
    public MediaFormat getMediaFormat() {
        return mOutputFormat;
    }

    @Override
    public void setLoop(boolean loop) {
        mLoop = loop;
    }

    @Override
    public void start() {
        if (mEncoder == null) {
            throw new IllegalStateException("The audio encoder is null ! ");
        }
        mEncoder.start();
        mExtractThread = new Thread(mExtractRunnable);
        mExtractThread.setName("Extract audio thread");
        mExtractThread.start();
    }

    @Override
    public void stop() {
        if (mExtractThread != null) {
            mExtractThread.interrupt();
        }
        mExtractThread = null;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void setAudioEncoder(IAudioRawConsumer encoder) {
        mEncoder = encoder;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void setProcessListener(IProcessListener listener) {
        mListener = listener;
    }


    //Async mode
    private Runnable mExtractRunnable = new Runnable() {
        @Override
        public void run() {
            boolean extractorDone = false;
            boolean decoderDone = false;
            ByteBuffer[] mAudioDecoderOutputBuffers = mAudioDecoder.getOutputBuffers();
            while (true) {
                if (extractorDone && decoderDone) {
                    break;
                }
                if (!extractorDone) {
                    int inputIndex = mAudioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputIndex < 0) {
                        if (VERBOSE) Log.i(TAG, "Audio decoder is not available!");
                    } else {
                        ByteBuffer buffer = mAudioDecoder.getInputBuffers()[inputIndex];
                        int state = mExtractor.readSampleData(buffer, 0);
                        if (state < 0) {
                            mAudioDecoder.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            if (VERBOSE) Log.i(TAG, "Audio extractor is over");
                            extractorDone = true;
                        } else {
                            int trackIndex = mExtractor.getSampleTrackIndex();
                            if (trackIndex == mAudioTrack) {
                                long presentationTimeUs = mExtractor.getSampleTime();
                                mAudioDecoder.queueInputBuffer(inputIndex, 0, state, presentationTimeUs, mExtractor.getSampleFlags());
                            }
                            mExtractor.advance();
                        }
                    }
                }

                if (!decoderDone) {
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int decoderStatus = mAudioDecoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        if (VERBOSE) Log.d(TAG, "no output from mVideoDecoder available");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not important for us, since we're using SurfaceØ
                        if (VERBOSE) Log.d(TAG, "mAudioDecoder output buffers changed");
                        mAudioDecoderOutputBuffers = mAudioDecoder.getOutputBuffers();
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = mAudioDecoder.getOutputFormat();
                        if (VERBOSE)
                            Log.d(TAG, "mAudioDecoder output format changed: " + newFormat);
                    } else if (decoderStatus < 0) {

                    } else { // decoderStatus >= 0
                        if (VERBOSE)
                            Log.d(TAG, "surface mAudioDecoder given buffer " + decoderStatus +
                                    " (size=" + info.size + ")");
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (VERBOSE) Log.d(TAG, "output EOS");
                            decoderDone = true;
                            mEncoder.drainAudioRawData(true, null, info);
                        } else {
                            if (info.size > 0) {
                                mAudioDecoderOutputBuffers[decoderStatus].position(info.offset);
                                mAudioDecoderOutputBuffers[decoderStatus].limit(info.offset + info.size);
//                                if (mAudioDecoder.hasSource()) {
//                                    int length = mAudioDecoder.pumpAudioBuffer(info.size);
//                                    if (VERBOSE)
//                                        Log.d(TAG, String.format("decode mix audio len=%d,time=%d,audio len = %d time=%d ",
//                                                length, mAudioDecoder.decodeInfo.presentationTimeUs,
//                                                info.size, info.presentationTimeUs));
//                                    mAudioDecoderOutputBuffers[decoderStatus].get(mAudioBytes, 0, info.size);
//                                    AudioUtil.mixVoice(mAudioBytes, mAudioDecoder.getResult(), Math.min(length, info.size));
//                                    mAudioByteBuffer.position(0);
//                                    info.offset = 0;
//                                    mAudioByteBuffer.put(mAudioBytes, 0, info.size);
//                                    mAudioByteBuffer.flip();
//                                    mEncoder.drainAudioRawData(false, mAudioByteBuffer, info);
//                                } else {
                                int samplerate = mOutputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                                int channel = mOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                                if (AudioMp4Config.OUTPUT_AUDIO_SAMPLE_RATE_HZ == samplerate) {
                                    mEncoder.drainAudioRawData(false, mAudioDecoderOutputBuffers[decoderStatus], info);
                                } else {
//                                    mAudioDecoderOutputBuffers[decoderStatus].get(mInput, info.offset, info.size);
//                                    int len = AudioNdkInterface.pcm_convert(mInput, info.size, samplerate, channel, mOutput, AudioMp4Config.OUTPUT_AUDIO_SAMPLE_RATE_HZ);
//                                    if (VERBOSE)
//                                        Log.i(TAG, String.format("input size =%d rate=%d,output size=%d rate=%d", info.size, samplerate, len, AudioMp4Config.OUTPUT_AUDIO_SAMPLE_RATE_HZ));
//                                    info.size = len;
//                                    info.offset =0;
//                                    mOutputByteBuffer.clear();
//                                    mOutputByteBuffer.put(mOutput,0,len);
//                                    mEncoder.drainAudioRawData(false, mOutputByteBuffer, info);
                                }
                            }
                        }
                        mAudioDecoder.releaseOutputBuffer(decoderStatus, false);
                    }
                }

            }

            release();
        }
    };

    public void release() {
        mExtractor.release();
        mAudioDecoder.stop();
        mAudioDecoder.release();
    }


    @Override
    public AudioRawData pumpAudioBuffer(int expectLength) {
        int sum = 0;
        int loopCount = 0;
        while (sum < expectLength) {

            int dstLen = expectLength - sum;
            if (mLeftLen >= dstLen) {
                System.arraycopy(mOutputBuffer, 0, mResult, sum, dstLen);
                mLeftLen -= dstLen;
                System.arraycopy(mOutputBuffer, dstLen, mOutputBuffer, 0, mLeftLen);
                sum += dstLen;
                continue;
            } else if (mLeftLen > 0) {
                System.arraycopy(mOutputBuffer, 0, mResult, sum, mLeftLen);
                sum += mLeftLen;
                mLeftLen = 0;
            }

            loopCount++;
            if (loopCount > 64) {
                if (VERBOSE) Log.d(TAG, "extractor fail");
                mExtractor = null;
                break;
            }

            int inputIndex = mAudioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
            int outputIndex = -1;
            if (!mIsOver) {

                if (inputIndex >= 0) {
                    ByteBuffer buffer = mAudioDecoder.getInputBuffers()[inputIndex];
                    int chunkSize = mExtractor.readSampleData(buffer, 0);
                    if (chunkSize > 0) {
                        long presentationTimeUs = mExtractor.getSampleTime();
                        mAudioDecoder.queueInputBuffer(inputIndex, 0, chunkSize, presentationTimeUs, mExtractor.getSampleFlags());
                        outputIndex = mAudioDecoder.dequeueOutputBuffer(decodeInfo, TIMEOUT_USEC);
                    } else {
                        if (mLoop) {
                            mExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        } else {
                            mIsOver = true;
                            mAudioDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        }
                    }
                    if (!mExtractor.advance()) {
                        if (mLoop) {
                            mExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        } else {
                            mIsOver = true;
                        }
                    }
                } else {
                    release();
                    try {
                        prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (VERBOSE) Log.d(TAG, "no output from mAudioDecoder available");
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not important for us, since we're using Surface
                if (VERBOSE) Log.d(TAG, "mAudioDecoder output buffers changed");
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mAudioDecoder.getOutputFormat();
                if (VERBOSE) Log.d(TAG, "mAudioDecoder output format changed: " + newFormat);
            } else if (outputIndex < 0) {

            } else {
                ByteBuffer decodeBuffer = mAudioDecoder.getOutputBuffers()[outputIndex];
                decodeBuffer.get(mOutputBuffer, mLeftLen, decodeInfo.size);
                mLeftLen += decodeInfo.size;
                mAudioDecoder.releaseOutputBuffer(outputIndex, false);

                mRawData.info.flags = decodeInfo.flags;
                mRawData.info.presentationTimeUs = decodeInfo.presentationTimeUs;
                if ((decodeInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!mLoop) {
                        break;
                    }
                }
            }
        }
        mRawData.data = mResult;
        mRawData.info.size = sum;
        return mRawData;
    }


}
