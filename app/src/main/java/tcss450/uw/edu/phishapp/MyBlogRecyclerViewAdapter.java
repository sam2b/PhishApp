package tcss450.uw.edu.phishapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tcss450.uw.edu.phishapp.BlogFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.phishapp.blog.BlogPost;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BlogPost} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyBlogRecyclerViewAdapter extends RecyclerView.Adapter<MyBlogRecyclerViewAdapter.ViewHolder> {

    private final List<BlogPost> mValues;
    private final OnListFragmentInteractionListener mListener;

    //Constructor
    public MyBlogRecyclerViewAdapter(List<BlogPost> blogs, OnListFragmentInteractionListener listener) {
        mValues = blogs;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_blog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mBlog = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).id);
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mDateView.setText(mValues.get(position).getPubDate());
        holder.mSamplingView.setText(mValues.get(position).getTeaser());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onBlogListFragmentInteraction(holder.mBlog);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        //public final TextView mIdView;
        private final TextView mTitleView;
        private final TextView mDateView;
        private final TextView mSamplingView; //aka: Teaser
        private BlogPost mBlog;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            //mIdView =  view.findViewById(R.id.item_number); //TODO is this needed?
            mTitleView = view.findViewById(R.id.blog_title);
            mDateView = view.findViewById(R.id.blog_date);
            mSamplingView = view.findViewById(R.id.blog_sampling);
        }

        @Override
        public String toString() {
            return super.toString()
                    + " '"
                    //+ mIdView.getText() + "\n"
                    + mTitleView.getText() + "\n"
                    + mDateView.getText() + "\n"
                    + mSamplingView.getText() + "'";
        }
    }

}
