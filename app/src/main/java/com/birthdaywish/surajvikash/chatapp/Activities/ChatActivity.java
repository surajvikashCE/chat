package com.birthdaywish.surajvikash.chatapp.Activities;


import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.birthdaywish.surajvikash.chatapp.Adapters.MessageAdapter;
import com.birthdaywish.surajvikash.chatapp.DataModels.Messages;
import com.birthdaywish.surajvikash.chatapp.GetTimeAgo;
import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private String friendId, friendName;
    private Toolbar mToobar;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
   // private ProgressDialog progressDialog;
    private TextView mTitleName, mTitleLastSeen;
    private SimpleDraweeView mProfileImage;
    private String curr_id, thumb_image;
    private EditText mChatMessage;
    private ImageButton mChatImageBtn, mChatSendBtn;
    private RecyclerView messagesListRecyclerView;
    private List<Messages> mMessagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter messageAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fresco.initialize(this);
        setContentView(R.layout.activity_chat);

        mToobar = (Toolbar) findViewById(R.id.chat_activity_appbar);
        setSupportActionBar(mToobar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatMessage = (EditText)findViewById(R.id.chat_message_edit_text);
        mChatImageBtn = (ImageButton)findViewById(R.id.chat_add_images);
        mChatSendBtn = (ImageButton)findViewById(R.id.chat_send_button);
        messagesListRecyclerView = (RecyclerView)findViewById(R.id.chat_messages_recycler_view);
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.chat_refresh_layout);
        mLinearLayoutManager = new LinearLayoutManager(this);


        mAuth = FirebaseAuth.getInstance();
        curr_id = mAuth.getCurrentUser().getUid();

        friendId = getIntent().getStringExtra("user_id");
        friendName = getIntent().getStringExtra("name");

        //Log.e("Chat-->", friendName+"  "+friendId);

        getSupportActionBar().setTitle(friendName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionBarView);
        mTitleName = (TextView)findViewById(R.id.custom_bar_display_name);
        mTitleLastSeen = (TextView)findViewById(R.id.custom_bar_last_seen);
        mProfileImage = (SimpleDraweeView) findViewById(R.id.custom_bar_image);
        mTitleName.setText(friendName);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        messagesListRecyclerView.setHasFixedSize(true);
        messagesListRecyclerView.setLayoutManager(mLinearLayoutManager);
        messageAdapter = new MessageAdapter(mMessagesList, thumb_image);

        //messagesListRecyclerView.setAdapter(messageAdapter);

        mRootRef.child("Chats").child(curr_id).child(friendId).child("seen").setValue(true);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            loadMessages();
        }
        else{
            loadMessages1();
        }*/
        loadMessages();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("user_id", friendId);
                startActivity(intent);
            }
        });

        mRootRef.child("Users").child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastSeen = dataSnapshot.child("online").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                messageAdapter = new MessageAdapter(mMessagesList, thumb_image);
                messagesListRecyclerView.setAdapter(messageAdapter);
                messageAdapter.notifyDataSetChanged();

                if(mMessagesList.size() > 0)
                    messagesListRecyclerView.scrollToPosition(mMessagesList.size()-1);
                    //messagesListRecyclerView.scrollToPosition(0);

                mProfileImage.setImageURI(thumb_image);
                if(lastSeen.equals("true") || lastSeen.contains("true")){
                    mTitleLastSeen.setText("Online");
                }
                else{
                    GetTimeAgo gto = new GetTimeAgo();
                    long lastTime = Long.parseLong(lastSeen);
                    String lastSeenTime = gto.getTimeAgo(lastTime, getApplicationContext());
                    mTitleLastSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chats").child(curr_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(friendId)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chats/"+curr_id+"/"+friendId, chatAddMap);
                    chatUserMap.put("Chats/"+friendId+"/"+curr_id, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.e("Chat log1--> ", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();
            }
        });


        /*//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            final LinearLayoutManager lm = (LinearLayoutManager) messagesListRecyclerView.getLayoutManager();
            messagesListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int totalItem = lm.getItemCount();
                    int firstItem = lm.findFirstVisibleItemPosition();
                    //lm.getChildAt(firstVisibleItemPosition);
                    Log.e("vote", "total " + totalItem + " first vis" + firstItem);
                    //Log.e("test", " " + lm.getChildAt(firstVisibleItemPosition) + "l  " + lm.getChildAt(lastVisibleItem));

                    if(firstItem <= 1){
                        mCurrentPage++;

                        itemPos = 0;

                        loadMoreMessages();
                    }
                }
            });
        //}*/
    }

    private void loadMessages1() {

        mRootRef.child("messages").child(curr_id).child(friendId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Log.e("Chat messages", "he "+dataSnapshot.toString());
                Messages messages = dataSnapshot.getValue(Messages.class);
                mMessagesList.add(messages);

                messagesListRecyclerView.scrollToPosition(mMessagesList.size()-1);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = mChatMessage.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/"+curr_id+"/"+friendId;
            String chat_user_ref = "messages/"+friendId+"/"+curr_id;

            DatabaseReference userMessageRef = mRootRef.child("messages").child(curr_id).child(friendId).push();
            String pushId = userMessageRef.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", curr_id);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+"/"+pushId, messageMap);
            messageUserMap.put(chat_user_ref+"/"+pushId, messageMap);

            mChatMessage.setText("");

            mRootRef.child("Chats").child(curr_id).child(friendId).child("seen").setValue(true);
            mRootRef.child("Chats").child(curr_id).child(friendId).child("timeStamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chats").child(friendId).child(curr_id).child("seen").setValue(false);
            mRootRef.child("Chats").child(friendId).child(curr_id).child("timeStamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.e("Chat log2--> ", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mMessagesList.size() > 0)
            messagesListRecyclerView.scrollToPosition(mMessagesList.size()-1);

        mRootRef.child("Users").child(curr_id).child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootRef.child("Users").child(curr_id).child("online").setValue(ServerValue.TIMESTAMP);
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(curr_id).child(friendId);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey1 = dataSnapshot.getKey();
                Log.e(itemPos+"check key --> ", messageKey1);



                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    Log.e("last key --> ", messageKey);

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }
                itemPos++;

                mMessagesList.add(message);
                messageAdapter.notifyDataSetChanged();

                messagesListRecyclerView.scrollToPosition(mMessagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(curr_id).child(friendId);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    mMessagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1) {
                    mLastKey = messageKey;
                }

                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                messageAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayoutManager.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
