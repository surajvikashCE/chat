package com.birthdaywish.surajvikash.chatapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.birthdaywish.surajvikash.chatapp.Activities.ChatActivity;
import com.birthdaywish.surajvikash.chatapp.Activities.ProfileActivity;
import com.birthdaywish.surajvikash.chatapp.DataModels.Friends;
import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.backends.pipeline.Fresco;
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
public class FriendsFragment extends Fragment {

    private View view;
    private DatabaseReference friendsDataRef, usersDataRef;
    private RecyclerView friendsRecyclerView;
    private FirebaseAuth mAuth;
    private TextView mMessage;
    private ProgressBar progressBar;
    //private String friendName;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Fresco.initialize(getContext());
        view =  inflater.inflate(R.layout.fragment_friends, container, false);

        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view);
        progressBar = view.findViewById(R.id.friends_progress_bar);
        mMessage = view.findViewById(R.id.friends_msg);
        mMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();

        String curr_id = mAuth.getCurrentUser().getUid();

        friendsDataRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(curr_id);
        friendsDataRef.keepSynced(true);
        usersDataRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDataRef.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        friendsRecyclerView.setLayoutManager(linearLayoutManager);

        //if(friendsDataRef.getKey())

        friendsDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() == null){
                    progressBar.setVisibility(View.INVISIBLE);
                    mMessage.setVisibility(View.VISIBLE);
                    mMessage.setText("You have no friends yet.\nSend requests to make new friends.");
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

        FirebaseRecyclerAdapter<Friends, FriendsViewholder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewholder>
                (Friends.class, R.layout.all_users_element_view, FriendsViewholder.class, friendsDataRef) {
            @Override
            protected void populateViewHolder(final FriendsViewholder viewHolder, Friends friends, int position) {
                viewHolder.mDate.setText(friends.getDate());

                final String friendId = getRef(position).getKey();

                usersDataRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String friendName = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        //Toast.makeText(getContext(), "th"+thumb_image, Toast.LENGTH_SHORT).show();
                        viewHolder.mName.setText(friendName);
                        viewHolder.mProfileImage.setImageURI(Uri.parse(thumb_image));

                        if(dataSnapshot.hasChild("online")){
                            String onlineStatus = dataSnapshot.child("online").getValue().toString();

                            if(onlineStatus.equals("true") || onlineStatus.contains("true")){
                                viewHolder.mOnline.setVisibility(View.VISIBLE);
                            }
                            else {
                                viewHolder.mOnline.setVisibility(View.INVISIBLE);
                            }
                            progressBar.setVisibility(View.INVISIBLE);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Select options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i==0){
                                                startActivity(new Intent(getContext(), ProfileActivity.class)
                                                        .putExtra("user_id", friendId));
                                            }
                                            else if(i == 1){
                                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                                intent.putExtra("user_id", friendId);
                                                intent.putExtra("name", friendName);
                                                Log.e("Chat-->", friendName+"  "+friendId);

                                                startActivity(intent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressBar.setVisibility(View.INVISIBLE);
                        //Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        friendsRecyclerView.setAdapter(adapter);
    }

    public static class FriendsViewholder extends RecyclerView.ViewHolder{

        TextView mName, mDate;
        ImageView mOnline;
        SimpleDraweeView mProfileImage;
        View mView;

        public FriendsViewholder(View itemView) {
            super(itemView);
            mView = itemView;
            mName = itemView.findViewById(R.id.all_users_element_name);
            mDate = itemView.findViewById(R.id.all_users_element_status);
            mOnline = itemView.findViewById(R.id.all_users_element_online_image);
            mProfileImage = itemView.findViewById(R.id.all_users_element_image);
            mOnline.setVisibility(View.INVISIBLE);
        }
    }
}
