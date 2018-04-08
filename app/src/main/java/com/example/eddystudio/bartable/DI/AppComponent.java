package com.example.eddystudio.bartable.DI;

import com.example.eddystudio.bartable.UI.DashboardFragment;
import com.example.eddystudio.bartable.UI.HomePageRecyclerViewFragment;
import com.example.eddystudio.bartable.MainActivity;
import com.example.eddystudio.bartable.UI.NotificationFragment;
import com.example.eddystudio.bartable.Model.Repository;
import com.example.eddystudio.bartable.Adapter.BaseRecyclerViewAdapter;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppContextModule.class, RetrofitModule.class})
@AppScope
@Singleton
public interface AppComponent {
    void inject(MainActivity mainActivity);
    void inject(HomePageRecyclerViewFragment homePageRecyclerViewFragment);
    void inject(DashboardFragment dashboardFragment);
    void inject(Repository repository);
    void inject(BaseRecyclerViewAdapter baseRecyclerViewAdapter);
    void inject(NotificationFragment notificationFragment);
}
