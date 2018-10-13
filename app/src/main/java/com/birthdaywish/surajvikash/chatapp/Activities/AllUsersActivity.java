package com.birthdaywish.surajvikash.chatapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.birthdaywish.surajvikash.chatapp.R;
import com.birthdaywish.surajvikash.chatapp.DataModels.Users;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar mToobar;
    private DatabaseReference mRef;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    //private Button fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fresco.initialize(this);
        setContentView(R.layout.activity_all_users);

        recyclerView = (RecyclerView)findViewById(R.id.all_users_recyclerview);
        progressBar = (ProgressBar)findViewById(R.id.all_users_progress_bar);
        //fb = (Button)findViewById(R.id.fb_button);

        mToobar = (Toolbar) findViewById(R.id.all_users_activity_appbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAuth = FirebaseAuth.getInstance();

        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
        mRef.keepSynced(true);

        /*fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                try{
                    startActivity(intent);
                }
                catch (Exception e){
                    Intent playIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.katana"));
                    startActivity(playIntent);
                }
            }
        });*/

        FirebaseRecyclerAdapter<Users, ViewHolder> adapter = new FirebaseRecyclerAdapter<Users, ViewHolder>
                (Users.class, R.layout.all_users_element_view, ViewHolder.class, mRef) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, Users user, int position) {

                //String curr_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String user_id = getRef(position).getKey();

                viewHolder.name.setText(user.getName());
                viewHolder.status.setText(user.getStatus());

                if(!user.getThumb_image().equals("default") || !user.getThumb_image().contains("default")) {
                    viewHolder.profileImage.setImageURI(user.getThumb_image());
                }
                else{
                    viewHolder.profileImage.setActualImageResource(R.mipmap.londa);
                }
                progressBar.setVisibility(View.GONE);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(AllUsersActivity.this, ProfileActivity.class)
                                .putExtra("user_id", user_id));
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {

        mRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
        Log.e("Start-->", "2");
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onRestart() {
        mRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
        super.onRestart();

    }

    /*@Override
    protected void onStop() {
        super.onStop();
        Log.e("Stop-->", "2");
        mRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
    }*/

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, status;
        SimpleDraweeView profileImage;
        View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            name = itemView.findViewById(R.id.all_users_element_name);
            status = itemView.findViewById(R.id.all_users_element_status);
            profileImage = itemView.findViewById(R.id.all_users_element_image);
        }
    }
}
