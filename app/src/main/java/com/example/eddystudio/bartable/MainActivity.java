package com.example.eddystudio.bartable;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.eddystudio.bartable.Dashboard.DashboardFragment;
import com.example.eddystudio.bartable.HomePage.HomePageRecyclerViewFragment;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final HomePageRecyclerViewFragment homePageRecyclerViewFragment = new HomePageRecyclerViewFragment();
    private final DashboardFragment dashboardFragment = new DashboardFragment();
    public static SharedPreferences preference;
    public static Set<String> dashboardRouts;
    public final static String DASHBOARDROUTS = "dashboardRouts";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame_layout, homePageRecyclerViewFragment, homePageRecyclerViewFragment.getClass().getSimpleName()).commit();
                    return true;
                case R.id.navigation_dashboard:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame_layout,dashboardFragment,dashboardFragment.getClass().getSimpleName()).commit();
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        preference = PreferenceManager.getDefaultSharedPreferences(this);
        dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, homePageRecyclerViewFragment, homePageRecyclerViewFragment.getClass().getSimpleName()).commit();
        requestInternetPermission();
    }

    private void requestInternetPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                   // Snackbar.make(findViewById(R.id.main_frame_layout),"You need I/O Permission",Snackbar.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
