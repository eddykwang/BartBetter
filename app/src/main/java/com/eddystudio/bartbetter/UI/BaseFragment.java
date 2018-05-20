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

import static com.eddystudio.bartbetter.UI.MainActivity.DASHBOARDROUTS;


public class BaseFragment extends Fragment {
    @Inject
    public Repository repository;
    @Inject
    public SharedPreferences preference;

    protected List<String> getSharedPreferencesData(){
        Type type = new TypeToken<List<String>>(){}.getType();
        Gson gson = new Gson();
        List<String> empty = new ArrayList<>();
        String emptyList = gson.toJson(empty);
        String json = preference.getString(DASHBOARDROUTS, emptyList);
        List<String> routeList = gson.fromJson(json, type);
        return routeList == null ? new ArrayList<>() : routeList;
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
