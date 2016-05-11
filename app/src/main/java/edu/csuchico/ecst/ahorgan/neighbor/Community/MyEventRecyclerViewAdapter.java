package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import edu.csuchico.ecst.ahorgan.neighbor.Community.EventFragment.OnListFragmentInteractionListener;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Database;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Event;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Profile;
import edu.csuchico.ecst.ahorgan.neighbor.Community.items.DummyContent.DummyItem;
import edu.csuchico.ecst.ahorgan.neighbor.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyEventRecyclerViewAdapter extends RecyclerView.Adapter<MyEventRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "MyEventRecyclerVA";
    public static final int ACTION_REMOVE = 0;
    public static final int ACTION_TOGGLE = 1;
    public static final int ACTION_PROFILE = 2;
    private final List<Map<String, Object>> mValues;
    private final EventFragment.OnListFragmentInteractionListener mListener;
    private LiveQuery mQuery;

    public MyEventRecyclerViewAdapter(List<Map<String, Object>> items, EventFragment.OnListFragmentInteractionListener listener) {
        Log.d(TAG, "MyProfileRecyclerViewAdapter()");
        Database db = Database.getInstance(null);
        mQuery = db.getEventsbydateView().createQuery().toLiveQuery();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        mQuery.setStartKey(dateFormat.format(new Date()));

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        Log.d(TAG, getItemCount() + " items");

        Map<String, Object> values = mValues.get(position);
        if(!values.get(Event.OWNERPROFILE).equals("me"))
            holder.mOwnerView.setText((String)values.get(Event.OWNERPROFILE));
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

        holder.mDateTimeView.setText((String)values.get(Event.DATETIME));
        holder.mNameView.setText((String)values.get(Event.NAME));
        holder.mLocationtView.setText((String)values.get(Event.LOCATION));
        holder.mDetailsView.setText((String)values.get(Event.DETAILS));
        holder.mCheckBox.setChecked((Boolean)values.get(Event.BCAST));

        holder.mItem = values;

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener) {
                    mValues.remove(holder.mItem);
                    mListener.onListFragmentInteraction(holder.mItem, ACTION_REMOVE);
                }
                return false;
            }
        });
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mListener) {
                    Boolean toggle = !(Boolean)holder.mItem.get(Event.BCAST);
                    holder.mItem.put(Event.BCAST, toggle);
                    holder.mCheckBox.setChecked((Boolean)holder.mItem.get(Event.BCAST));
                    mListener.onListFragmentInteraction(holder.mItem, ACTION_TOGGLE);
                }
            }
        });
        holder.mOwnerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListFragmentInteraction(holder.mItem, ACTION_PROFILE);
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
        public final TextView mNameView;
        public final TextView mLocationtView;
        public final TextView mDateTimeView;
        public final TextView mDetailsView;
        public final TextView mOwnerView;
        public final CheckBox mCheckBox;
        public Map<String, Object> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.eventNameTv);
            mLocationtView = (TextView) view.findViewById(R.id.eventLocationTv);
            mDateTimeView = (TextView) view.findViewById(R.id.eventDateTimeTv);
            mDetailsView = (TextView) view.findViewById(R.id.eventDetailsTv);
            mOwnerView = (TextView) view.findViewById(R.id.eventOwnerTv);
            mCheckBox = (CheckBox) view.findViewById(R.id.broadcastCheckBox);
        }
    }
}
