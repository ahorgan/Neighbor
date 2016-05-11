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
public class CreateProfileFragment extends Fragment {
    private static String TAG = "CreateProfileFragment";
    private Context mContext;
    private static Database db;
    private static Map<String, Object> newProfile;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static EditText nameText;
    private static EditText occupationText;
    private static EditText eduText;
    private static EditText birthdateText;
    private static RadioGroup genderGroup;
    private static EditText contextText;
    private static RadioButton genderBtn;
    private static EditText messageText;
    private Map<String, Object> item;
    private View view;
    String id;

    public CreateProfileFragment() {
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
    public static CreateProfileFragment newInstance(String param1, String param2) {
        CreateProfileFragment fragment = new CreateProfileFragment();
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
        view = inflater.inflate(R.layout.fragment_create_profile, container, false);
        nameText = (EditText) view.findViewById(R.id.nameText);
        occupationText = (EditText) view.findViewById(R.id.occupationText);
        eduText = (EditText) view.findViewById(R.id.educationText);
        birthdateText = (EditText) view.findViewById(R.id.birthdateText);
        genderGroup = (RadioGroup) view.findViewById(R.id.genderGroup);
        contextText = (EditText) view.findViewById(R.id.contextText);
        messageText = (EditText) view.findViewById(R.id.messageText);

        if(item != null) {
            nameText.setText((String) item.get(Profile.NAME));
            occupationText.setText((String) item.get(Profile.OCCUPATION));
            eduText.setText((String) item.get(Profile.EDUCATION));
            birthdateText.setText((String) item.get(Profile.BIRTHDATE));
            if(item.containsKey(Profile.GENDER) && item.get(Profile.GENDER).equals("female"))
                genderGroup.check(R.id.femaleBtn);
            else if(item.containsKey(Profile.GENDER) && item.get(Profile.GENDER).equals("male"))
                genderGroup.check(R.id.maleBtn);
            else
                genderGroup.check(R.id.unspecifiedBtn);
            messageText.setText((String) item.get(Profile.MESSAGE));
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
        nameText = (EditText) getActivity().findViewById(R.id.nameText);
        occupationText = (EditText) getActivity().findViewById(R.id.occupationText);
        eduText = (EditText) getActivity().findViewById(R.id.educationText);
        birthdateText = (EditText) getActivity().findViewById(R.id.birthdateText);
        genderGroup = (RadioGroup) getActivity().findViewById(R.id.genderGroup);
        contextText = (EditText) getActivity().findViewById(R.id.contextText);
        messageText = (EditText) getActivity().findViewById(R.id.messageText);
        genderBtn = (RadioButton) getActivity().findViewById(genderGroup.getCheckedRadioButtonId());

        if(item != null && item.containsKey("_id"))
            Log.d(TAG, (String)item.get("_id"));
        else
            Log.d(TAG, "New Profile: ");
        Log.d(TAG, nameText.getText().toString());
        Log.d(TAG, occupationText.getText().toString());
        Log.d(TAG, eduText.getText().toString());
        Log.d(TAG, birthdateText.getText().toString());
        Log.d(TAG, genderBtn.getText().toString());
        Log.d(TAG, contextText.getText().toString());
        Log.d(TAG, messageText.getText().toString());

        Map<String, Object> profile = new HashMap<>();

        profile.put(Profile.NAME, nameText.getText().toString());
        profile.put(Profile.OCCUPATION, occupationText.getText().toString());
        profile.put(Profile.EDUCATION, eduText.getText().toString());
        profile.put(Profile.BIRTHDATE, birthdateText.getText().toString());
        profile.put(Profile.GENDER, genderBtn.getText().toString());
        profile.put(Profile.CONTEXT, contextText.getText().toString());
        profile.put(Profile.MESSAGE, messageText.getText().toString());
        profile.put(Profile.OWNER, "me");
        profile.put(Profile.TYPE, "profile");
        if(item != null)
            id = (String)item.get("_id");
        else
            id = null;
        db.addDocument(id, profile);

        return item;
    }



}
