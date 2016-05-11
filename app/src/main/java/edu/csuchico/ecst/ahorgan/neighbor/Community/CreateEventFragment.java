package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Database;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Event;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Profile;
import edu.csuchico.ecst.ahorgan.neighbor.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateEventFragment extends Fragment {
    private static String TAG = "CreateProfileFragment";
    private Context mContext;
    private static Database db;
    private static Map<String, Object> newProfile;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static EditText nameText;
    private static EditText locationText;
    private static EditText dateText;
    private static EditText timeText;
    private static EditText detailsText;
    private Map<String, Object> item;
    private View view;
    String id;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateEventFragment newInstance(String param1, String param2) {
        CreateEventFragment fragment = new CreateEventFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            item = (HashMap)getArguments().getSerializable("ITEM_MAP");
        db = Database.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_event, container, false);
        nameText = (EditText) view.findViewById(R.id.eventNameText);
        locationText = (EditText) view.findViewById(R.id.eventLocationText);
        dateText = (EditText) view.findViewById(R.id.eventDateText);
        timeText = (EditText) view.findViewById(R.id.eventTimeText);
        detailsText = (EditText) view.findViewById(R.id.eventDetailsText);

        if(item != null) {
            nameText.setText((String) item.get(Event.NAME));
            locationText.setText((String) item.get(Event.LOCATION));
            String datetime = (String) item.get(Event.DATETIME);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
            try {
                Date date = dateFormat.parse(datetime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                String date_val = cal.get(Calendar.MONTH) + "/" +
                        cal.get(Calendar.DAY_OF_MONTH) + "/" +
                        cal.get(Calendar.YEAR);
                String time_val = cal.get(Calendar.HOUR) + ":" +
                        cal.get(Calendar.MINUTE);
                dateText.setText(date_val);
                timeText.setText(time_val);
            }
            catch(ParseException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            detailsText.setText((String) item.get(Event.DETAILS));
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            mListener = new OnFragmentInteractionListener() {
                @Override
                public void onFragmentInteraction(Uri uri) {
                    Log.d(TAG, "onFragemntInteraction");
                }
            };
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public Map<String, Object> updateProfile() {
        Log.d(TAG, "updateProfile()");
        nameText = (EditText) getActivity().findViewById(R.id.eventNameText);
        locationText = (EditText) getActivity().findViewById(R.id.eventLocationText);
        dateText = (EditText) getActivity().findViewById(R.id.eventDateText);
        timeText = (EditText) getActivity().findViewById(R.id.eventTimeText);
        detailsText = (EditText) getActivity().findViewById(R.id.eventDetailsText);

        if(item != null && item.containsKey("_id"))
            Log.d(TAG, (String)item.get("_id"));
        else
            Log.d(TAG, "New Event: ");
        Log.d(TAG, nameText.getText().toString());
        Log.d(TAG, locationText.getText().toString());
        Log.d(TAG, dateText.getText().toString());
        Log.d(TAG, timeText.getText().toString());
        Log.d(TAG, detailsText.getText().toString());

        Map<String, Object> event = new HashMap<>();

        event.put(Event.NAME, nameText.getText().toString());
        event.put(Event.LOCATION, locationText.getText().toString());
        event.put(Event.DATETIME, timeText.getText().toString() + " " +
            dateText.getText().toString());
        event.put(Event.DETAILS, detailsText.getText().toString());
        event.put(Event.OWNERPROFILE, "me");
        event.put(Event.TYPE, "event");
        event.put(Event.BCAST, true);
        if(item != null)
            id = (String)item.get("_id");
        else
            id = null;
        db.addDocument(id, event);

        return item;
    }



}
