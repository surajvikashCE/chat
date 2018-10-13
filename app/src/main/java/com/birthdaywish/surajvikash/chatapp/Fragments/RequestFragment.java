package com.birthdaywish.surajvikash.chatapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.birthdaywish.surajvikash.chatapp.Activities.ProfileActivity;
import com.birthdaywish.surajvikash.chatapp.DataModels.Requests;
import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    View view;

    private FirebaseAuth mAuth;
    private TextView mMessage;
    private RecyclerView mRecyclerView;
    private DatabaseReference mRequestDatabase, mUsersDatabase;
    private ProgressBar progressBar;
    //private String curr_id;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_request, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_requests_recyclerview);
        progressBar = (ProgressBar)view.findViewById(R.id.fragment_request_progress_bar);
        mMessage = (TextView)view.findViewById(R.id.fragment_request_msg);
        progressBar.setVisibility(View.VISIBLE);
        mMessage.setVisibility(View.INVISIBLE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        String curr_id = mAuth.getCurrentUser().getUid();

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequest").child(curr_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mRequestDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Log.e("test", "hr"+dataSnapshot);

                if(dataSnapshot.getValue() == null){
                    progressBar.setVisibility(View.INVISIBLE);
                    mMessage.setVisibility(View.VISIBLE);
                    mMessage.setText("No pending requests");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestsViewholder> adapter = new FirebaseRecyclerAdapter<Requests, RequestsViewholder>(
                Requests.class, R.layout.all_users_element_view, RequestsViewholder.class, mRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewholder viewHolder, final Requests requests, int position) {

                final String user_id = getRef(position).getKey();
                //Log.e("test  ", user_id);
                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.mProfilePic.setImageURI(dataSnapshot.child("thumb_image").getValue().toString());
                        viewHolder.mName.setText(dataSnapshot.child("name").getValue().toString());
                        if(requests.getRequestType().equals("received") || requests.getRequestType().contains("received")){
                            viewHolder.mRequestType.setText("Request received");
                        }
                        else {
                            viewHolder.mRequestType.setText("Request sent");
                        }
                        progressBar.setVisibility(View.INVISIBLE);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), ProfileActivity.class);
                                intent.putExtra("user_id", user_id);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
    }

    public static class RequestsViewholder extends RecyclerView.ViewHolder{

        SimpleDraweeView mProfilePic;
        TextView mName, mRequestType;
        View mView;

        public RequestsViewholder(View itemView) {
            super(itemView);

            mView = itemView;
            mProfilePic = itemView.findViewById(R.id.all_users_element_image);
            mName = itemView.findViewById(R.id.all_users_element_name);
            mRequestType = itemView.findViewById(R.id.all_users_element_status);
        }
    }
}
