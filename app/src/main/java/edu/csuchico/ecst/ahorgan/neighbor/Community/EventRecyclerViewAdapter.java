package edu.csuchico.ecst.ahorgan.neighbor.Community;

import android.os.AsyncTask;
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
import edu.csuchico.ecst.ahorgan.neighbor.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a dummyItem and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "MyEventRecyclerVA";
    public static final int ACTION_REMOVE = 0;
    public static final int ACTION_TOGGLE = 1;
    public static final int ACTION_PROFILE = 2;
    private final List<Map<String, Object>> mValues;
    private final EventFragment.OnListFragmentInteractionListener mListener;
    private LiveQuery mQuery;
    private Database db;
    com.couchbase.lite.View eventView;

    public EventRecyclerViewAdapter(List<Map<String, Object>> items, EventFragment.OnListFragmentInteractionListener listener) {
        Log.d(TAG, "MyProfileRecyclerViewAdapter()");
        mValues = new ArrayList<>();
        db = Database.getInstance(null);
        eventView = db.getEventsbydateView();
        if(!items.isEmpty() && items.get(0).containsKey(EventFragment.EVENT_TYPE)) {
            switch ((int)items.get(0).get(EventFragment.EVENT_TYPE)) {
                case EventFragment.BROADCASTED_EVENTS:
                    eventView = db.getBroadcastEventsView();
                    break;
                default:
                    break;
            }
        }
        mQuery = eventView.createQuery().toLiveQuery();
        mQuery.setDescending(false);
        mListener = listener;
        try {
            QueryEnumerator results = mQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                QueryRow row = it.next();
                mValues.add((Map) row.getValue());
            }
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getMessage());
        }
        mQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                updateValues(event);
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder");
        Log.d(TAG, getItemCount() + " items");

        Map<String, Object> values = mValues.get(position);
        if(!values.get(Event.OWNER).equals("me"))
            holder.mOwnerView.setText((String)values.get(Event.OWNER));
        values.putAll(db.getDocument((String)values.get("_id")).getProperties());

        for ( Map.Entry entry : values.entrySet() ) {
            Log.d(TAG, entry.getKey() + " : " + entry.getValue());
        }

        holder.mStartDateTimeView.setText((String)values.get(Event.STARTDATETIME));
        holder.mEndDateTimeView.setText((String)values.get(Event.ENDDATETIME));
        holder.mNameView.setText((String)values.get(Event.TITLE));
        holder.mLocationtView.setText((String)values.get(Event.LOCATION));
        holder.mDetailsView.setText((String)values.get(Event.DETAILS));
        holder.mCheckBox.setChecked((Boolean)values.get(Event.BCAST));

        holder.mItem = values;

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener) {
                    db.deleteDocument(mValues.get(position).get("_id").toString());
                    mValues.remove(position);
                    notifyItemRemoved(position);
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
                    db.addDocument(holder.mItem.get("_id").toString(), holder.mItem);
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

    public void updateValues(LiveQuery.ChangeEvent event) {
        Log.d(TAG, "update values");
        QueryEnumerator results = event.getRows();
        for (Iterator<QueryRow> it = results; it.hasNext();) {
            QueryRow row = it.next();
            Document doc = row.getDocument();
            int index = mValues.indexOf(doc.getProperties());
            if(index == -1 && !doc.isDeleted()) {
                /*
                    Item was inserted. Must find where item should be inserted
                    since mValues is sorted by start date
                 */
                for(Map<String, Object> entry : mValues) {
                    if(doc.getProperties().get(Event.STARTDATETIME).toString()
                            .compareTo(entry.get(Event.STARTDATETIME).toString()) <= 0) {
                        index = mValues.indexOf(entry);
                        mValues.add(index, doc.getProperties());
                        notifyItemInserted(index);
                        break;
                    }
                }
            }
            else if(!mValues.get(index).equals(doc.getProperties()))
                notifyItemChanged(index, doc.getProperties());
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
        public final TextView mStartDateTimeView;
        public final TextView mEndDateTimeView;
        public final TextView mDetailsView;
        public final TextView mOwnerView;
        public final CheckBox mCheckBox;
        public Map<String, Object> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.eventNameTv);
            mLocationtView = (TextView) view.findViewById(R.id.eventLocationTv);
            mStartDateTimeView = (TextView) view.findViewById(R.id.eventStartDateTimeTv);
            mEndDateTimeView = (TextView) view.findViewById(R.id.eventEndDateTimeTv);
            mDetailsView = (TextView) view.findViewById(R.id.eventDetailsTv);
            mOwnerView = (TextView) view.findViewById(R.id.eventOwnerTv);
            mCheckBox = (CheckBox) view.findViewById(R.id.broadcastCheckBox);
        }
    }
}
