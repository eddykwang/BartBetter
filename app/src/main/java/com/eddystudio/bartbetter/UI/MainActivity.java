package com.eddystudio.bartbetter.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final QuickLookupFragment quickLookupFragment =
            new QuickLookupFragment();
    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final NotificationFragment notificationFragment = new NotificationFragment();
    @Inject
    public SharedPreferences preference;

    @Inject
    public Repository repository;

    public final static String DASHBOARDROUTS = "dashboardRouts";
    public static final String BUDDLE_ARG_FROM = "Buddle_Arg_From";
    public static final String BUDDLE_ARG_TO = "Buddle_Arg_To";

    public static ArrayList<String> stationList = new ArrayList<>();
    public static ArrayList<String> stationListSortcut = new ArrayList<>();

    private Fragment fragment;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_quick_lookup:
                fragment = quickLookupFragment;
                getSupportActionBar().setTitle(R.string.title_quick_look_up);

                break;
            case R.id.navigation_my_routes:
                fragment = dashboardFragment;
                getSupportActionBar().setTitle(R.string.title_my_routes);

                break;
            case R.id.navigation_notifications:
                fragment = notificationFragment;
                getSupportActionBar().setTitle(R.string.title_notifications);

                break;
        }
        commitToNewFragment(fragment);
        return true;
    };

    private void commitToNewFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

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
        navigation.setSelectedItemId(R.id.navigation_my_routes);
        getAllStations();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllStations() {
        compositeDisposable.add(repository.getStations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(station -> station.getRoot().getStations().getStation())
                .concatMap(Observable::fromArray)
                .subscribe(stations -> {
                    for (int i = 0; i < stations.size(); ++i) {
                        stationList.add(stations.get(i).getName());
                        stationListSortcut.add(stations.get(i).getAbbr());
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }
}
