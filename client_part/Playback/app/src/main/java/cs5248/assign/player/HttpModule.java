package cs5248.assign.player;

/**
 * Created by Administrator on 2015-11-14.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import android.os.Build;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015-11-12.
 */
public class HttpModule {
    private String TAG = "HTTP";
    private boolean VERBOSE = true;
    private boolean isTesting = false;

    private HttpClient mClient = new DefaultHttpClient();
    private static final String PILATUS = "http://pilatus.d1.comp.nus.edu.sg/~team08/";
    private static final String PLUTO = "http://pluto.comp.nus.edu.sg/dash/";
    private String urlPrefix = null;
    private List<VideoInfo> mVideoList = new ArrayList<VideoInfo>();

    public HttpModule() {
        if (isTesting) {
            urlPrefix = PLUTO;
        } else {
            urlPrefix = PILATUS;
        }

    }
    public boolean GetPlaylistFromServer() {
        String url = urlPrefix + "retrieve.php";
        HttpPost post = new HttpPost(url);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("id", "GET_LIST");
        post.setEntity(builder.build());

        HttpResponse response = null;
        try {
            response  = mClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String responseStr = getResponseString(response);

        if (PaserPlaylist(responseStr)) {
            return true;
        }
        return false;
    }

    private boolean PaserPlaylist(String resp) {

        if (VERBOSE) Log.d(TAG, resp);

        if (resp.contains("####") && resp.contains("@@@@")) {
            int start = resp.indexOf("####");
            int end = resp.indexOf("@@@@");

            String subStr = resp.substring(start + 4, end);
            if (VERBOSE) Log.d(TAG, "start =" + start + " end = " + end);
            if (VERBOSE) Log.d(TAG, subStr);

            while(!subStr.isEmpty()) {
                int at = subStr.indexOf('@');
                int hash = subStr.indexOf('#');

                String name = subStr.substring(0, at);
                String live = subStr.substring(at+1, hash);
                String url = "";
                boolean isLive = false;

                if (live.equals("")) {
                    isLive = false;
                } else if (live.equals("Live")) {
                    isLive = true;
                }

                url = urlPrefix + "video_repo/" + name +"/" + name + ".mpd";

                if (VERBOSE) Log.d(TAG, "name: " + name);
                if (VERBOSE) Log.d(TAG, "url: " + url);
                if (VERBOSE) Log.d(TAG, "live: " + live);
                VideoInfo vi = new VideoInfo(name, url, isLive);
                mVideoList.add(vi);
                subStr = subStr.substring(hash+1);
                if (VERBOSE) Log.d(TAG, subStr);
            }
            if (VERBOSE) Log.d(TAG, "Size: " + mVideoList.size());
            return true;
        }
        return false;
    }

    public List<VideoInfo> GetPlayList() {
        return mVideoList;
    }
    private String getResponseString(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        InputStream is;
        StringBuilder sb = new StringBuilder();
        try {
            is = entity.getContent();

            //convert response to string
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sb.toString();
    }
}
