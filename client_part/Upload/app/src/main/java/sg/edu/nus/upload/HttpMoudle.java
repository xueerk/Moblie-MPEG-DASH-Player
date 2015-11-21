package sg.edu.nus.upload;


import android.os.Build;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2015-11-12.
 */
public class HttpMoudle {
    private String TAG = "HTTP";
    private boolean VERBOSE = true;
    private boolean isTesting = false;

    private HttpClient mClient = new DefaultHttpClient();
    private static final String PILATUS = "http://pilatus.d1.comp.nus.edu.sg/~team08/";
    private static final String PLUTO = "http://pluto.comp.nus.edu.sg/dash/";
    private String urlPrefix = null;

    public HttpMoudle() {
        if (isTesting) {
            urlPrefix = PLUTO;
        } else {
            urlPrefix = PILATUS;
        }

    }
    public boolean SendSegment(String path, String name, int sn, int last) {
        String url = urlPrefix + "upload.php";
        HttpPost post = new HttpPost(url);

       // if (VERBOSE) Log.d(TAG, "send segment: " +url );

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("id", name);
        builder.addTextBody("duration", "3000");
        builder.addTextBody("sn", String.valueOf(sn));
        builder.addTextBody("last", String.valueOf(last));

        if (last == 0) {
            File file = new File(path);
            builder.addBinaryBody("userfile", file, ContentType.create("video/mp4"), file.getName());
        }
        post.setEntity(builder.build());
        HttpResponse response;

        String uploadStatus = "";
        try {
            response = mClient.execute(post);
            uploadStatus = getResponseString(response);

           // if (VERBOSE) Log.d(TAG, "httpRespose: " + uploadStatus);

            if (uploadStatus.indexOf("SUCCESS") != -1)
                return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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
