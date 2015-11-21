package cs5248.assign.player;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cong on 2015/11/15.
 */
public class SegmentLists {

    private String baseUrl;
    private int bandwidthLo;
    private int bandwidthMe;
    private int bandwidthHi;
    private List<String> segmentListLo;
    private List<String> segmentListMe;
    private List<String> segmentListHi;

    public SegmentLists() {
        segmentListLo = new ArrayList<>();
        segmentListMe = new ArrayList<>();
        segmentListHi = new ArrayList<>();
    }

    public int getSegmentListSize() { return segmentListLo.size(); }

    public void print() {
        Log.d("SegmentList", String.valueOf(bandwidthHi));
        Log.d("SegmentList", segmentListLo.toString());
        Log.d("SegmentList", segmentListMe.toString());
        Log.d("SegmentList", segmentListHi.toString());
    }

    public String getFromSegmentListLo(int idx) { return segmentListLo.get(idx); }
    public String getFromSegmentListMe(int idx) { return segmentListMe.get(idx); }
    public String getFromSegmentListHi(int idx) { return segmentListHi.get(idx); }

    public void addToSegmentListLo(String url) { segmentListLo.add(url); }
    public void addToSegmentListMe(String url) { segmentListMe.add(url); }
    public void addToSegmentListHi(String url) { segmentListHi.add(url); }

    public void setBandwidthLo(int bw) { bandwidthLo = bw/1000; }
    public void setBandwidthMe(int bw) { bandwidthMe = bw/1000; }
    public void setBandwidthHi(int bw) { bandwidthHi = bw/1000; }
    public void setBaseUrl(String str) { baseUrl = str; }
    public int getBandwidthLo() { return bandwidthLo; }
    public int getBandwidthMe() { return bandwidthMe; }
    public int getBandwidthHi() { return bandwidthHi; }
    public String getBaseUrl() { return baseUrl; }

}
