package com.birthdaywish.surajvikash.chatapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.birthdaywish.surajvikash.chatapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences userPref;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    private TextView textView;
    private Button retryButton;
    private ImageView errorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = (ProgressBar)findViewById(R.id.splash_progress_bar);
        textView = (TextView)findViewById(R.id.splash_textview);
        retryButton = (Button)findViewById(R.id.splash_retry);
        errorImage = (ImageView)findViewById(R.id.error_image);
        progressBar.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);
        errorImage.setVisibility(View.INVISIBLE);

        userPref = getSharedPreferences("users", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        boolean isLogin;
        final String email, password;

        isLogin = userPref.getBoolean("isLogin", false);
        email = userPref.getString("email", "");
        password = userPref.getString("password","");

        Log.e("df : ",email+password);

        if(isLogin){
            login(email, password);
        }
        else{
            startActivity(new Intent(this, EntryPage.class));
            finish();
        }

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(email, password);
            }
        });
    }

    private void login(String email, String password) {

        textView.setText("Loading, please wait...");
        retryButton.setVisibility(View.GONE);
        errorImage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressBar.setVisibility(View.INVISIBLE);
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                textView.setText("Oops...\nSomething went wrong\nTry again");
                retryButton.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                Log.e("Login Error : ", e.getMessage());
                //Toast.makeText(SplashActivity.this, "Network error, try again", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
