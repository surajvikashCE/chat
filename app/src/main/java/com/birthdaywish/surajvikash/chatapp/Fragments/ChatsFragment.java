package com.birthdaywish.surajvikash.chatapp.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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

import com.birthdaywish.surajvikash.chatapp.Activities.ChatActivity;
import com.birthdaywish.surajvikash.chatapp.DataModels.Conversation;
import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View view;
    private RecyclerView chatsRecyclerView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private TextView mMessage;
    private DatabaseReference mConvDatabase, mUserDatabase, mMessageDatabase;
    private String curr_id;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_chats, container, false);
        chatsRecyclerView = (RecyclerView)view.findViewById(R.id.chats_fragment_recycler_view);
        progressBar = (ProgressBar)view.findViewById(R.id.chats_fragment_progress_bar);
        mMessage = (TextView)view.findViewById(R.id.chats_fragment_msg);
        progressBar.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        curr_id = mAuth.getCurrentUser().getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child(curr_id);
        mConvDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(curr_id);
        mMessageDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        chatsRecyclerView.setLayoutManager(linearLayoutManager);

        mConvDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.e("test", "Hr"+dataSnapshot);
                if(dataSnapshot.getValue() == null){
                    progressBar.setVisibility(View.INVISIBLE);
                    mMessage.setVisibility(View.VISIBLE);
                    mMessage.setText("No chats yet.\nSend message to a friend.");
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

        Query conversationQuery = mConvDatabase.orderByChild("timeStamp");

        FirebaseRecyclerAdapter<Conversation, ConvViewHolder> adapter = new FirebaseRecyclerAdapter<Conversation, ConvViewHolder>(
                Conversation.class, R.layout.all_users_element_view, ConvViewHolder.class, conversationQuery) {
            @Override
            protected void populateViewHolder(final ConvViewHolder viewHolder, final Conversation conv, int position) {

                final String friendId = getRef(position).getKey();
                Query lastMessageQuery = mMessageDatabase.child(friendId).limitToLast(1);

                //Log.e("query-->", friendId);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String message = dataSnapshot.child("message").getValue().toString();
                        //Log.e("Message", "45"+dataSnapshot);
                        viewHolder.mMessage.setText(message);

                        if(!conv.isSeen()){
                            viewHolder.mMessage.setTypeface(viewHolder.mMessage.getTypeface(), Typeface.BOLD);
                            viewHolder.mMessage.setTextColor(Color.BLACK);
                        }
                        else {
                            viewHolder.mMessage.setTypeface(viewHolder.mMessage.getTypeface(), Typeface.NORMAL);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.e("Message2", "45"+dataSnapshot);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.e("Message3", "45"+dataSnapshot);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.e("Message4", "45"+dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("ErrorChat-->", databaseError.getMessage());
                    }
                });

                mUserDatabase.child(friendId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String friendName = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.mName.setText(friendName);
                        viewHolder.mProfileImage.setImageURI(thumb_image);

                        if(dataSnapshot.hasChild("online")) {

                            String onlineStatus = dataSnapshot.child("online").getValue().toString();
                            if(onlineStatus.equals("true") || onlineStatus.contains("true")){
                                viewHolder.mOnline.setVisibility(View.VISIBLE);
                            }
                            else {
                                viewHolder.mOnline.setVisibility(View.INVISIBLE);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                        if(dataSnapshot == null){
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("user_id", friendId);
                                intent.putExtra("name", friendName);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        chatsRecyclerView.setAdapter(adapter);
    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder{

        TextView mName, mMessage;
        SimpleDraweeView mProfileImage;
        ImageView mOnline;
        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mName = itemView.findViewById(R.id.all_users_element_name);
            mMessage = itemView.findViewById(R.id.all_users_element_status);
            mOnline = itemView.findViewById(R.id.all_users_element_online_image);
            mProfileImage = itemView.findViewById(R.id.all_users_element_image);
            mOnline.setVisibility(View.INVISIBLE);
        }
    }
}