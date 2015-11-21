package sg.edu.nus.upload;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;


public class MainActivity extends AppCompatActivity
        implements CameraView.CameraReadyCallback{

    private static String TAG = "MainActivity@@@@";
    private final int PictureWidth = 1280;
    private final int PictureHeight = 720;


    private CameraView cameraView = null;
    private boolean isStarted = false;
    private ReentrantLock previewLock = new ReentrantLock();
    private MP4Generator mp4Genera = null;
    private HttpMoudle httpMoudle = new HttpMoudle();
    private boolean isButtonLocked = false;

    ExecutorService executor = Executors.newFixedThreadPool(3);
    VideoEncodingTask videoTask = new  VideoEncodingTask();
    MP4GeneratorTask mp4Task = new MP4GeneratorTask();
    HttpTask httpTask = new HttpTask();

    private TextView text_upload = null;
    private TextView text_filename = null;
    private TextView text_net = null;

    Timer timer_update = new Timer();

    byte[] yuvFrame = new byte[1920 * 1280 *2];

    private boolean inProcessing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCamera();
        mp4Genera = new MP4Generator(PictureWidth, PictureHeight);
        executor.execute(httpTask);

        text_upload = (TextView)findViewById(R.id.text_upload);
        text_filename = (TextView)findViewById(R.id.filename);
        text_net = (TextView)findViewById(R.id.net);

        timer_update.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() //run on ui thread
                {
                    public void run()
                    {
                        int upload = mp4Genera.GetUploadIndx();
                        int all = mp4Genera.GetSegs();
                        text_upload.setText("Upload: " + upload + " / " + all);
                        text_filename.setText("Filename: " + mp4Genera.GetFilePrefix());
                    }
                });
            }
        }, 500, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonClick(View v) {
        Button button = (Button)v;
        if (isButtonLocked)
            return;
        isButtonLocked = true;
        if (!isStarted) {
            button.setText("Stop");
            isStarted = true;
            cameraView.StartPreview();
            //mp4Genera.Start();
            executor.execute(mp4Task);
        } else {
            isStarted = false;
            button.setText("Start");
            mp4Genera.Stop();
           // cameraView.StartPreview();
        }
        Log.d(TAG, "Button Pressed");
        isButtonLocked = false;
    }
    private void initCamera() {
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surface_camera);
        cameraView = new CameraView(surfaceView);
        cameraView.setCameraReadyCallback(this);
        Log.d(TAG, "InitCamera end!");
    }

    public void onCameraReady() {
        cameraView.StopPreview();
        cameraView.setupCamera(PictureWidth, PictureHeight, 4, 25.0, previewCb);
        cameraView.StartPreview();
        Log.d(TAG, "onCameraReady");
    }

    private void doDataCopy(byte[] data) {
        if (inProcessing == true) {
            return;
        }
        inProcessing = true;
        int picWidth = cameraView.Width();
        int picHeight = cameraView.Height();
        int size = picWidth*picHeight + picWidth*picHeight/2;
        System.arraycopy(data, 0, yuvFrame, 0, size);
        executor.execute(videoTask);
    }

    private void doHttp() {
        Log.d(TAG, "dohttp");
        while(true) {
            Log.d(TAG, "take 11");
            Segment segment = mp4Genera.GetSegment();
            Log.d(TAG, "take 22");
            long start = 0;
            long end = 0;
            double delay = 0;

            if (segment != null) {
                do {
                    Log.d(TAG, "take 22");
                    start = System.nanoTime();
                    boolean rlt = httpMoudle.SendSegment(segment.GetPath(),
                            segment.GetFilename(),
                            segment.GetSn(),
                            segment.isLast() ? 1 : 0);
                    end = System.nanoTime();
                    delay = (double)(end - start) / 1000000L;
                    if (rlt)
                        break;
                }while(true);
                if (!segment.isLast()) {
                    mp4Genera.IncUplodIndx();
                    final int upload = mp4Genera.GetUploadIndx();
                    final int all = mp4Genera.GetSegs();
                    final int inner_delay = (int)delay;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_upload.setText("Upload: " + upload + " / " + all);
                            text_net.setText("Delay: " + inner_delay + "ms");
                        }
                    });
                }
               Log.d(TAG, "File is sent: " + segment.GetFilename()+"_" + segment.GetSn() + " delay: "+delay +" Last: " + segment.isLast() );
            } else {
                Log.d(TAG, "Got a bad segment");
            }

        }
    }
    private class VideoEncodingTask implements Runnable {
        public void run() {
            mp4Genera.Encode(yuvFrame);
            inProcessing = false;
        }
    }

    private class HttpTask implements  Runnable {
        public void run() {
            doHttp();
        }
    }
    private class MP4GeneratorTask implements Runnable {
        public void run() {
            mp4Genera.Start();
        }
    }

    private PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] frame, Camera c) {
            // Log.d("###", "DATA");
            previewLock.lock();
            //  mp4Genera.Encode(frame);
            doDataCopy(frame);
            c.addCallbackBuffer(frame);
            previewLock.unlock();
            //  Log.d("####", "DATA end");
        }
    };

}
