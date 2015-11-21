package cs5248.assign.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;


import java.io.File;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class PlayerFragment extends Fragment implements
        SurfaceHolder.Callback, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaController.MediaPlayerControl {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String TAG = "@@@@";

    private final int MAX_BUFFER_SIZE = 5;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener listener;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private MediaPlayer backupMediaPlayer;
    private MediaController mediaController;

    private BlockingQueue playBufferQueue;

    private Handler playHandler;

    // path of download segments
    private SegmentDownloader segmentDownloader = null;

    private int numPlayedSeg;

    final Lock lock = new ReentrantLock();
    Condition playSlot = lock.newCondition();

     /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerFragment newInstance(String param1, String param2) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerFragment() {
        // Required empty public constructor
        playBufferQueue = new ArrayBlockingQueue(MAX_BUFFER_SIZE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_player, container, false);

        surfaceView=(SurfaceView) view.findViewById(R.id.surface_view);

        surfaceHolder = surfaceView.getHolder(); // set surface holder and set listeners
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        new Thread(new Runnable() { // Start segment handling as background task
            @Override
            public void run() {
                //handleNextSegment();
                playSegement();
            }
        }).start();




        return view;
    }

    public void startPlay(VideoInfo v) {

        Log.d(TAG, "start 1");
        lock.lock();
        closeMedia();
        if (segmentDownloader != null) {
            segmentDownloader.stop();
            segmentDownloader = null;
        }

        segmentDownloader = new SegmentDownloader(v , playBufferQueue);
        segmentDownloader.execute();

        numPlayedSeg = 0;
        mediaPlayer = new MediaPlayer(); // Create media player and set listeners
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setScreenOnWhilePlaying(true);

        mediaController = new MediaController(getActivity(), false);
        mediaController.setMediaPlayer(this);
        mediaController.setEnabled(true);
        mediaController.setAnchorView(surfaceView);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mediaController.show();
                return false;
            }
        });

        playSlot.signal();
        lock.unlock();
        Log.d(TAG, "start 2");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (listener != null) {
            listener.onFragmentInteraction(uri);
        }
    }

    public String getText() {
        if(segmentDownloader != null)
            return segmentDownloader.getAdapterInfo();
        return "Quality = unknown, Bandwidth = unknown";
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mediaPlayer != null) {
            mediaPlayer.start();
        }

    }

    @Override
    public void onDestroy() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     * See the Android Training lesson
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void onCompletion(MediaPlayer player) {
        numPlayedSeg += 1;
        Log.d("Player", String.valueOf(numPlayedSeg));

        Log.d(TAG, "Before signal");
        lock.lock();
        playSlot.signal();
        lock.unlock();
        Log.d(TAG, "After signal");
        /*
        if (numPlayedSeg < segmentDownloader.getNumTotalSeg()) {
            handleNextSegment();
        } else {
            mediaController.setEnabled(false);
            mediaPlayer.stop();
            Log.d("Player", "play finished");
        }
        */
        // Update UI’s buffer bar
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return true;
    }

    private void playSegement() {
        while(true) {
            handleNextSegment();
        }
    }

    private void handleNextSegment() {

        String filename = null;
        Log.d(TAG, "Take 1");
        try {
            filename = (String)playBufferQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Take 2");

        lock.lock();
        String filePath = segmentDownloader.getExtStorageDirectory() + filename;
        File file = new File(filePath);
        if (!file.exists()) {
            lock.unlock();
            return;
        }
        setNextDataSource(filePath); // Start segment load procedure
        try {
            Log.d(TAG, "await 1");
            playSlot.await();
            Log.d(TAG, "await 2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
    }

    private void setNextDataSource(String nextFilePath) {
        Log.d(TAG, "Path: " + nextFilePath);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(nextFilePath);
            mediaPlayer.prepareAsync(); // Prepare segment asynchronously

            //Log.d(TAG, "Before Await");
           // lock.lock();
           // playSlot.await();
           // lock.unlock();
           // Log.d(TAG, "After Await");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeMedia() {
        if(mediaPlayer != null) mediaPlayer.release();
        if(segmentDownloader != null) {
            segmentDownloader.cancel(true);
            segmentDownloader.clearBuffer();
        }
    }


    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(segmentDownloader != null)
            return numPlayedSeg * segmentDownloader.getSegmentDuration() + mediaPlayer.getCurrentPosition();
        return 0;
    }

    @Override
    public int getDuration() {
        if(segmentDownloader != null)
            return segmentDownloader.getTotalDuration();
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void pause () {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public void start() {
        super.onStart();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }


    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
