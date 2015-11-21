package sg.edu.nus.upload;

import android.util.Log;

import java.io.File;

/**
 * Created by Administrator on 2015-11-12.
 */
public class Segment {
    private String filename = null;
    private String path = null;
    private int sn = 0;
    private boolean isLast = false;

    public Segment(String f, String p, int s, boolean l) {
        filename = f;
        path = p;
        sn = s;
        isLast = l;
    }

    public String GetFilename() {
        return filename;
    }

    public String GetPath() {
        return path;
    }

    public int GetSn() {
        return sn;
    }

    public boolean isLast() {
        return isLast;
    }
}
