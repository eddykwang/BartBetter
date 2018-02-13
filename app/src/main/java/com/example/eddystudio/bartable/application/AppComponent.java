package com.example.eddystudio.bartable.application;

import android.content.Context;

import com.example.eddystudio.bartable.Dashboard.DashboardFragment;
import com.example.eddystudio.bartable.HomePage.HomePageRecyclerViewFragment;
import com.example.eddystudio.bartable.MainActivity;
import com.example.eddystudio.bartable.Repository.Repository;
import com.example.eddystudio.bartable.Uilts.BaseRecyclerViewAdapter;

import javax.inject.Singleton;

import dagger.Component;
import retrofit2.Retrofit;

@Component(modules = {AppContextModule.class, RetrofitModule.class})
@AppScope
@Singleton
public interface AppComponent {
    void inject(MainActivity mainActivity);
    void inject(HomePageRecyclerViewFragment homePageRecyclerViewFragment);
    void inject(DashboardFragment dashboardFragment);
    void inject(Repository repository);
    void inject(BaseRecyclerViewAdapter baseRecyclerViewAdapter);
}
