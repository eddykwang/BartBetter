package com.example.eddystudio.bartable.DI;

import com.example.eddystudio.bartable.UI.DashboardFragment;
import com.example.eddystudio.bartable.UI.QuickLookupFragment;
import com.example.eddystudio.bartable.UI.MainActivity;
import com.example.eddystudio.bartable.UI.NotificationFragment;
import com.example.eddystudio.bartable.Model.Repository;
import com.example.eddystudio.bartable.Adapter.BaseRecyclerViewAdapter;
import com.example.eddystudio.bartable.UI.RouteDetailFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppContextModule.class, RetrofitModule.class})
@AppScope
@Singleton
public interface AppComponent {
    void inject(MainActivity mainActivity);
    void inject(QuickLookupFragment quickLookupFragment);
    void inject(DashboardFragment dashboardFragment);
    void inject(Repository repository);
    void inject(BaseRecyclerViewAdapter baseRecyclerViewAdapter);
    void inject(NotificationFragment notificationFragment);
    void inject(RouteDetailFragment routeDetailFragment);
}
