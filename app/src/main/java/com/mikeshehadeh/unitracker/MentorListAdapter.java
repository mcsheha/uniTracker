package com.mikeshehadeh.unitracker;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MentorListAdapter extends RecyclerView.Adapter<MentorListAdapter.MentorListViewHolder> {
    private MentorListAdapter.OnItemClickListener mListener;
    private Context mContext;
    private Cursor mCursor;



    public MentorListAdapter (Context context, Cursor cursor){
        mContext = context;
        mCursor = cursor;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(MentorListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class MentorListViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;

        public MentorListViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.course_item_image_view);
            mTextView1 = itemView.findViewById(R.id.course_item_text_view);
            mTextView2 = itemView.findViewById(R.id.mentor_textView2);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }

                }
            });

        }
    }

    @NonNull
    @Override
    public MentorListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mentor_item, parent, false);
        MentorListViewHolder mlvh = new MentorListViewHolder(v, mListener);
        return mlvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MentorListViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        String mentorName = mCursor.getString(mCursor.getColumnIndex(DBTables.mentorTable.COLUMN_MENTOR_NAME));
        String mentorEmail = mCursor.getString(mCursor.getColumnIndex(DBTables.mentorTable.COLUMN_MENTOR_EMAIL));
        //String mentorID = mCursor.getString(mCursor.getColumnIndex(DBTables.Table.COLUMN_COURSE_DESIGNATOR));
        //String courseTitle = mCursor.getString(mCursor.getColumnIndex(DBTables.courseTable.COLUMN_COURSE_NAME));
        long id = mCursor.getLong(mCursor.getColumnIndex(DBTables.mentorTable.COLUMN_MENTOR_ID));

        holder.mImageView.setImageResource(R.drawable.person);
        holder.mTextView1.setText(mentorName);
        holder.mTextView2.setText(mentorEmail);
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

}
