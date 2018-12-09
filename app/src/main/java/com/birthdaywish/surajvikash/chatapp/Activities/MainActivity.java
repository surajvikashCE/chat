package com.birthdaywish.surajvikash.chatapp.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import com.birthdaywish.surajvikash.chatapp.Adapters.MyPagerAdapter;
import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

/*    Button button;
    EditText mName, mContact, mEmail, mAddress;
    String name, contact, email, address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.button);
        mName = (EditText)findViewById(R.id.name);
        mAddress = (EditText)findViewById(R.id.address);
        mEmail = (EditText)findViewById(R.id.email);
        mContact = (EditText)findViewById(R.id.contact);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = mName.getText().toString();
                address = mAddress.getText().toString();
                email = mEmail.getText().toString();
                contact = mContact.getText().toString();

                Toast.makeText(MainActivity.this, name+" "+contact+" "+email+" "+address, Toast.LENGTH_SHORT).show();
            }
        });

    }
}*/

    public static boolean Homeshown = false;
    private static final int PERMISSIONS_REQUEST = 100;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference userRef;
    private TabLayout tabs;
    private ViewPager viewPager;
    private Toolbar mToobar;
    private MyPagerAdapter mPagerAdapter;
    private SharedPreferences userPref;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Fresco.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Homeshown = true;

        tabs = (TabLayout)findViewById(R.id.main_tablayout);
        viewPager = (ViewPager)findViewById(R.id.main_viewpager);

        mToobar = (Toolbar) findViewById(R.id.main_activity_appbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("Chat App");

        userPref = getSharedPreferences("users", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() == null){
                    startActivity(new Intent(MainActivity.this, EntryPage.class));
                    finish();
                }
                else {
                    userRef.child("online").setValue("true");
                }
            }
        };

        if(mAuth.getCurrentUser() != null){
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        userRef.child("online").setValue(ServerValue.TIMESTAMP);
        Log.e("Main stop", "1");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() == null){
                    startActivity(new Intent(MainActivity.this, EntryPage.class));
                    finish();
                }
                else {
                    userRef.child("online").setValue("true");
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.main_menu_sign_out :

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("Sign out");
                builder.setMessage("Are you sure?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        userPref.edit().clear().apply();
                        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show(MainActivity.this,"", "Logging you out....");
                        thread = new Thread(){
                            @Override
                            public void run() {
                                //super.run();
                                try {
                                    thread.sleep(1000);
                                    FirebaseAuth.getInstance().signOut();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();

                break;

            case R.id.main_menu_settings :
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.main_menu_users :
                startActivity(new Intent(this, AllUsersActivity.class));
                break;
            case R.id.main_menu_location:
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    finish();
                }
                int permission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    startTrackerService();
                } else {

//If the app doesn’t currently have access to the user’s location, then request access//

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST);
                }
                break;
            case R.id.main_menu_map:
                startActivity(new Intent(this, MapsActivity.class));
                break;





        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {

//If the permission has been granted...//

        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//...then start the GPS tracking service//

            startTrackerService();
        } else {

//If the user denies the permission request, then display a toast with some more information//

            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
        }
    }
    private void startTrackerService() {
        startService(new Intent(this, TrackingService.class));

//Notify the user that tracking has been enabled//

        Toast.makeText(this, "GPS tracking enabled", Toast.LENGTH_SHORT).show();

//Close MainActivity//

        finish();
    }

    public void notify(View view) {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.chat_button_bg)
//                .setContentTitle("Hello")
//                .setContentText("sua")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//// notificationId is a unique int for each notification that you must define
//        notificationManager.notify(12, mBuilder.build());

        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), "notify_001");
        Intent ii = new Intent(this.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("hello");
        bigText.setBigContentTitle("Today's Bible Verse");
        bigText.setSummaryText("Text in detail");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Your Title");
        mBuilder.setContentText("Your text");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_001",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}
