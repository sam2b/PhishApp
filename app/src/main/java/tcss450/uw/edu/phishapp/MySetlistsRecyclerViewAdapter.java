package tcss450.uw.edu.phishapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tcss450.uw.edu.phishapp.SetlistFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.phishapp.setlists.Setlist;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Setlist} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MySetlistsRecyclerViewAdapter extends RecyclerView.Adapter<MySetlistsRecyclerViewAdapter.ViewHolder> {

    private final List<Setlist> mValues;
    private final OnListFragmentInteractionListener mListener;

    //Constructor
    private MySetlistsRecyclerViewAdapter(List<Setlist> setlists, OnListFragmentInteractionListener listener) {
        mValues = setlists;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_setlists, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mSetlist = mValues.get(position);
        holder.mLongDateView.setText(mValues.get(position).getLongDate());
        holder.mLocationView.setText(mValues.get(position).getLocation());
        holder.mVenueView.setText(mValues.get(position).getVenue());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSetlistListFragmentInteraction(holder.mSetlist);
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
        public final TextView mLongDateView;
        public final TextView mLocationView;
        public final TextView mVenueView;
        public Setlist mSetlist;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLongDateView = view.findViewById(R.id.setlist_longDate);
            mLocationView = view.findViewById(R.id.setlist_location);
            mVenueView = view.findViewById(R.id.setlist_Venue);
        }

        @Override
        public String toString() {
            return super.toString()
                    + " '"
                    + mLongDateView.getText() + "\n"
                    + mLocationView.getText() + "\n"
                    + mVenueView.getText() + "'";
        }
    }

}
