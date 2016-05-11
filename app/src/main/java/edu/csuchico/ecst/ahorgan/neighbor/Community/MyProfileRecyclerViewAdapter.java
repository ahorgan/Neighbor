package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import org.w3c.dom.Text;

import edu.csuchico.ecst.ahorgan.neighbor.Community.ProfileFragment.OnListFragmentInteractionListener;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Database;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Profile;
import edu.csuchico.ecst.ahorgan.neighbor.Community.items.DbItemContent;
import edu.csuchico.ecst.ahorgan.neighbor.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
    private LiveQuery mQuery;

    public MyProfileRecyclerViewAdapter(List<Map<String, Object>> items, OnListFragmentInteractionListener listener) {
        Log.d(TAG, "MyProfileRecyclerViewAdapter()");
        Database db = Database.getInstance(null);
        mQuery = db.getProfilesbyownerView().createQuery().toLiveQuery();
        ArrayList<Object> keys = new ArrayList<>();
        keys.add("me");
        mQuery.setKeys(keys);

        mValues = items;
        updateValues();
        mListener = listener;

        mQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                updateValues();
            }
        });
        mQuery.start();
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
        try {
            values.putAll(Database.getDatabaseInstance().
                    getDocument((String)values.get("_id"))
                    .getProperties());
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        for ( Map.Entry entry : values.entrySet() ) {
            Log.d(TAG, entry.getKey() + " : " + entry.getValue());
        }
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date birthDate = new Date();
        try {
            birthDate = dateFormat.parse((String)values.get(Profile.BIRTHDATE));
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

        holder.mNameView.setText((String)values.get(Profile.NAME));
        holder.mOccView.setText((String)values.get(Profile.OCCUPATION));
        holder.mEduView.setText((String)values.get(Profile.EDUCATION));
        holder.mAgeView.setText(String.valueOf(age));
        holder.mMessageView.setText((String)values.get(Profile.MESSAGE));

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
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mQuery.stop();
    }

    public void updateValues() {
        Log.d(TAG, "update values");
        try {
            QueryEnumerator results;
            if(mValues.size() == 0) {
                results = mQuery.run();
                for (Iterator<QueryRow> it = results; it.hasNext();) {
                    Log.d(TAG, "Next row");
                    QueryRow row = it.next();
                    mValues.add((Map) row.getValue());
                }
            }
            else {
                results = mQuery.getRows();
                for (Iterator<QueryRow> it = results; it.hasNext();) {
                    QueryRow row = it.next();
                    Document doc = Database.getInstance(null)
                            .getDocument((String)((Map) row.getValue()).get("_id"));
                    for(int i = 0; i < mValues.size(); i++) {
                        if(mValues.get(i).get("_id").equals(doc.getId())) {
                            Log.d(TAG, "Change item at position " + i);
                            this.notifyItemChanged(i, doc.getProperties());
                        }
                    }
                }
            }
        }
        catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error querying view.", e);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mNameView;
        public final TextView mOccView;
        public final TextView mEduView;
        public final TextView mAgeView;
        public final TextView mMessageView;
        public Map<String, Object> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mNameView = (TextView) view.findViewById(R.id.itemName);
            mOccView = (TextView) view.findViewById(R.id.itemOccupation);
            mEduView = (TextView) view.findViewById(R.id.itemEducation);
            mAgeView = (TextView) view.findViewById(R.id.itemAge);
            mMessageView = (TextView) view.findViewById(R.id.itemMessage);
        }
    }
}
