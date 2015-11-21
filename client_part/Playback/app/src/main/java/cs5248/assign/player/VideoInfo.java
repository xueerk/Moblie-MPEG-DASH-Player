package cs5248.assign.player;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015-11-14.
 */
public class VideoInfo {

    private final int READYSIZE = 5;

    private int totalIndex;
    private int currentIdx;

    private String videoName;
    private String videoUrl;
    private boolean isLive;

    public VideoInfo(String name, String url, boolean bool) {
        videoName = name;
        videoUrl  = url;
        isLive = bool;
        Log.d("HTTP", name + " isLive: " + String.valueOf(isLive));
    }

    public String GetVideoName() {
        return videoName;
    }

    public String GetVideoUrl() {
        return videoUrl;
    }

    public boolean isLive() { return isLive; }

}
