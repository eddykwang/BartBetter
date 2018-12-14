package com.eddystudio.bartbetter.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.util.concurrent.TimeUnit;

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

  private View badgeView;
  private BottomNavigationItemView bottoMNavigationItemView;
  private BottomNavigationView navigation;
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
        bottoMNavigationItemView.removeView(badgeView);
        break;
    }
    commitToNewFragment(fragment);
    return true;
  };

  private void commitToNewFragment(Fragment fragment) {
    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(R.anim.fragment_trans_anim, android.R.anim.fade_out)
        .replace(R.id.main_frame_layout, fragment, fragment.getClass().getSimpleName())
        .commit();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    navigation = findViewById(R.id.navigation);

    Application.getAppComponet().inject(this);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    if(savedInstanceState == null) {
      getAllStations();
      setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    BottomNavigationMenuView bottomNavigationItemView = (BottomNavigationMenuView) navigation.getChildAt(0);
    bottoMNavigationItemView = (BottomNavigationItemView) bottomNavigationItemView.getChildAt(2);
    badgeView = LayoutInflater.from(this).inflate(R.layout.notification_badge_layout, bottomNavigationItemView, false);
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
    navigation.postDelayed(this::getDelayNotification, 2000);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private void getDelayNotification() {
    compositeDisposable.add(
        repository.getDelayReport()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(delayReport -> {
              String delayStation = delayReport.getRoot().getBsa().get(0).getStation();
              if(!delayStation.contains("")) {
                bottoMNavigationItemView.addView(badgeView);
              } else {
                bottoMNavigationItemView.removeView(badgeView);
              }
            }));
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
      navigation.setSelectedItemId(R.id.navigation_my_routes);
    }
  }

  private void getAllStationsFromApi() {
    View mView = getLayoutInflater().inflate(R.layout.loading_dialog_layout, null);
    AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setView(mView)
        .setTitle("Loading")
        .setCancelable(false)
        .create();

    alertDialog.show();

    AlertDialog errorDialog = new AlertDialog.Builder(this)
        .setTitle("Error")
        .setMessage("Cannot get data, please make sure you have Internet connection.")
        .setCancelable(false)
        .setPositiveButton("Retry", (dialogInterface, i) -> {
          getAllStationsFromApi();
        })
        .create();

    compositeDisposable.add(repository.getStations()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .delay(500, TimeUnit.MILLISECONDS)
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
            },
            error -> {
              alertDialog.dismiss();
              errorDialog.show();
            }
            ,
            () -> {

              if(alertDialog.isShowing()) {
                alertDialog.dismiss();
              }

              if(errorDialog.isShowing()) {
                errorDialog.dismiss();
              }

              navigation.setSelectedItemId(R.id.navigation_my_routes);
            }));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    compositeDisposable.clear();
  }
}
