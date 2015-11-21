package cs5248.assign.player;

import android.app.Activity;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ResourceList extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<VideoInfo> playlist = null;
    private ResourceContent content;
    private OnFragmentInteractionListener mListener;

    ExecutorService executor = Executors.newFixedThreadPool(2);
    HttpTask httpTask = new HttpTask();

    HttpModule http = new HttpModule();

    // TODO: Rename and change types of parameters
    public static ResourceList newInstance(String param1, String param2) {
        ResourceList fragment = new ResourceList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ResourceList() {
    }

    private void doHttp() {
        try {
            if (http.GetPlaylistFromServer()) {
                playlist = http.GetPlayList();
            } else {
                Log.d("HTTP", "Get playlist failed");
            }
        }catch (Exception e) {
            Log.d("HTTP", e.toString());
        }

        if (playlist != null && !playlist.isEmpty()) {
            int i = 0;
            String str_live = "";
            for (VideoInfo vi : playlist) {
                if (vi.isLive()) {
                    str_live = "_LIVE";
                } else {
                    str_live = "";
                }
                content.addItem(content.new ResourceItem("" + i, vi.GetVideoName()+str_live));
                i++;
            }
        }
    }

    private class HttpTask implements  Runnable {
        public void run() {
            doHttp();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executor.execute(httpTask);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        content = new ResourceContent();

        setListAdapter(new ArrayAdapter<ResourceContent.ResourceItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, content.ITEMS));
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //int item_id = content.ITEMS.get(position).id;
          mListener.onFragmentInteraction(playlist.get((int)id));

            //Log.d("RL", "click: pos: " + position);
           // Log.d("RL", "click: id: " + id);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     * See the Android Training lesson
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragmentsfor more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(VideoInfo vi);
    }

    public class ResourceContent {

        // List of resources
        public List<ResourceItem> ITEMS = new ArrayList<ResourceItem>();

        // A map of resource items, by ID
        public Map<String, ResourceItem> ITEM_MAP = new HashMap<String, ResourceItem>();

        public ResourceContent() {
            // Add 3 sample items.
           // addItem(new ResourceItem("1", "Item 1"));
          //  addItem(new ResourceItem("2", "Item 2"));
          //  addItem(new ResourceItem("3", "Item 3"));
        }

        private void addItem(ResourceItem item) {
            ITEMS.add(item);
            ITEM_MAP.put(item.id, item);
        }

        // A resource item representing a piece of content.
        public class ResourceItem {
            public String id;
            public String content;

            public ResourceItem(String id, String content) {
                this.id = id;
                this.content = content;
            }

            @Override
            public String toString() {
                return content;
            }
        }
    }

}
