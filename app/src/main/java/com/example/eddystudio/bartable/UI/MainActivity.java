package com.example.eddystudio.bartable.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.DI.Application;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
  private final DiscoverFragment discoverFragment =
      new DiscoverFragment();
  private final DashboardFragment dashboardFragment = new DashboardFragment();
  private final NotificationFragment notificationFragment = new NotificationFragment();
  @Inject
  public SharedPreferences preference;
  public static Set<String> dashboardRouts;
  public final static String DASHBOARDROUTS = "dashboardRouts";
  public static final String BUDDLE_ARG_FROM = "Buddle_Arg_From";
  public static final String BUDDLE_ARG_TO = "Buddle_Arg_To";

  private Fragment fragment;
  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
      = item -> {
    switch (item.getItemId()) {
      case R.id.navigation_home:
        fragment = discoverFragment;
        getSupportActionBar().setTitle("Discover");

        break;
      case R.id.navigation_dashboard:
        fragment = dashboardFragment;
        getSupportActionBar().setTitle("My Routes");

        break;
      case R.id.navigation_notifications:
        fragment = notificationFragment;
        getSupportActionBar().setTitle("Notification");

        break;
    }
    commitToNewFragment(fragment);
    return true;
  };

  private void commitToNewFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_frame_layout, fragment, fragment.getClass().getSimpleName()).commit();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

    Application.getAppComponet().inject(this);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    //AppBarLayout appBarLayout = findViewById(R.id.app_bar);
    //CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
    //collapsingToolbarLayout.setTitle("");
    //collapsingToolbarLayout.setTitleEnabled(true);
    //appBarLayout.setExpanded(false,false);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    navigation.setSelectedItemId(R.id.navigation_dashboard);
  }

  @Override
  protected void onStart() {
    super.onStart();
    dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());
  }
}
