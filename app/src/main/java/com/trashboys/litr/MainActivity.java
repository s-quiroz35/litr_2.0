package com.trashboys.litr;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActionBar toolbar;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private ArrayList<QueryDocumentSnapshot> litterlist = new ArrayList<>();
    private ArrayList<LatLng> locations = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_user:
                    toolbar.setTitle("User Info");
                    fragment = new UserInfoFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_camera:
                    toolbar.setTitle("Litter Cam");
                    fragment = new CameraFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_map:
                    toolbar.setTitle("Litter Map");
                    loadFragment(mapFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        db.collection("litter")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.get("geolocation") != null){
                                    GeoPoint loc = (GeoPoint) document.get("geolocation");
                                    LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.
                                            defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title("Trash"));
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(33.7773, -84.3962);

        mMap.addMarker(new MarkerOptions().position(location).title("Current Location"));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

//        mMap.addMarker(new MarkerOptions().position(location).title("Litter"));
//        mMap.addMarker(new MarkerOptions().position(location).title("Litter"));
//        mMap.addMarker(new MarkerOptions().position(location).title("Litter"));
//        mMap.addMarker(new MarkerOptions().position(location).title("Litter"));
//        mMap.addMarker(new MarkerOptions().position(location).title("Litter"));


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = getSupportActionBar();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        toolbar.setTitle("User Info");
        mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        loadFragment(new UserInfoFragment());
    }


    private void loadFragment(Fragment fragment) {
        // load fragment
        if (getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof CameraFragment && fragment instanceof CameraFragment ||
                getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof UserInfoFragment && fragment instanceof UserInfoFragment ||
                getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof SupportMapFragment && fragment instanceof SupportMapFragment) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
