package com.eddystudio.bartbetter.UI;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.eddystudio.bartbetter.Model.Repository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.eddystudio.bartbetter.UI.MainActivity.DASHBOARDROUTS;


public class BaseFragment extends Fragment {
    @Inject
    public Repository repository;
    @Inject
    public SharedPreferences preference;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onDestroy() {
        if (compositeDisposable != null && compositeDisposable.isDisposed()){
            compositeDisposable.clear();
        }
        super.onDestroy();
    }

    protected void addDisposable(Disposable disposable){
        if (compositeDisposable == null){
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    protected List<String> getSharedPreferencesData(){
        Type type = new TypeToken<List<String>>(){}.getType();
        Gson gson = new Gson();
        List<String> empty = new ArrayList<>();
        String emptyList = gson.toJson(empty);
        String json = preference.getString(DASHBOARDROUTS, emptyList);
        List<String> routeList = gson.fromJson(json, type);
        return routeList == null ? new ArrayList<>() : routeList;
    }

    protected void saveSharedPreferenceData(List<String> routes){
      SharedPreferences.Editor prefsEditor = preference.edit();
      Gson gson = new Gson();
      String json = gson.toJson(routes);
      prefsEditor.putString(DASHBOARDROUTS, json);
      prefsEditor.apply();
    }

    protected void addPreferencesData(String rout){
        List<String> routeList = getSharedPreferencesData();
        routeList.add(rout);
        SharedPreferences.Editor prefsEditor = preference.edit();
        Gson gson = new Gson();
        String json = gson.toJson(routeList);
        prefsEditor.putString(DASHBOARDROUTS, json);
        prefsEditor.apply();
    }

    protected void deletePreferencesData(int position){
        List<String> routeList = getSharedPreferencesData();
        routeList.remove(position);
        SharedPreferences.Editor prefsEditor = preference.edit();
        Gson gson = new Gson();
        String json = gson.toJson(routeList);
        prefsEditor.putString(DASHBOARDROUTS, json);
        prefsEditor.apply();
    }
}
