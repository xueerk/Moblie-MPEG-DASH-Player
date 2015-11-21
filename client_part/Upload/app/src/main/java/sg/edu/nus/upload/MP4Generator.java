package sg.edu.nus.upload;

/**
 * Created by Administrator on 2015-11-09.
 */


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;


public class MP4Generator
{
    private static final String TAG = "MP$Generator@@@@";
    private boolean VERBOSE = false;
    private static final int DURATION_SEC = 5;
    private volatile boolean isRunning = false;
    private volatile boolean isWorking = false;

    private final int TIMEOUT_USEC = 100000;
    private long timeCount = 0;
    private MediaCodec mediaCodec = null;
    private MediaMuxer muxer = null;

    private static final String outPutDir = Environment.getExternalStorageDirectory().toString() + "/CS5248";
    private String filePrefix  = "";
    private BufferInfo bufferInfo = null;
    private int m_width;
    private int m_height;

    private boolean isMuxerStarted = false;
    private int trackIndex = 0;

    private int allSegs = 0;
    private int mp4Index = 0;
    private int uploadIndx = 0;

    private byte[] yuv420 = null;

    private final String VideoType = "video/avc";
    private static final int mFramerate = 30;
    private static final int mBitrate = 6000000;

    MediaFormat mediaFormat = null;
    MediaFormat newFormat = null;

    //private final Lock lock = new ReentrantLock();
    //Condition conFileList = lock.newCondition();

    BlockingQueue<Segment> segmentQueue = new ArrayBlockingQueue<Segment>(1000);

    @SuppressLint("NewApi")
    public MP4Generator(int width, int height) {

        m_width  = width;
        m_height = height;
        yuv420 = new byte[width*height*3/2];

        try {
            mediaCodec = MediaCodec.createEncoderByType(VideoType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startCodec();
        File dir = new File(outPutDir);
        if (!dir.exists())
            dir.mkdir();

        if (!segmentQueue.isEmpty()) {
            segmentQueue.clear();
        }

        Log.d(TAG, "MP4Generator: Construct!");
    }

    public Segment GetSegment() {
        try {
            return segmentQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int GetUploadIndx () {
        return uploadIndx;
    }

    public void IncUplodIndx() {
        uploadIndx++;
    }

    public int GetSegs() {
        return allSegs;
    }

    public String GetFilePrefix() {
        return filePrefix;
    }

    private void startCodec() {
        mediaFormat = MediaFormat.createVideoFormat(VideoType, m_width, m_height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFramerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        bufferInfo = new MediaCodec.BufferInfo();
        isWorking = true;
    }
    public void Start() {
        isRunning = true;
        mp4Index = 0;
        doOutputFile();
    }
    public void Stop() {
        isRunning = false;
    }

    private void startMuxer(String filename) {
        if(VERBOSE) Log.d(TAG, filename);
        try {
            muxer = new MediaMuxer(filename, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stopMuxer() {
       isWorking = false;
        isMuxerStarted = false;
        mediaCodec.stop();
       // mediaCodec.release();
        muxer.stop();
        muxer.release();
        muxer = null;
        if(VERBOSE) Log.d(TAG, "Muxer is stoped");
    }

    @SuppressLint("NewApi")
    public void Encode(byte[] input)
    {
        if(VERBOSE) Log.d(TAG, "Encode1");

        if (!(isRunning && isWorking))
            return;
        if(VERBOSE) Log.d(TAG, "Encode2");

        swapYV12toI420(input, yuv420, m_width, m_height);
        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0)
            {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(yuv420);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv420.length, System.nanoTime() / 1000, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void doOutputFile() {
        long startWhen = 0;
        long endWhen = 0;
        long realEndTime = 0;
        long duration = 0;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date();
        filePrefix = formatter.format(now);
        String curFilename = null;
        String lastFilenmae = null;
        while (isRunning) {
            if (!isWorking) {
                startCodec();
                isWorking = true;
            }
            lastFilenmae = curFilename;
            if (lastFilenmae != null) {
                segmentQueue.add(new Segment(filePrefix, lastFilenmae, mp4Index, false));
                mp4Index++;
                allSegs++;
            }
            curFilename = outPutDir + "/" + filePrefix + "_" + String.valueOf(mp4Index) + "_3000.mp4";
            startMuxer(curFilename);

            startWhen = System.nanoTime();
            endWhen = startWhen + DURATION_SEC * 1000000000L;
            while (((realEndTime = System.nanoTime()) < endWhen)) {
                writeMp4File(false);
                if (!isRunning)
                    break;
            }
            duration = realEndTime - startWhen;

            if(VERBOSE) Log.d(TAG, "Duration: " + duration);
            stopMuxer();
        }

        File from = new File(curFilename);
        if (duration > 500000000L) {
            duration /= 1000000L;
            String newFilename = outPutDir + "/" + filePrefix + "_" + String.valueOf(mp4Index) + "_" + String.valueOf(duration) + ".mp4";
            File dir = new File(outPutDir);
            if (dir.exists()) {
                File to = new File(newFilename);
                if (from.exists()) {
                    from.renameTo(to);
                    from.delete();
                    segmentQueue.add(new Segment(filePrefix, newFilename, mp4Index, false));
                    allSegs++;
                }

            }
        } else {
            from.delete();
        }

        segmentQueue.add(new Segment(filePrefix, "END", mp4Index+1, true));
    }

    private boolean writeMp4File(boolean endOfStream) {
        ByteBuffer[] encoderOutputBuffers = null; //mediaCodec.getOutputBuffers();
        int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        if (VERBOSE) Log.d(TAG, "encoderStatus: " + encoderStatus);

        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            if (!endOfStream) {
                return true;
            } else {
                return false;
            }
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not expected for an encoder

        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // should happen before receiving buffers, and should only happen once
            Log.d(TAG, "writeMp4File: INFO_OUTPUT_FORMAT_CHANGED");
            if (isMuxerStarted) {
                throw new RuntimeException("format changed twice");
            }

            newFormat = mediaCodec.getOutputFormat();
            Log.d(TAG, "encoder output format changed: " + newFormat);

            // now that we have the Magic Goodies, start the muxer
            trackIndex = muxer.addTrack(newFormat);
            muxer.start();
            isMuxerStarted = true;
        } else if (encoderStatus < 0) {
            Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
        } else {
            encoderOutputBuffers = mediaCodec.getOutputBuffers();
            ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
            if (encodedData == null) {
                throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                        " was null");
            }

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // The codec config data was pulled out and fed to the muxer when we got
                // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                bufferInfo.size = 0;
            }

            if (bufferInfo.size != 0) {
                if (!isMuxerStarted) {
                    throw new RuntimeException("muxer hasn't started");
                }

                // adjust the ByteBuffer values to match BufferInfo (not needed?)
                encodedData.position(bufferInfo.offset);
                encodedData.limit(bufferInfo.offset + bufferInfo.size);

                muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                //if (VERBOSE) Log.d(TAG, "sent " + bufferInfo.size + " bytes to muxer");
            }

            mediaCodec.releaseOutputBuffer(encoderStatus, false);

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                if (!endOfStream) {
                    Log.w(TAG, "reached end of stream unexpectedly");
                } else {
                    if (VERBOSE) Log.d(TAG, "End of stream reached");
                }
                return true;
            }
        }
        return false;
    }

    //yv12 to yuv420p  yvu -> yuv
    private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height)
    {
        System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
        System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);
        System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);
    }

}
