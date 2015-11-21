package sg.edu.nus.upload;

/**
 * Created by Administrator on 2015-11-10.
 */


import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.*;
import android.os.Handler;

public class CameraView implements SurfaceHolder.Callback {
    public static interface CameraReadyCallback {
        public void onCameraReady();
    }

    private static String TAG = "CameraView@@@@";
    private static String CAMERA_THREAD_NAME = "CAMERA";
    private Camera camera_ = null;
    private SurfaceHolder surfaceHolder_ = null;
    private SurfaceView surfaceView_;
    CameraReadyCallback cameraReadyCb_ = null;

    private List<int[]> supportedFrameRate;
    private List<Camera.Size> supportedSizes;
    private Camera.Size procSize_;

    private CameraHandlerThread mThread = null;
    private HandlerThread mCameraThread = null;
    private Handler mCameraHandler = null;

    public CameraView(SurfaceView sv) {
        surfaceView_ = sv;

        surfaceHolder_ = surfaceView_.getHolder();
        surfaceHolder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder_.addCallback(this);
    }

    public List<Camera.Size> getSupportedPreviewSize() {
        return supportedSizes;
    }

    public int Width() {
        return procSize_.width;
    }

    public int Height() {
        return procSize_.height;
    }

    public void setCameraReadyCallback(CameraReadyCallback cb) {
        cameraReadyCb_ = cb;
    }

    public void StartPreview() {
        if (camera_ == null)
            return;
        camera_.startPreview();
    }

    public void StopPreview() {
        if (camera_ == null)
            return;
        camera_.stopPreview();
    }

    public void AutoFocus() {
        camera_.autoFocus(afcb);
    }

    public void Release() {
        if (camera_ != null) {
            camera_.stopPreview();
            camera_.release();
            camera_ = null;
        }
    }

    public void setupCamera(int wid, int hei, int bufNumber, double fps, PreviewCallback cb) {

        double diff = Math.abs(supportedSizes.get(0).width * supportedSizes.get(0).height - wid * hei);
        int targetIndex = 0;
        for (int i = 1; i < supportedSizes.size(); i++) {
            double newDiff = Math.abs(supportedSizes.get(i).width * supportedSizes.get(i).height - wid * hei);
            Log.d(TAG, "Support size " + i + ": " + supportedSizes.get(i).width + "x" + supportedSizes.get(i).height);
            if (newDiff < diff) {
                diff = newDiff;
                targetIndex = i;
            }
        }
        Log.d(TAG, wid + "x" + hei);
        procSize_.width = supportedSizes.get(targetIndex).width;
        procSize_.height = supportedSizes.get(targetIndex).height;

        diff = Math.abs(supportedFrameRate.get(0)[0] * supportedFrameRate.get(0)[1] - fps * fps * 1000 * 1000);
        targetIndex = 0;
        for (int i = 1; i < supportedFrameRate.size(); i++) {
            double newDiff = Math.abs(supportedFrameRate.get(i)[0] * supportedFrameRate.get(i)[1] - fps * fps * 1000 * 1000);
            if (newDiff < diff) {
                diff = newDiff;
                targetIndex = i;
            }
        }
        int targetMaxFrameRate = supportedFrameRate.get(targetIndex)[0];
        int targetMinFrameRate = supportedFrameRate.get(targetIndex)[1];

        Camera.Parameters p = camera_.getParameters();
        p.setPreviewSize(procSize_.width, procSize_.height);
        p.setPreviewFormat(ImageFormat.YV12);
        p.setPreviewFpsRange(targetMaxFrameRate, targetMinFrameRate);
        camera_.setParameters(p);

        List<Integer> lists = p.getSupportedPreviewFormats();

        for( Integer i : lists) {
            Log.d(TAG, "format: " + i.toString() );
        }
        PixelFormat pixelFormat = new PixelFormat();
        PixelFormat.getPixelFormatInfo(ImageFormat.NV21, pixelFormat);
        int bufSize = procSize_.width * procSize_.height * pixelFormat.bitsPerPixel / 8;
        byte[] buffer = null;
        for (int i = 0; i < bufNumber; i++) {
            buffer = new byte[bufSize];
            camera_.addCallbackBuffer(buffer);
        }
        camera_.setPreviewCallbackWithBuffer(cb);
    }

    private void initCamera() {
        camera_ = Camera.open();
        //startCamera();
       // newOpenCamera();
        while (camera_ == null) ;
        procSize_ = camera_.new Size(0, 0);
        Camera.Parameters p = camera_.getParameters();

        supportedFrameRate = p.getSupportedPreviewFpsRange();

        supportedSizes = p.getSupportedPreviewSizes();
        procSize_ = supportedSizes.get(supportedSizes.size() / 2);
        p.setPreviewSize(procSize_.width, procSize_.height);

        camera_.setParameters(p);
        //camera_.setDisplayOrientation(90);
        try {
            camera_.setPreviewDisplay(surfaceHolder_);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        camera_.setPreviewCallbackWithBuffer(null);
        camera_.startPreview();

        Camera.Size size = p.getPreviewSize();
        Log.d(TAG, "Camera preview size is " + size.width + "x" + size.height);
    }

    private Camera.AutoFocusCallback afcb = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder sh, int format, int w, int h) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder sh) {
        initCamera();
        if (cameraReadyCb_ != null)
            cameraReadyCb_.onCameraReady();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder sh) {
        Release();
    }

    private void openCamera() {
        camera_ = Camera.open();
    }

    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }
        synchronized (mThread) {
            mThread.openCamera();
        }
    }

    private static class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    openCamera();
                    notifyCameraOpened();
                }
            });
            /*
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(TAG, "wait was interrupted");
            }
            */
        }
    }
}
