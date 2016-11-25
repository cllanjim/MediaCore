package com.zxzx74147.mediacore.components.audio.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.zxzx74147.mediacore.components.audio.encoder.AudioEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.zxzx74147.mediacore.components.util.TimeUtil.TIMEOUT_USEC;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class AudioMediaSource implements IAudioSource {
    private static final String TAG = AudioMediaSource.class.getName();
    private boolean VERBOSE = false;

    private MediaExtractor mExtractor = null;
    private File mFile = null;
    private int mAudioTrack = -1;
    private Thread mExtractThread = null;
    private AudioEncoder mEncoder = null;
    private MediaCodec mAudioDecoder = null;

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
            }
        }
        if (mAudioTrack <= 0) {
            throw new IllegalArgumentException("media file does not include audio track! " + (mFile != null ? mFile.toString() : ""));
        }

    }

    @Override
    public void start() {
        if (mEncoder != null) {
            throw new IllegalStateException("The audio encoder is null ! ");
        }
        mExtractThread = new Thread(mExtractRunnable);
        mExtractThread.setName("Extract audio thread");
        mExtractThread.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void setAudioEncoder(AudioEncoder encoder) {
        mEncoder = encoder;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

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
                    }else {
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
                        // not important for us, since we're using Surface
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
//                                                length, mAudioDecoder.latest.presentationTimeUs,
//                                                info.size, info.presentationTimeUs));
//                                    mAudioDecoderOutputBuffers[decoderStatus].get(mAudioBytes, 0, info.size);
//                                    AudioUtil.mixVoice(mAudioBytes, mAudioDecoder.getResult(), Math.min(length, info.size));
//                                    mAudioByteBuffer.position(0);
//                                    info.offset = 0;
//                                    mAudioByteBuffer.put(mAudioBytes, 0, info.size);
//                                    mAudioByteBuffer.flip();
//                                    mEncoder.drainAudioRawData(false, mAudioByteBuffer, info);
//                                } else {
                                mEncoder.drainAudioRawData(false, mAudioDecoderOutputBuffers[decoderStatus], info);
//                                }
                            }
                        }
                        mAudioDecoder.releaseOutputBuffer(decoderStatus, false);
                    }
                }

            }

            mExtractor.release();
            mAudioDecoder.stop();
            mAudioDecoder.release();
        }
    };


}
