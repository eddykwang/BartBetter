package com.eddystudio.bartbetter.UI;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.eddystudio.bartbetter.Model.RouteModel;
import com.eddystudio.bartbetter.Model.Repository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.eddystudio.bartbetter.UI.MainActivity.DASHBOARDROUTS;


public abstract class BaseFragment extends Fragment {
  @Inject
  public Repository repository;
  @Inject
  public SharedPreferences preference;
  private CompositeDisposable compositeDisposable;
  public static final int LOCATION_REQUEST_CODE = 4344;

  @Override
  public void onDestroy() {
    if(compositeDisposable != null && compositeDisposable.isDisposed()) {
      compositeDisposable.clear();
    }
    super.onDestroy();
  }

  protected void addDisposable(Disposable disposable) {
    if(compositeDisposable == null) {
      compositeDisposable = new CompositeDisposable();
    }
    compositeDisposable.add(disposable);
  }

  protected List<String> getOldSharedPreferencesData() {
    Type type = new TypeToken<List<String>>() {}.getType();
    Gson gson = new Gson();
    List<String> empty = new ArrayList<>();
    String emptyList = gson.toJson(empty);
    String json = preference.getString("dashboardRouts", emptyList);
    List<String> routeList = gson.fromJson(json, type);
    return routeList == null ? new ArrayList<>() : routeList;
  }

  protected List<RouteModel> getSharedPreferencesData() {
    Type type = new TypeToken<List<RouteModel>>() {}.getType();
    Gson gson = new Gson();
    List<RouteModel> empty = new ArrayList<>();
    String emptyList = gson.toJson(empty);
    String json = preference.getString(DASHBOARDROUTS, emptyList);
    List<RouteModel> routeList = gson.fromJson(json, type);
    return routeList == null ? new ArrayList<>() : routeList;
  }

  protected void saveSharedPreferenceData(List<RouteModel> routes) {
    SharedPreferences.Editor prefsEditor = preference.edit();
    Gson gson = new Gson();
    String json = gson.toJson(routes);
    prefsEditor.putString(DASHBOARDROUTS, json);
    prefsEditor.apply();
  }

  protected void addPreferencesData(RouteModel rout) {
    List<RouteModel> routeList = getSharedPreferencesData();
    routeList.add(rout);
    SharedPreferences.Editor prefsEditor = preference.edit();
    Gson gson = new Gson();
    String json = gson.toJson(routeList);
    prefsEditor.putString(DASHBOARDROUTS, json);
    prefsEditor.apply();
  }

  protected void deletePreferencesData(int position) {
    List<RouteModel> routeList = getSharedPreferencesData();
    routeList.remove(position);
    SharedPreferences.Editor prefsEditor = preference.edit();
    Gson gson = new Gson();
    String json = gson.toJson(routeList);
    prefsEditor.putString(DASHBOARDROUTS, json);
    prefsEditor.apply();
  }

  protected boolean deleteRouteInDashBoard(RouteModel route) {
    List<RouteModel> routeList = getSharedPreferencesData();
    boolean result = routeList.remove(route);

    SharedPreferences.Editor prefsEditor = preference.edit();
    Gson gson = new Gson();
    String json = gson.toJson(routeList);
    prefsEditor.putString(DASHBOARDROUTS, json);
    prefsEditor.apply();

    return result;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch(requestCode) {
      case LOCATION_REQUEST_CODE:
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          setUpGetCurrentLocation();
        } else {
          AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity())).create();
          alertDialog.setTitle("Failed");
          alertDialog.setMessage("You need to grant permission to find the closest station.");
          alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Close",
              (dialog, which) -> dialog.dismiss());
          alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Request",
              ((dialog, which) -> setUpGetCurrentLocation()));
          alertDialog.show();
        }

    }
  }

  public void setUpGetCurrentLocation() {
    if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(
          new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION},
          LOCATION_REQUEST_CODE);

    }else {
      locationPermissionGot();
    }
  }

  public void locationPermissionGot(){}
}
