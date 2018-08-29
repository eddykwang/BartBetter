package com.eddystudio.bartbetter.UI;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.Stations.Station;
import com.eddystudio.bartbetter.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
  private final QuickLookupFragment quickLookupFragment =
      new QuickLookupFragment();
  private final DashboardFragment dashboardFragment = new DashboardFragment();
  private final NotificationFragment notificationFragment = new NotificationFragment();

  @Inject
  public Repository repository;

  public final static String DASHBOARDROUTS = "dashboardRouts";
  public static final String BUDDLE_ARG_FROM = "Buddle_Arg_From";
  public static final String BUDDLE_ARG_TO = "Buddle_Arg_To";
  public static final String AUTO_REFRESH_ENABLED = "auto_refresh_enabled";

  public static final String STATION_INFO_LIST = "station_info_list";

  //  public static Map<String, String> stationShotcutMapper = new HashMap<>();
  public static ArrayList<String> stationList = new ArrayList<>();
  public static ArrayList<String> stationListSortcut = new ArrayList<>();
  public static List<Station> stationInfoList = new ArrayList<>();

  private Fragment fragment;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
      = item -> {
    switch(item.getItemId()) {
      case R.id.navigation_quick_lookup:
        fragment = quickLookupFragment;

        break;
      case R.id.navigation_my_routes:
        fragment = dashboardFragment;

        break;
      case R.id.navigation_notifications:
        fragment = notificationFragment;

        break;
    }
    commitToNewFragment(fragment);
    return true;
  };

  private void commitToNewFragment(Fragment fragment) {
    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.main_frame_layout, fragment, fragment.getClass().getSimpleName())
        .commit();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    BottomNavigationView navigation = findViewById(R.id.navigation);

    Application.getAppComponet().inject(this);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    navigation.setSelectedItemId(R.id.navigation_my_routes);
    getAllStations();

    setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
    getWindow().setStatusBarColor(Color.TRANSPARENT);

  }

  public static void setWindowFlag(Activity activity, final int bits, boolean on) {
    Window win = activity.getWindow();
    WindowManager.LayoutParams winParams = win.getAttributes();
    if(on) {
      winParams.flags |= bits;
    } else {
      winParams.flags &= ~bits;
    }
    win.setAttributes(winParams);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private void getAllStations() {
    Type type = new TypeToken<List<Station>>() {}.getType();
    Gson gson = new Gson();
    List<Station> empty = new ArrayList<>();
    String emptyList = gson.toJson(empty);
    String mapper = PreferenceManager.getDefaultSharedPreferences(this).getString(STATION_INFO_LIST, emptyList);
    stationInfoList = gson.fromJson(mapper, type);

    if(stationInfoList.isEmpty()) {
      getAllStationsFromApi();
    } else {
      if(stationList.isEmpty() || stationListSortcut.isEmpty()) {
        for(Station station : stationInfoList) {
          stationList.add(station.getName());
          stationListSortcut.add(station.getAbbr());
        }
      }
    }
  }

  private void getAllStationsFromApi() {
    compositeDisposable.add(repository.getStations()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(station -> station.getRoot().getStations().getStation())
        .subscribe(stations -> {
          stationInfoList = stations;

          for(Station station : stationInfoList) {
            stationList.add(station.getName());
            stationListSortcut.add(station.getAbbr());
          }

          SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
          Gson gson = new Gson();
          String stationInfoListStr = gson.toJson(stationInfoList);
          prefsEditor.putString(STATION_INFO_LIST, stationInfoListStr);
          prefsEditor.apply();
        }));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    compositeDisposable.clear();
  }
}
