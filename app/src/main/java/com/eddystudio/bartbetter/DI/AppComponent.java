package com.eddystudio.bartbetter.DI;

import com.eddystudio.bartbetter.Adapter.BaseRecyclerViewAdapter;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.UI.DashboardFragment;
import com.eddystudio.bartbetter.UI.MainActivity;
import com.eddystudio.bartbetter.UI.NotificationFragment;
import com.eddystudio.bartbetter.UI.QuickLookupFragment;
import com.eddystudio.bartbetter.UI.RouteDetailFragment;
import com.eddystudio.bartbetter.ViewModel.DashboardRecyclerViewItemVM;
import com.eddystudio.bartbetter.ViewModel.DashboardViewModel;
import com.eddystudio.bartbetter.ViewModel.QuickLookupViewModel;

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

  void inject(QuickLookupViewModel quickLookupViewModel);

  void inject(DashboardViewModel dashboardViewModel);

}
