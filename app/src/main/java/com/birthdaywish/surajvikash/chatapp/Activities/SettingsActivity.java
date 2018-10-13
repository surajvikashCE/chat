package com.birthdaywish.surajvikash.chatapp.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private final static int IMAGE_CODE = 100;
    private final static int READ_STORAGE = 101;

    private FirebaseUser user;
    private DatabaseReference mDataRef;
    private StorageReference mStorageRef;
    private SimpleDraweeView imageView;
    private TextView mName, mStatus;
    private String status;
    byte[] thumbByte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fresco.initialize(this);
        setContentView(R.layout.activity_settings);

        imageView = findViewById(R.id.settings_image);
        mName = findViewById(R.id.settings_display_name);
        mStatus = findViewById(R.id.settings_status);
        Button mChangeStatus = findViewById(R.id.settings_change_status);
        Button mChangeImage = findViewById(R.id.settings_change_image);

        Toolbar mToobar = findViewById(R.id.settings_activity_appbar);
        //hello();
        setSupportActionBar(mToobar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ProgressDialog pd1 = new ProgressDialog(SettingsActivity.this);
        pd1.setMessage("Please wait...");
        pd1.setCanceledOnTouchOutside(false);
        pd1.show();

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String curr_id = user.getUid();

        mDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(curr_id);
        mDataRef.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(SettingsActivity.this, ""+dataSnapshot, Toast.LENGTH_SHORT).show();

                String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                //String thumb_image = Objects.requireNonNull(dataSnapshot.child("thumb_image").getValue()).toString();

                mName.setText(name);
                mStatus.setText(status);
                if (!image.equals("default") || !image.contains("default")) {
                    imageView.setImageURI(image);
                }
                pd1.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error 1 -->", databaseError.getMessage());
                Toast.makeText(SettingsActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                pd1.dismiss();
            }
        });

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("change ", " started");

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Change Status");
                builder.setCancelable(false);
                final EditText editText = new EditText(SettingsActivity.this);
                editText.setText(mStatus.getText());

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                editText.setLayoutParams(lp);
                builder.setView(editText);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        status = editText.getText().toString();
                        //if(status.compareTo("") == 0) {
                        final ProgressDialog pd2 = new ProgressDialog(SettingsActivity.this);
                        pd2.setMessage("Updating status...");
                        pd2.setCanceledOnTouchOutside(false);
                        pd2.show();
                        mDataRef.child("status").setValue(status).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //updateStatusCard.setVisibility(View.GONE);
                                pd2.dismiss();
                                Toast.makeText(SettingsActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error 2 -->", e.getMessage());
                                pd2.dismiss();
                                Toast.makeText(SettingsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });
        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check_permission();

            }
        });
    }

    private void check_permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadimage();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadimage();
        } else {
            Toast.makeText(this, "Permission_Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadimage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK) {
            Log.e("ResultURI-->", "Gotit");
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(SettingsActivity.this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.e("ResultURI-->", "got after crop");

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                uploadImage(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("Change Image crop-->", error.getMessage());
            }
        } else {
            Log.e("ResultURI-->", "Error");
        }
    }

    private void uploadImage(final Uri resultUri) {
        final ProgressDialog pd3 = new ProgressDialog(SettingsActivity.this);
        pd3.setMessage("Uploading image...");
        pd3.setCanceledOnTouchOutside(false);
        pd3.show();

        File thumb_file = new File(resultUri.getPath());
        String curr_id = user.getUid();

        try {
            Bitmap thumb_bitmap = new Compressor(this)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .compressToBitmap(thumb_file);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            thumbByte = outputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }


        StorageReference filePath = mStorageRef.child("ProfileImages").child(mName.getText() + curr_id + ".jpg");
        final StorageReference thumbFilePath = mStorageRef.child("ProfileImages").child("thumbs").child(mName.getText() + curr_id + ".jpg");


        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    Log.e("upload-->", "success");
                    final String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

                    UploadTask uploadTask = thumbFilePath.putBytes(thumbByte);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {
                            if (thumbTask.isSuccessful()) {

                                String thumbDownloadUrl = Objects.requireNonNull(thumbTask.getResult().getDownloadUrl()).toString();

                                Map<String, Object> map = new HashMap<>();
                                map.put("image", downloadUrl);
                                map.put("thumb_image", thumbDownloadUrl);

                                mDataRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        imageView.setImageURI(resultUri);
                                        Toast.makeText(SettingsActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                        pd3.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error 3 -->", e.getMessage());
                                        pd3.dismiss();
                                    }
                                });

                            } else {
                                Log.e("Error 4 -->", Objects.requireNonNull(thumbTask.getException()).getMessage());
                                Toast.makeText(SettingsActivity.this, "Error uploading thumb image", Toast.LENGTH_SHORT).show();
                                pd3.dismiss();
                            }
                        }
                    });
                } else {
                    Log.e("Error 4 -->", Objects.requireNonNull(task.getException()).getMessage());
                    Toast.makeText(SettingsActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    pd3.dismiss();
                }
            }
        });/*.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
               // pd3.show();
                Log.e("Progress", "Uploaded "+(int)progress+"%");
                pd3.setMessage("Uploaded "+(int)progress+"%");
            }
        });*/

    }

    private void hello1() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        @SuppressLint("HardwareIds")
        String str = ((TelephonyManager) Objects.requireNonNull(getSystemService(Context.TELEPHONY_SERVICE))).getDeviceId();
        Log.e("dfghj : ", str);
        String url = "http://www.live365sports.com/webclient/";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response : ", ": "+ response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error ", ": "+ error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("username", "92505");
                map.put("password", "Davinder");
                map.put("uuid", "7aca11f8-f404-35a7-93bc-8c8878631e7f");
                map.put("1", "1");
                map.put("device", "android");
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDataRef.child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDataRef.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
