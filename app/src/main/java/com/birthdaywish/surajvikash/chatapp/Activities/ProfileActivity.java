package com.birthdaywish.surajvikash.chatapp.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    //private ImageView mProfileImage;
    private SimpleDraweeView mProfileImageD;
    private TextView mName, mStatus;
    private Button mSendRequest, mDeclineRequest;
    //private Toolbar mToobar;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef, userDataRef, friendRequestDataRef, friendsDataRef, notificationsDataRef;
    private ProgressDialog progressDialog;
    private String mCurrStatus;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fresco.initialize(this);
        setContentView(R.layout.activity_profile);

        //mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mProfileImageD = (SimpleDraweeView) findViewById(R.id.profile_image_drawee);
        mName = (TextView)findViewById(R.id.profile_name);
        mStatus = (TextView)findViewById(R.id.profile_status);
        mSendRequest = (Button)findViewById(R.id.profile_send_rqst_btn);
        mDeclineRequest = (Button)findViewById(R.id.profile_decline_rqst_btn);
        progressBar = (ProgressBar)findViewById(R.id.profile_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);
        mDeclineRequest.setVisibility(View.INVISIBLE);
        mDeclineRequest.setEnabled(false);

        /*mToobar = (Toolbar) findViewById(R.id.profile_activity_appbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("Profile");*/


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mCurrStatus = "not_friends";
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mRef.child("online").setValue("true");

        final String user_id = getIntent().getStringExtra("user_id");
        final String curr_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String a = "a"+user_id;
        Log.e("Profile --> ", a.toString());

        if(a.toString().equals("anull") || a.toString() == "anull"){
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
        else if(user_id.equals(curr_uid)){
            mSendRequest.setVisibility(View.INVISIBLE);
            mDeclineRequest.setVisibility(View.INVISIBLE);
            mDeclineRequest.setEnabled(false);
        }

        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        userDataRef.keepSynced(true);
        friendRequestDataRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        friendRequestDataRef.keepSynced(true);
        friendsDataRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsDataRef.keepSynced(true);
        notificationsDataRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        userDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").toString();
                mName.setText(name);
                mStatus.setText(status);
                mProfileImageD.setImageURI(image);

                friendRequestDataRef.child(curr_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String rqst_type = dataSnapshot.child(user_id).child("requestType").getValue().toString();
                            if(rqst_type.equals("received") || rqst_type.contains("received")){
                                mCurrStatus = "rqst_received";
                                mSendRequest.setText("ACCEPT FRIEND REQUEST");
                                mDeclineRequest.setVisibility(View.VISIBLE);
                                mDeclineRequest.setEnabled(true);
                            }
                            else if(rqst_type.equals("sent")||rqst_type.contains("sent")){
                                mCurrStatus = "rqst_sent";
                                mSendRequest.setText("CANCEL FRIEND REQUEST");
                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                mDeclineRequest.setEnabled(false);
                            }
                            progressDialog.dismiss();

                        }
                        else{
                            friendsDataRef.child(curr_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrStatus = "friends";
                                        mSendRequest.setText("UNFRIEND THIS PERSON");
                                        mDeclineRequest.setVisibility(View.INVISIBLE);
                                        mDeclineRequest.setEnabled(false);
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, ""+databaseError, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, ""+databaseError, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, ""+databaseError, Toast.LENGTH_SHORT).show();
            }
        });

        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                mSendRequest.setEnabled(false);
                Log.e("Profile status-->", mCurrStatus);

                if (mCurrStatus.equals("not_friends")){
                    friendRequestDataRef.child(curr_uid).child(user_id).child("requestType").setValue( "sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isComplete()){
                                        friendRequestDataRef.child(user_id).child(curr_uid).child("requestType").setValue("received")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        saveNotifiation(curr_uid, user_id);

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(ProfileActivity.this, "Error sending request",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(ProfileActivity.this, "Error sending request",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                if(mCurrStatus.equals("rqst_sent")){
                    friendRequestDataRef.child(curr_uid).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendRequestDataRef.child(user_id).child(curr_uid).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendRequest.setEnabled(true);
                                    mSendRequest.setText("SEND FRIEND REQUEST");
                                    mDeclineRequest.setVisibility(View.INVISIBLE);
                                    mDeclineRequest.setEnabled(false);
                                    mCurrStatus = "not_friends";

                                    deleteNotification(user_id, curr_uid);
                                    progressBar.setVisibility(View.INVISIBLE);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                if(mCurrStatus.equals("rqst_received")){
                    final String curr_date = DateFormat.getDateTimeInstance().format(new Date());

                    friendsDataRef.child(curr_uid).child(user_id).child("date").setValue(curr_date)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isComplete()){
                                friendsDataRef.child(user_id).child(curr_uid).child("date").setValue(curr_date)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        friendRequestDataRef.child(curr_uid).child(user_id).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        friendRequestDataRef.child(user_id).child(curr_uid).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mSendRequest.setEnabled(true);
                                                                mSendRequest.setText("UNFRIEND THIS PERSON");
                                                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                                                mDeclineRequest.setEnabled(false);
                                                                mCurrStatus = "friends";
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Error\n", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                }

                if(mCurrStatus.equals("friends")){
                    progressBar.setVisibility(View.INVISIBLE);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setMessage("Are you sure to unfriend this person?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            progressBar.setVisibility(View.VISIBLE);
                            friendsDataRef.child(user_id).child(curr_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isComplete()){
                                        friendsDataRef.child(curr_uid).child(user_id).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mSendRequest.setEnabled(true);
                                                mSendRequest.setText("SEND FRIEND REQUEST");
                                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                                mDeclineRequest.setEnabled(false);
                                                mCurrStatus = "not_friends";
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(ProfileActivity.this, "Error\n", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mSendRequest.setVisibility(View.VISIBLE);
                            mSendRequest.setEnabled(true);
                            mDeclineRequest.setVisibility(View.INVISIBLE);
                            mDeclineRequest.setEnabled(false);
                            dialogInterface.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });

        mDeclineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                mDeclineRequest.setEnabled(false);
                if(mCurrStatus.equals("rqst_received")){
                    friendRequestDataRef.child(curr_uid).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendRequestDataRef.child(user_id).child(curr_uid).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mSendRequest.setEnabled(true);
                                                    mSendRequest.setText("SEND FRIEND REQUEST");
                                                    mDeclineRequest.setVisibility(View.INVISIBLE);
                                                    mDeclineRequest.setEnabled(false);
                                                    mCurrStatus = "not_friends";
                                                    //deleteNotification(curr_uid, user_id);
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDeclineRequest.setEnabled(true);
                            Toast.makeText(ProfileActivity.this, "Error\n"+e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }


    private void deleteNotification(final String user_id, final String curr_uid) {

        notificationsDataRef.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> keys = dataSnapshot.getChildren();

                for(DataSnapshot key : keys){
                    String id = key.child("from").getValue().toString();
                    if(id.equals(curr_uid) || id.contains(curr_uid)){
                        notificationsDataRef.child(user_id).child(key.getKey()).removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveNotifiation(String curr_uid, String user_id) {

        HashMap<String, String> data = new HashMap<>();
        data.put("from", curr_uid);
        data.put("type", "request");

        notificationsDataRef.child(user_id).push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mSendRequest.setEnabled(true);
                mSendRequest.setText("CANCEL FRIEND REQUEST");
                mDeclineRequest.setVisibility(View.INVISIBLE);
                mDeclineRequest.setEnabled(false);
                mCurrStatus = "rqst_sent";
                Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mRef.child("online").setValue("true");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRef.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

}
