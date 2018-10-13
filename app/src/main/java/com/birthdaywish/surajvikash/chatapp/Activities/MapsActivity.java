package com.birthdaywish.surajvikash.chatapp.Activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.birthdaywish.surajvikash.chatapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    int i = 0;
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("location");
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float lat = Float.parseFloat(dataSnapshot.child("latitude").getValue().toString().trim());
                float longi = Float.parseFloat(dataSnapshot.child("longitude").getValue().toString().trim());
                LatLng pos = new LatLng(lat, longi);
                //GoogleMap mMap = googleMap;
                googleMap.addMarker(new MarkerOptions().position(pos).title("Current Location"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(pos));

                if(i<2)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 22.0f));
                i++;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        // Add a marker in Sydney and move the camera
    }
}
