package com.trashboys.litr;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ActionBar toolbar;

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
                    fragment = new MapFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = getSupportActionBar();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        toolbar.setTitle("User Info");
        loadFragment(new UserInfoFragment());
    }


    private void loadFragment(Fragment fragment) {
        // load fragment
        if (getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof CameraFragment && fragment instanceof CameraFragment ||
                getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof UserInfoFragment && fragment instanceof UserInfoFragment ||
                getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof MapFragment && fragment instanceof MapFragment) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
