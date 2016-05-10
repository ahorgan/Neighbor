package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.csuchico.ecst.ahorgan.neighbor.Community.ProfileFragment.OnListFragmentInteractionListener;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Profile;
import edu.csuchico.ecst.ahorgan.neighbor.Community.items.DbItemContent;
import edu.csuchico.ecst.ahorgan.neighbor.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyProfileRecyclerViewAdapter extends RecyclerView.Adapter<MyProfileRecyclerViewAdapter.ViewHolder> {
    private final String TAG = "MyProfileRecyclerVA";
    private final List<Map<String, Object>> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyProfileRecyclerViewAdapter(List<Map<String, Object>> items, OnListFragmentInteractionListener listener) {
        Log.d(TAG, "MyProfileRecyclerViewAdapter()");
        Log.d(TAG, "Items count: " + items.size());
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        Log.d(TAG, getItemCount() + " items");
        holder.mIdView.setText((String)mValues.get(position).get(Profile.CONTEXT));
        Map<String, Object> values = mValues.get(position);
        for ( Map.Entry entry : values.entrySet() ) {
            Log.d(TAG, entry.getKey() + " : " + entry.getValue());
        }
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date birthDate = new Date();
        try {
            birthDate = dateFormat.parse((String)mValues.get(position).get(Profile.BIRTHDATE));
        }
        catch(ParseException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if(today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
                (
                        today.get(Calendar.YEAR) == birth.get(Calendar.MONTH) &&
                                today.get(Calendar.DAY_OF_MONTH) < today.get(Calendar.DAY_OF_MONTH)
                ))
            age--;
        holder.mContentView.setText(values.get(Profile.NAME) + "\t" +
                values.get(Profile.OCCUPATION) + "\t" +
                values.get(Profile.EDUCATION) + "\t" +
                String.valueOf(age) + "\n" +
                values.get(Profile.MESSAGE));

        holder.mItem = values;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Map<String, Object> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }
    }
}
