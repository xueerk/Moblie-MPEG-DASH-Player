package cs5248.assign.player;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements
        ResourceList.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener {

    private String TAG = "Main";
    private SegmentDownloader videoLoader;
    private PlayerFragment player;

    private Timer timer_update = new Timer();
    private TextView text_bandwidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        player = (PlayerFragment)getSupportFragmentManager().findFragmentById(R.id.player_fragment);

        text_bandwidth = (TextView)findViewById(R.id.text_net);

        timer_update.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() //run on ui thread
                {
                    public void run()
                    {
                       text_bandwidth.setText(player.getText());
                    }
                });
            }
        }, 500, 500);
    }

    @Override
    // resource list
    public void onFragmentInteraction(VideoInfo vi) {
        //Log.d(TAG, "OnClick video name: " + vi.GetVideoName());
       // Log.d(TAG, "OnClick video url: "  + vi.GetVideoUrl());
       // Log.d(TAG, "OnClick video name: "  + String.valueOf(vi.IsLive()));
        player.startPlay(vi);
    }

    @Override
    // player fragment
    public void onFragmentInteraction(Uri uri) {

    }

}
