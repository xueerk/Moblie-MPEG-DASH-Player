package cs5248.assign.player;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cong on 2015/11/11.
 */
public class RateAdapter {

    private final double SENSITIVITY = 0.8;

    private int measuredThroughput;
    private int currentIdx;
    private int segmentListSize;
    private boolean isStart;

    // record current quality
    private String quality;
    private int prevBW;
    private int currentBW;
    private int nextBW;

    private boolean isLive;
    private int liveIdx;

    private SegmentLists segmentLists;

    public RateAdapter(SegmentLists sLists, boolean bool) {
        segmentLists = sLists;
        isStart = false;
        isLive = bool;
        segmentListSize = segmentLists.getSegmentListSize();
        liveIdx = segmentListSize;
        measuredThroughput = 0;
        quality = "";
    }

    public void setStart(boolean b) {
        if(b) adapt();
        isStart = b;
    }

    public String getAdapterInfo() {
        return "Quality = " + quality + ", Bandwidth = " + String.valueOf(measuredThroughput)+"kbps";
    }

    public void adapt() {
        if (!isStart) { // when start download
            switchMinDown();
            isStart = true;
        } else {
            int throughput = (int) (measuredThroughput * SENSITIVITY);
            if (throughput > currentBW) {
                while (nextBW < throughput && !quality.equals("HIGH"))
                    switchUp(); // switch up for one level
            } else {
                do {
                    switchDown(); // switch down for one level
                } while (prevBW > throughput && !quality.equals("LOW"));
            }
        }
    }

    public String getNextSegment() {
        String nextSegment = null;
        if(currentIdx < segmentListSize) {
            nextSegment = segmentLists.getBaseUrl();
            if (quality.equals("LOW")) {
                nextSegment += segmentLists.getFromSegmentListLo(currentIdx);
            } else if (quality.equals("MEDIUM")) {
                nextSegment += segmentLists.getFromSegmentListMe(currentIdx);
            } else if (quality.equals("HIGH")) {
                nextSegment += segmentLists.getFromSegmentListHi(currentIdx);
            }
            currentIdx += 1;
        }
        if(isLive && nextSegment == null) {
            nextSegment = segmentLists.getBaseUrl();
            nextSegment += String.valueOf(liveIdx);
            if(quality.equals("LOW")) nextSegment += "_240x160.mp4";
            else if(quality.equals("MEDIUM")) nextSegment += "_480x320.mp4";
            else nextSegment += "_720x480.mp4";
            liveIdx += 1;
            Log.d("Adapter", String.valueOf(liveIdx));
        }
        return nextSegment;
    }

    private void switchUp() {
        if(quality.equals("LOW")) {
            quality = "MEDIUM";
            prevBW = segmentLists.getBandwidthLo();
            currentBW = segmentLists.getBandwidthMe();
            nextBW = segmentLists.getBandwidthHi();
        }
        else if(quality.equals("MEDIUM")) {
            quality = "HIGH";
            prevBW = segmentLists.getBandwidthMe();
            currentBW = segmentLists.getBandwidthHi();
            nextBW = -1;
        }
    }

    private void switchDown() {
        if(quality.equals("HIGH")) {
            quality = "MEDIUM";
            prevBW = segmentLists.getBandwidthLo();
            currentBW = segmentLists.getBandwidthMe();
            nextBW = segmentLists.getBandwidthHi();
        }
        else if(quality.equals("MEDIUM")) {
            quality = "LOW";
            prevBW = -1;
            currentBW = segmentLists.getBandwidthLo();
            nextBW = segmentLists.getBandwidthMe();
        }
    }

    private void switchMinDown() {
        quality = "LOW";
        prevBW = -1;
        currentBW = segmentLists.getBandwidthLo();
        nextBW = segmentLists.getBandwidthMe();
    }

    public void setAvailBandwidth(int availBW) {
        measuredThroughput = availBW;
    }

}
