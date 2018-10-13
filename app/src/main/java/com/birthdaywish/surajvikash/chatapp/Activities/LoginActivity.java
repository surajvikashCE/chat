package com.birthdaywish.surajvikash.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.birthdaywish.surajvikash.chatapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import info.hoang8f.widget.FButton;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputLayout textEmail, textPassword;
    private FButton loginBtn;
    private Toolbar mToobar;
    private ProgressDialog progressDialog;
    private DatabaseReference userDataRef;
    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textEmail = (TextInputLayout)findViewById(R.id.login_email);
        textPassword = (TextInputLayout)findViewById(R.id.login_password);
        loginBtn = (FButton) findViewById(R.id.login_btn);

        mToobar = (Toolbar) findViewById(R.id.login_appbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPref = getSharedPreferences("users", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Logging you in...");
                String email = textEmail.getEditText().getText().toString();
                String password = textPassword.getEditText().getText().toString();
                if( !TextUtils.isEmpty(email) | !TextUtils.isEmpty(password)){
                    progressDialog.show();
                    login(email, password);
                }
                else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login(final String email, final String password) {
       // progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String curr_id = mAuth.getCurrentUser().getUid();
                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                userDataRef.child(curr_id).child("tokenId").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.hide();
                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.putBoolean("isLogin", true);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        Toast.makeText(LoginActivity.this, "Something went wrong\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.hide();
                Toast.makeText(LoginActivity.this, "Something went wrong\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
