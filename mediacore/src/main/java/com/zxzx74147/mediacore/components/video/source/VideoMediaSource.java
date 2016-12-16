package com.zxzx74147.mediacore.components.video.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

import com.zxzx74147.mediacore.MediaCore;
import com.zxzx74147.mediacore.components.video.encoder.VideoEncoder;
import com.zxzx74147.mediacore.components.video.encoder.VideoMp4Config;
import com.zxzx74147.mediacore.components.video.filter.IChangeFilter;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterType;
import com.zxzx74147.mediacore.components.video.source.media.CodecOutputSurface;
import com.zxzx74147.mediacore.recorder.IProcessListener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.zxzx74147.mediacore.components.util.TimeUtil.TIMEOUT_USEC;

/**
 * Created by zhengxin on 2016/11/21.
 */

public class VideoMediaSource implements IVideoSource ,IChangeFilter{
    private static final String TAG = VideoMediaSource.class.getName();
    private boolean VERBOSE = false;

    private Handler mHandler = null;
    private MediaExtractor mExtractor = null;
    private File mFile = null;
    private Uri mUri = null;
    private int mVideoTrack = -1;
    private Thread mExtractThread = null;
    private VideoEncoder mEncoder = null;
    private MediaCodec mVideoDecoder = null;
    private CodecOutputSurface outputSurface = null;
    private long mMeidaDuration = 0;
    private int decodeCount = 0;
    private IProcessListener mListener = null;
    private MagicFilterType mFilterType = null;

    public VideoMediaSource(File mediaFile) {
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
        mHandler = new Handler(Looper.getMainLooper());
    }

    public VideoMediaSource(Uri uri) {
        mUri = uri;
        if (mUri == null ) {
            throw new IllegalArgumentException("media file is not exist" + (mUri != null ? mUri.toString() : ""));
        }
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(MediaCore.getContext(),mUri,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void prepare() throws IOException {

        int numTracks = mExtractor.getTrackCount();
        for (int i = 0; i < numTracks; ++i) {
            MediaFormat format = mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                mVideoTrack = i;
                mExtractor.selectTrack(mVideoTrack);
                mVideoDecoder = MediaCodec.createDecoderByType(mime);
                mVideoDecoder.configure(format, null, null, 0);
                mVideoDecoder.start();
                break;
            }
        }
        if (mVideoTrack < 0) {
            throw new IllegalArgumentException("media file does not include video track! " + (mFile != null ? mFile.toString() : ""));
        }

        if (mEncoder == null) {
            throw new IllegalArgumentException("Video encoder is null! ");
        }



    }

    @Override
    public void start() {
        mExtractThread = new Thread(mExtractRunnable);
        mExtractThread.setName("Video extractor Runnable");
        mExtractThread.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void setRenderSurfaceView(SurfaceView surface) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void setVideoEncoder(VideoEncoder videoEncoder) {
        mEncoder = videoEncoder;
    }

    @Override
    public void setProcessListener(IProcessListener listener) {
        mListener = listener;
    }

    public Runnable mExtractRunnable = new Runnable() {
        @Override
        public void run() {
            MediaFormat format = mExtractor.getTrackFormat(mVideoTrack);
            if (VERBOSE) {
                Log.d(TAG, "Video size is " + format.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                        format.getInteger(MediaFormat.KEY_HEIGHT));
            }
            mMeidaDuration = format.getLong(MediaFormat.KEY_DURATION);
            if(mListener!=null){
                  mListener.preparedDone((int) mMeidaDuration);
            }
            int videoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            int videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
            int rotation = 0;
            try {
                rotation = format.getInteger(MediaFormat.KEY_ROTATION);
            } catch (Exception e) {

            }
            if (rotation % 180 == 90) {
                videoHeight = videoHeight + videoWidth;
                videoWidth = videoHeight - videoWidth;
                videoHeight = videoHeight - videoWidth;
            }
            VideoMp4Config config = new VideoMp4Config();
            config.width = videoWidth;
            config.height = videoHeight;
            mEncoder.prepare(config);
            outputSurface = new CodecOutputSurface(config.width, config.height, mEncoder.getEncoderSurface());
            setFilter(mFilterType);
            mEncoder.start();
            outputSurface.setVideoWH(videoWidth, videoHeight);

            String mime = format.getString(MediaFormat.KEY_MIME);
            try {
                mVideoDecoder = MediaCodec.createDecoderByType(mime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mVideoDecoder.configure(format, outputSurface.getSurface(), null, 0);
            mVideoDecoder.start();

            boolean extractorDone = false;
            boolean decoderDone = false;
            while (true) {
                if (extractorDone && decoderDone) {
                    break;
                }
                if (!extractorDone) {
                    int inputIndex = mVideoDecoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputIndex < 0) {
                        if (VERBOSE) Log.i(TAG, "Audio decoder is not available!");
                    } else {
                        ByteBuffer buffer = mVideoDecoder.getInputBuffers()[inputIndex];
                        int state = mExtractor.readSampleData(buffer, 0);
                        if (state < 0) {
                            mVideoDecoder.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            if (VERBOSE) Log.i(TAG, "Audio extractor is over");
                            extractorDone = true;
                        } else {
                            int trackIndex = mExtractor.getSampleTrackIndex();
                            if (trackIndex == mVideoTrack) {


                                long presentationTimeUs = mExtractor.getSampleTime();
                                mVideoDecoder.queueInputBuffer(inputIndex, 0, state, presentationTimeUs, mExtractor.getSampleFlags());
                            }
                            mExtractor.advance();
                        }
                    }
                }

                if (!decoderDone) {
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int decoderStatus = mVideoDecoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        if (VERBOSE) Log.d(TAG, "no output from mVideoDecoder available");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not important for us, since we're using Surface
                        if (VERBOSE) Log.d(TAG, "mVideoDecoder output buffers changed");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = mVideoDecoder.getOutputFormat();
                        if (VERBOSE)
                            Log.d(TAG, "mVideoDecoder output format changed: " + newFormat);
                    } else if (decoderStatus < 0) {

                    } else { // decoderStatus >= 0
                        if (VERBOSE)
                            Log.d(TAG, "surface mVideoDecoder given buffer " + decoderStatus +
                                    " (size=" + info.size + ")");
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (VERBOSE) Log.d(TAG, "output EOS");
                            decoderDone = true;
                            mEncoder.drainVideoRawData(true);
                            mVideoDecoder.releaseOutputBuffer(decoderStatus, false);
                        } else {
                            boolean doRender = (info.size != 0);
                            mVideoDecoder.releaseOutputBuffer(decoderStatus, doRender);
                            if (doRender) {
                                if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);

                                outputSurface.makeCurrent(1);
                                outputSurface.awaitNewImage();
                                outputSurface.drawImage(true);
                                outputSurface.setPresentationTime(info.presentationTimeUs*1000);
                                if(VERBOSE) Log.d(TAG,"timestamp="+info.presentationTimeUs);
                                mEncoder.drainVideoRawData(false);
                                outputSurface.swapBuffers();
                                decodeCount++;
                            }
                            if(mListener!=null){
                                mListener.progress((int) info.presentationTimeUs);
                            }
                        }

                    }
                }

            }
        }
    };

    @Override
    public void setFilter(MagicFilterType type) {
        mFilterType = type;
        if(outputSurface!=null) {
            outputSurface.setFilter(mFilterType);
        }
    }
}
