package cs5248.assign.player;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * Created by Cong on 2015/11/11.
 */
public class SegmentDownloader extends AsyncTask<Void, Void, Void> {

    private String TAG = "@@@@";
    private final int MAX_ATTEMPTS = 30;
    private final int TIMEOUT = 3000;
    private final int BYTE_BLOCK = 8192;
    private final int MAX_ALLOWED_TIME = 10000;
    private final int SEGMENT_DURATION = 3000;
    private final int SLEEP_TIME_MILLIS = 2000;

    private int attempt;
    private int totalBytes;
    private int count;
    private byte[] data;
    private HttpURLConnection connection;
    private BufferedInputStream bis;
    private FileOutputStream fos;
    private RateAdapter adapter;
    private DashParser dashParser;
    private  long startFlowTime;
    private long endFlowTime;
    private long lengthFlowTime;

    private int availBW;
    private int totalDuration;

    private BlockingQueue playBuffer;

    private int numDownloadSeg;
    private int numTotalSeg;
    private String extStorageDirectory;

    private volatile boolean isRunning = false;
    private VideoInfo videoInfo;
    private boolean isLive;

    public SegmentDownloader(VideoInfo v, BlockingQueue pb) {

        data = new byte[BYTE_BLOCK];
        // create temporal directory to save segments
        File folder = new File(Environment.getExternalStorageDirectory().toString()+"/temp/DASH/");

        if(folder.exists() && folder.isDirectory()) {
            String[] children = folder.list();
           for (int i = 0; i < children.length; i++) {
                new File(folder, children[i]).delete();
            }
        } else {
            folder.mkdirs();
        }
        extStorageDirectory = folder.toString();

        // init buffer
        numDownloadSeg = 0;
        playBuffer = pb;
        adapter = null;
        dashParser = new DashParser();
        videoInfo = v;
        isLive = videoInfo.isLive();
    }

    public String getAdapterInfo () {
        if(adapter != null)
            return adapter.getAdapterInfo();
        return "Quality = unknown, Bandwidth = unknown";
    }

    public void clearBuffer() { playBuffer.clear(); }

    public int getNumTotalSeg() { return numTotalSeg; }

    public int getTotalDuration() { return totalDuration; }

    public int getSegmentDuration() { return SEGMENT_DURATION; }

    public String getExtStorageDirectory() { return extStorageDirectory; }

    public void stop() {
        if (!isRunning) {
            //Log.d(TAG, "This thread already stopped!");
            return;
        }
        isRunning = false;
        playBuffer.clear();
    }


    @Override
    protected Void doInBackground(Void... paras) {
        // wait until rate adapter is created
        isRunning = true;
        Log.d(TAG, "Start SegmentDownloader!");
        dashParser.doParse(videoInfo.GetVideoUrl());
        adapter.setStart(true);

        while (numDownloadSeg < numTotalSeg || isLive) {
            String urlStr = adapter.getNextSegment();
            if (urlStr != null) {
              //  Log.d(TAG, "DownLoading: " + urlStr);
                try {
                    URL url = new URL(urlStr);
                    downloadSegment(url); // Fetch next segment
                    if (!isRunning) {
                      //  Log.d(TAG, "isRuning: false");
                        break;
                    }
                } catch (IOException e) {

                } finally {
                    adapter.adapt(); // Perform adaptation in the rate-adaptation module
                }
            }
        }

        isRunning = false;
        //Log.d(TAG, "End doInBackground!");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        return;
    }

    private void downloadSegment(URL url) throws IOException {
        String filename = extStorageDirectory + getFileName(url.toString());
        Log.d("URL",url.toString());
        if (!new File(filename).exists()) {
            attempt = 0;
            while (attempt < MAX_ATTEMPTS) {
                attempt++;
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(TIMEOUT);
                connection.setDoInput(true);
                connection.connect();

                int responseCode = connection.getResponseCode(); // Obtain HTTP response code
                Log.d("RESCODE", String.valueOf(responseCode));
                if (responseCode == HttpURLConnection.HTTP_OK)
                    break;
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {

                    try {
                        Log.d("@@@@", "attempt time " + String.valueOf(attempt));
                        Thread.sleep(SLEEP_TIME_MILLIS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                connection.disconnect();

            }
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return; // Exit if HTTP response code is not HTTP-200

            // start timer
            totalBytes = 0;
            lengthFlowTime = 0;

            // TODO: Read the Content-Length header field
            startFlowTime = System.currentTimeMillis();
            connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            endFlowTime = System.currentTimeMillis();
            lengthFlowTime += endFlowTime - startFlowTime;


            bis = new BufferedInputStream(is, BYTE_BLOCK);
            fos = new FileOutputStream(filename);

            while ((count = bis.read(data, 0, data.length)) != -1) {
                // TODO: Stop downloading if player is closed
                fos.write(data, 0, count);
                totalBytes += count;

            }

            // Close streams
            bis.close();
            fos.close();
            try {
                Log.d(TAG,"PUT 1 ");
                Log.d("TAG", "PUTNAME: " + filename);
                playBuffer.put(getFileName(filename));
                Log.d(TAG, "PUT 2 size: " + playBuffer.size());
                numDownloadSeg += 1;
            } catch (InterruptedException e) {
                Log.d(TAG, "PlayBuffer exp: " + e.toString());
            }

            //Log.d("Downloader", filename + " downloaded");

            try {
                Log.d("availBW", String.valueOf(totalBytes)+","+String.valueOf(lengthFlowTime));
                availBW = (int) ((totalBytes*8) / lengthFlowTime);
                adapter.setAvailBandwidth(availBW);

            } catch (ArithmeticException e) {
                Log.d(TAG, "adapter exp: " + e.toString());
            }

        }
      //  Log.d(TAG, "Download function ends!");
    }

    private String getFileName(String url) {
        return url.substring(url.lastIndexOf('/'));
    }

    private class DashParser {

        private final int MAX_BUF_SIZE = 1024;

        private SAXParserFactory factory;
        private SAXParser saxParser;
        private XMLReader xmlReader;
        private UserXMLHandler userXMLHandler;

        private URL parsingUrl;

        public DashParser() {

        }

        public SegmentLists getSegmentLists() {
            if(userXMLHandler != null) {
                return userXMLHandler.getSegLists();
            }
            return null;
        }

        public void doParse(String url) {

            try {
                parsingUrl = new URL(url);
                factory = SAXParserFactory.newInstance();
                saxParser = factory.newSAXParser();
                xmlReader = saxParser.getXMLReader();
                userXMLHandler = new UserXMLHandler();
                xmlReader.setContentHandler(userXMLHandler);
                xmlReader.parse(new InputSource(parsingUrl.openStream()));
            } catch (Exception e) {
                Log.d(TAG, "Error: doInBackground. " + e.getMessage());
            }

            SegmentLists sLists = dashParser.getSegmentLists();
            sLists.print();
            numTotalSeg = sLists.getSegmentListSize();
            totalDuration = SEGMENT_DURATION * sLists.getSegmentListSize();
            adapter = new RateAdapter(sLists, videoInfo.isLive());
            Log.d("Adapter","created!");
        }


        private class UserXMLHandler extends DefaultHandler {
            // Override  methods: startDocument(), endDocument() and endElement()
            private String tmpId;
            private SegmentLists lists;
            private String tmpBaseUrl;
            private boolean isInBaseUrl;

            public UserXMLHandler() {
                tmpId = "LOW";
                tmpBaseUrl = "";
                lists = new SegmentLists();
            }

            public SegmentLists getSegLists() {
                Log.d("SegLists", "BaseUrl:"+lists.getBaseUrl());
                Log.d("SegLists", "ListSize:"+String.valueOf(lists.getSegmentListSize()));
                return lists;
            }

            @Override
            public void startElement(String uri, String localName, String qName,
                                     Attributes attrs) throws SAXException {
                if(localName.equals("BaseURL")) {
                    isInBaseUrl = true;
                } else {
                    isInBaseUrl = false;
                }
                if(localName.equals("Representation")) {
                    tmpId = attrs.getValue("id");
                    if(tmpId.equals("LOW"))
                        lists.setBandwidthLo(Integer.parseInt(attrs.getValue("bandwidth")));
                    else if(tmpId.equals("MEDIUM"))
                        lists.setBandwidthMe(Integer.parseInt(attrs.getValue("bandwidth")));
                    else if(tmpId.equals("HIGH"))
                        lists.setBandwidthHi(Integer.parseInt(attrs.getValue("bandwidth")));
                }
                else if (localName.equals("SegmentURL")) {
                    if(tmpId.equals("LOW"))
                        lists.addToSegmentListLo(attrs.getValue("media"));
                    else if(tmpId.equals("MEDIUM"))
                        lists.addToSegmentListMe(attrs.getValue("media"));
                    else if(tmpId.equals("HIGH"))
                        lists.addToSegmentListHi(attrs.getValue("media"));
                }
            }

            @Override
            public void characters (char ch[], int start, int length) {
                if(isInBaseUrl) {
                    for (int i = start; i < start + length; i++) {
                        if (ch[i] != '\0' && ch[i] != '\n' && ch[i] != ' ') {
                            tmpBaseUrl += ch[i];
                        }
                    }
                }
            }

            @Override
            public void endDocument() {
                lists.setBaseUrl(tmpBaseUrl);
            }
        }
    }

}
