package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Profile;
import edu.csuchico.ecst.ahorgan.neighbor.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewProfileFragment extends Fragment {
    private static final String TAG = "ViewProfileFragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ITEM_MAP = "ITEM_MAP";

    private TextView nameView;
    private TextView contextView;
    private TextView educationView;
    private TextView occupationView;
    private TextView ageView;
    private TextView messageView;

    private Map<String, Object> item;

    private OnFragmentInteractionListener mListener;

    public ViewProfileFragment() {
        // Required empty public constructor
    }

    public static ViewProfileFragment newInstance(HashMap<String, Object> item) {
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(ITEM_MAP, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            this.item = (HashMap) getArguments().getSerializable("ITEM_MAP");
            for(Map.Entry entry : this.item.entrySet()) {
                Log.d(TAG, entry.getKey() + " " + entry.getValue());
            }
        }
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
        nameView = (TextView) view.findViewById(R.id.ProfileNameText);
        contextView = (TextView) view.findViewById(R.id.ProfileContextText);
        educationView = (TextView) view.findViewById(R.id.ProfileEducationText);
        occupationView = (TextView) view.findViewById(R.id.ProfileOccupationText);
        ageView = (TextView) view.findViewById(R.id.ProfileAgeText);
        messageView = (TextView) view.findViewById(R.id.ProfileMessageText);
        if(this.item != null) {
            Log.d(TAG, "item is not null");

            if(item.containsKey(Profile.NAME) && nameView != null)
                nameView.setText((String) item.get(Profile.NAME));
            if(item.containsKey(Profile.CONTEXT) && contextView != null)
                contextView.setText((String) item.get(Profile.CONTEXT));
            if(item.containsKey(Profile.EDUCATION) && educationView != null)
                educationView.setText((String) item.get(Profile.EDUCATION));
            if(item.containsKey(Profile.OCCUPATION) && occupationView != null)
                occupationView.setText((String) item.get(Profile.OCCUPATION));

            Calendar today = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date birthDate = new Date();
            try {
                if(item.containsKey(Profile.BIRTHDATE) && ageView != null) {
                    birthDate = dateFormat.parse((String) item.get(Profile.BIRTHDATE));
                    Calendar birth = Calendar.getInstance();
                    birth.setTime(birthDate);

                    int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                    if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
                            (
                                    today.get(Calendar.YEAR) == birth.get(Calendar.MONTH) &&
                                            today.get(Calendar.DAY_OF_MONTH) < today.get(Calendar.DAY_OF_MONTH)
                            ))
                        age--;
                    ageView.setText(String.valueOf(age));
                }
            } catch (ParseException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            if(item.containsKey(Profile.MESSAGE) && messageView != null)
                messageView.setText((String) item.get(Profile.MESSAGE));
        }
        else {
            Log.d(TAG, "Item is null");
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
    public void setArguments(Bundle args) {
        super.setArguments(args);
        item = (HashMap)args.getSerializable(ITEM_MAP);
        for(Map.Entry entry : item.entrySet()) {
            Log.d(TAG, entry.getKey() + " " + entry.getValue());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            mListener = new OnFragmentInteractionListener() {
                @Override
                public void onFragmentInteraction(Uri uri) {
                    Log.d(TAG, "onFragmentIntreaction");
                }
            };
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public Map getItem() {
        return this.item;
    }
}
