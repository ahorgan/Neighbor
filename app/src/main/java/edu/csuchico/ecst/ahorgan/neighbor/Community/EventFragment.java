package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class EventFragment extends Fragment {
    public final static String EVENT_TYPE = "event_type";
    public final static int ALL_EVENTS = 0;
    public final static int BROADCASTED_EVENTS = 1;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static EventFragment newInstance(int columnCount) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            ArrayList<Map<String, Object>> dummyholder = new ArrayList<>();
            /*
                Because the recycler uses the database LiveQuery to populate list,
                arraylist parameter is redundant.
                Use it to specify which database view to populate recycler
             */
            if(savedInstanceState != null && savedInstanceState.get(EVENT_TYPE) != null) {
                Map<String, Object> dummymap = new HashMap<>();
                dummymap.put(EVENT_TYPE, savedInstanceState.get(EVENT_TYPE));
                dummyholder.add(dummymap);
            }
            recyclerView.setAdapter(new EventRecyclerViewAdapter(dummyholder, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            mListener = new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(Map item, int action) {
                    if(action == EventRecyclerViewAdapter.ACTION_TOGGLE) {
                        ((MainActivity)getActivity()).onEventSelected(item);
                    }
                    else if(action == EventRecyclerViewAdapter.ACTION_PROFILE) {
                        Map item_info = new HashMap();
                        item_info.put("_id", item.get("_id").toString());
                        ((MainActivity)getActivity()).onProfileSelected(item_info);
                    }
                }
            };
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Map<String, Object> item, int action);
    }
}
