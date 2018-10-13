package com.birthdaywish.surajvikash.chatapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.birthdaywish.surajvikash.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class EntryPage extends AppCompatActivity {

    final String TAG = "EntryPage Activity ";
    private Button loginBtn, newAccountBtn;
    private Toolbar mToobar;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);

        loginBtn = (Button) findViewById(R.id.entry_login_btn);
        newAccountBtn = (Button) findViewById(R.id.entry_newaccount_btn);
        mToobar = (Toolbar) findViewById(R.id.entry_page_appbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("Chat App");

        mAuth = FirebaseAuth.getInstance();

        /*mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };*/

        /*try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {

            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
*/


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EntryPage.this, LoginActivity.class));
                //finish();
            }
        });

        newAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EntryPage.this, SignUpActivity.class));
                //finish();
            }
        });

    }

    @Override
    protected void onStart() {

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() != null){
                    startActivity(new Intent(EntryPage.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
