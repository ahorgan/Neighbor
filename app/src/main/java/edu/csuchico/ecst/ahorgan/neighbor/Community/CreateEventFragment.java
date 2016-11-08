package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Database;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Event;
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
    private static String TAG = "CreateEventFragment";
    private Context mContext;
    private static Database db;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static EditText nameText;
    private static EditText locationText;
    private static EditText startDateText;
    private static EditText startTimeText;
    private static EditText endDateText;
    private static EditText endTimeText;
    private static EditText detailsText;
    private Map<String, Object> item;
    private View view;
    String id;

    public CreateEventFragment() {
        // Required empty public constructor
    }


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
        startDateText = (EditText) view.findViewById(R.id.eventStartDateText);
        endDateText = (EditText) view.findViewById(R.id.eventEndDateText);
        startTimeText = (EditText) view.findViewById(R.id.eventStartTimeText);
        endTimeText = (EditText) view.findViewById(R.id.eventEndTimeText);
        detailsText = (EditText) view.findViewById(R.id.eventDetailsText);

        if(item != null) {
            nameText.setText((String) item.get(Event.TITLE));
            locationText.setText((String) item.get(Event.LOCATION));
            String datetime = (String) item.get(Event.STARTDATETIME);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            try {
                Date date = dateFormat.parse(datetime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                String date_val = cal.get(Calendar.YEAR) + "/" +
                        (cal.get(Calendar.MONTH)+1) + "/" +
                        cal.get(Calendar.DAY_OF_MONTH);
                String time_val = cal.get(Calendar.HOUR) + ":" +
                        cal.get(Calendar.MINUTE);
                startDateText.setText(date_val);
                startTimeText.setText(time_val);

                datetime = (String) item.get(Event.ENDDATETIME);
                date = dateFormat.parse(datetime);
                cal.setTime(date);
                date_val = cal.get(Calendar.YEAR) + "/" +
                        (cal.get(Calendar.MONTH)+1) + "/" +
                        cal.get(Calendar.DAY_OF_MONTH);
                time_val = cal.get(Calendar.HOUR) + ":" +
                        cal.get(Calendar.MINUTE);
                endDateText.setText(date_val);
                endTimeText.setText(time_val);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public Map<String, Object> updateEvent() {
        Log.d(TAG, "updateEvent()");
        nameText = (EditText) getActivity().findViewById(R.id.eventNameText);
        locationText = (EditText) getActivity().findViewById(R.id.eventLocationText);
        startDateText = (EditText) getActivity().findViewById(R.id.eventStartDateText);
        startTimeText = (EditText) getActivity().findViewById(R.id.eventStartTimeText);
        endDateText = (EditText) getActivity().findViewById(R.id.eventStartDateText);
        endTimeText = (EditText) getActivity().findViewById(R.id.eventStartTimeText);
        detailsText = (EditText) getActivity().findViewById(R.id.eventDetailsText);

        if(item != null && item.containsKey("_id"))
            Log.d(TAG, (String)item.get("_id"));
        else
            Log.d(TAG, "New Event: ");
        Log.d(TAG, nameText.getText().toString());
        Log.d(TAG, locationText.getText().toString());
        Log.d(TAG, startDateText.getText().toString());
        Log.d(TAG, startTimeText.getText().toString());
        Log.d(TAG, endDateText.getText().toString());
        Log.d(TAG, endTimeText.getText().toString());
        Log.d(TAG, detailsText.getText().toString());

        Map<String, Object> event = new HashMap<>();

        event.put(Event.TITLE, nameText.getText().toString());
        event.put(Event.LOCATION, locationText.getText().toString());
        Calendar cal = Calendar.getInstance();
        if(startDateText.getText().length() != 0) {
            SimpleDateFormat dateFormat;
            if(startTimeText.getText().length() == 0) {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                try {
                    cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateFormat.parse(startDateText.getText().toString()).getTime());
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                }
                catch(ParseException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
            else {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                try {
                    cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateFormat.parse(startDateText.getText().toString() +
                            startTimeText.getText().toString()).getTime());
                } catch (ParseException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }

        event.put(Event.STARTDATETIME, String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM", cal));
        cal = Calendar.getInstance();
        if(endDateText.getText().length() != 0) {
            SimpleDateFormat dateFormat;
            if(endTimeText.getText().length() == 0) {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                try {
                    cal.setTimeInMillis(dateFormat.parse(endDateText.getText().toString()).getTime());
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                } catch (ParseException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
            else {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                try {
                    cal.setTimeInMillis(dateFormat.parse(endDateText.getText().toString() +
                            endTimeText.getText().toString()).getTime());
                } catch (ParseException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        else {
            cal.add(Calendar.YEAR, 10);
            if(endTimeText.getText().length() != 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                try {
                    cal.setTime(dateFormat.parse(endTimeText.getText().toString()));
                } catch (ParseException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        event.put(Event.ENDDATETIME, String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM", cal));
        event.put(Event.DETAILS, detailsText.getText().toString());
        event.put(Event.OWNER, "me");
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
