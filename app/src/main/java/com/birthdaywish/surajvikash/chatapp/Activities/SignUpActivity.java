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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout textDisplayName, textEmail, textPassword;
    private Button createAccountBtn;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private Toolbar mToobar;
    private DatabaseReference mDataRef;
    private SharedPreferences userPref;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textDisplayName = (TextInputLayout)findViewById(R.id.signup_display_name);
        textEmail = (TextInputLayout)findViewById(R.id.signup_email);
        textPassword = (TextInputLayout)findViewById(R.id.signup_password);
        createAccountBtn = (Button)findViewById(R.id.signup_create_account_btn);

        mToobar = (Toolbar) findViewById(R.id.signup_activity_appbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("Sign UP");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPref = getSharedPreferences("users", MODE_PRIVATE);

        isLogin = userPref.getBoolean("isLogin", false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Creating new account...");

        mAuth = FirebaseAuth.getInstance();

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName, email, password;
                displayName = textDisplayName.getEditText().getText().toString();
                email = textEmail.getEditText().getText().toString();
                password = textPassword.getEditText().getText().toString();
                if(!TextUtils.isEmpty(displayName) | !TextUtils.isEmpty(email) | !TextUtils.isEmpty(password)){
                    signUp(displayName, email, password);
                }
                else {
                    Toast.makeText(SignUpActivity.this, "Kindly fill all the details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signUp(final String displayName, final String email, final String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser curr_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = curr_user.getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", displayName);
                    map.put("tokenId", deviceToken);
                    map.put("status", "Hi there, I am using Chat app");
                    map.put("image", "default");
                    map.put("thumb_image", "default");

                    mDataRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            //Snackbar.make(this, "Account Created", 1);
                            SharedPreferences.Editor editor = userPref.edit();
                            editor.putBoolean("isLogin", true);
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.apply();
                            Toast.makeText(SignUpActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            mAuth.signOut();
                            userPref.edit().clear().apply();

                            Toast.makeText(SignUpActivity.this, "Database error\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.hide();
                    Toast.makeText(SignUpActivity.this, "Something went wrong\nPlease try again\n" +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}