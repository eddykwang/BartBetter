package com.eddystudio.bartbetter.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.transition.Fade;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.eddystudio.bartbetter.Adapter.CardSwipeController;
import com.eddystudio.bartbetter.Adapter.DashboardRecyclerViewAdapter;
import com.eddystudio.bartbetter.Adapter.SwipeControllerActions;
import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.RouteModel;
import com.eddystudio.bartbetter.Model.Response.Stations.Station;
import com.eddystudio.bartbetter.Model.Uilt;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.ViewModel.DashboardRecyclerViewItemVM;
import com.eddystudio.bartbetter.ViewModel.DashboardViewModel;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.toptas.fancyshowcase.DismissListener;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

import static com.eddystudio.bartbetter.UI.MainActivity.AUTO_REFRESH_ENABLED;
import static com.eddystudio.bartbetter.UI.MainActivity.IS_USING_DISTANCE_TO_SORT;
import static com.eddystudio.bartbetter.UI.MainActivity.stationInfoList;

public class DashboardFragment extends BaseFragment {

  private FragmentDashboardBinding binding;
  private DashboardRecyclerViewAdapter adapter;
  private DashboardViewModel vm;
  private List<RouteModel> dashboardRouteList = new ArrayList<>();
  private List<Station> dashboardOriginStationList = new ArrayList<>();
  private SwitchCompat switchCompat;
  private Pair<DashboardRecyclerViewItemVM, Integer> lastDeletedItem = null;
  private RouteModel lastDeletedRouteString = null;
  private boolean isLocationLoading = false;
  private boolean isDataLoading = false;

  public DashboardFragment() {
    // Required empty public constructor
    setAllowEnterTransitionOverlap(false);
    setAllowReturnTransitionOverlap(false);
    setEnterTransition(new Fade());
    setExitTransition(new Fade());
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    binding = FragmentDashboardBinding.inflate(inflater, container, false);
    Toolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    getActivity().setTitle("My Routes");
    vm = new DashboardViewModel();
//    vm = ViewModelProviders.of(this).get(DashboardViewModel.class);
    Application.getAppComponet().inject(this);

    AppBarLayout appBarLayout = binding.appbarLayout;
    appBarLayout.postDelayed(() -> appBarLayout.addOnOffsetChangedListener((appbarLayout, offset) -> {
      showCaseSetup();
      int scrollRange = appBarLayout.getTotalScrollRange();
      if(scrollRange + offset == 0) {
        switchCompat.animate().translationX(0);
      } else {
        switchCompat.animate().translationX(switchCompat.getWidth() + 20);
      }
    }), 500);

    loadOldDataToNewData();
    init();

    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    binding.getRoot().postDelayed(this::loadFromPreference
        , 300);
    setupRadioGroup();
  }

  private void init() {
    vm.getEventsSubject()
        .observeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(event -> Observable.merge(
            event.ofType(Events.LoadingEvent.class).doOnNext(data -> {
              binding.swipeRefreshLy.setRefreshing(data.isLoad() || isLocationLoading);
              isDataLoading = data.isLoad();
              if(getActivity() != null && binding.recylerView.findViewHolderForAdapterPosition(0) != null) {
                new FancyShowCaseView.Builder(getActivity())
                    .focusOn(binding.recylerView.findViewHolderForAdapterPosition(0).itemView)
                    .title("Tap to see more schedules,\nswipe left to deleted,\npress and drag to rearrange position. ")
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .showOnce("recyclerview_item_showcase")
                    .delay(500)
                    .build()
                    .show();
              }
            }),
            event.ofType(Events.ErrorEvent.class).doOnNext(error -> handleError(error.getError())),
            event.ofType(Events.GoToDetailEvent.class).doOnNext(data -> goToDetail(data.getFrom(), data.getTo(), data.getRouteColor(), data.getView())),
            event.ofType(Events.GetDataEvent.class).doOnNext(data -> adapter.modifyData((DashboardRecyclerViewItemVM) data.getData()))
        ))
        .subscribe();

    setUpAdapter();
    setupFab();
    setupSwitch();
    binding.swipeRefreshLy.setOnRefreshListener(this::loadFromPreference);
    attachOnCardSwipe();
  }

  private void loadOldDataToNewData() {
    List<RouteModel> list = getSharedPreferencesData();

    if(list.isEmpty()) {
      List<String> oldRouteList = getOldSharedPreferencesData();
      if(!oldRouteList.isEmpty()) {
        for(String route : oldRouteList) {
          String fromStation = route.split("-", 2)[0];
          String toStation = route.split("-", 2)[1];

          Station from = null;
          Station to = null;
          for(Station station : stationInfoList) {
            if(station.getAbbr().equals(fromStation)) {
              from = station;
            }

            if(station.getAbbr().equals(toStation)) {
              to = station;
            }
          }
          if(from != null && to != null) {
            list.add(new RouteModel(from, to));
          }
        }
      }
      saveSharedPreferenceData(list);
    }
  }

  private void setupRadioGroup() {
    boolean isUsingDist = preference.getBoolean(IS_USING_DISTANCE_TO_SORT, false);
    binding.distRbt.setChecked(isUsingDist);
    binding.manualRbt.setChecked(!isUsingDist);
    if(isUsingDist) {
      isLocationLoading = true;
      binding.swipeRefreshLy.setRefreshing(true);
      setUpGetCurrentLocation();
    }
    binding.distRbt.setOnClickListener(v -> {
      binding.distRbt.setChecked(true);
      binding.manualRbt.setChecked(false);
      isLocationLoading = true;
      binding.swipeRefreshLy.setRefreshing(true);
      SharedPreferences.Editor prefsEditor = preference.edit();
      prefsEditor.putBoolean(IS_USING_DISTANCE_TO_SORT, true);
      prefsEditor.apply();
      setUpGetCurrentLocation();
    });

    binding.manualRbt.setOnClickListener(v -> {
      binding.manualRbt.setChecked(true);
      binding.distRbt.setChecked(false);
      SharedPreferences.Editor prefsEditor = preference.edit();
      prefsEditor.putBoolean(IS_USING_DISTANCE_TO_SORT, false);
      prefsEditor.apply();
    });
  }

  @SuppressLint("MissingPermission")
  @Override
  public void locationPermissionGot() {
    LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);

    dashboardOriginStationList.clear();
    for(RouteModel pair : dashboardRouteList) {
      for(Station station : stationInfoList) {
        if(pair.getFrom().getAbbr().equalsIgnoreCase(station.getAbbr())) {
          dashboardOriginStationList.add(station);
        }
      }
    }

    if(locationManager != null) {

      LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

          Comparator<Station> locationComparator = (o1, o2) -> {
            Location location1 = new Location("one");
            location1.setLatitude(Double.parseDouble(o1.getGtfsLatitude()));
            location1.setLongitude(Double.parseDouble(o1.getGtfsLongitude()));

            Location location2 = new Location("two");
            location2.setLatitude(Double.parseDouble(o2.getGtfsLatitude()));
            location2.setLongitude(Double.parseDouble(o2.getGtfsLongitude()));

            float dist1 = location.distanceTo(location1);
            float dist2 = location.distanceTo(location2);
            return Float.compare(dist1, dist2);
          };

          Collections.sort(dashboardOriginStationList, locationComparator);

          for(int i = 0; i < dashboardOriginStationList.size() - 1; ++i) {
            for(int j = 0; j < adapter.getItemList().size(); ++j) {
              if(dashboardOriginStationList.get(i).getAbbr().equalsIgnoreCase(adapter.getItemList().get(j).getFrom())) {
                adapter.swapDataPos(j, i);
                Collections.swap(dashboardRouteList, j, i);
              }
            }
          }
          Collections.sort(dashboardRouteList, (r1, r2) -> {
            Location location1 = new Location("one");
            location1.setLatitude(Double.parseDouble(r1.getFrom().getGtfsLatitude()));
            location1.setLongitude(Double.parseDouble(r1.getFrom().getGtfsLongitude()));

            Location location2 = new Location("two");
            location2.setLatitude(Double.parseDouble(r2.getFrom().getGtfsLatitude()));
            location2.setLongitude(Double.parseDouble(r2.getFrom().getGtfsLongitude()));

            float dist1 = location.distanceTo(location1);
            float dist2 = location.distanceTo(location2);
            return Float.compare(dist1, dist2);
          });
          saveSharedPreferenceData(dashboardRouteList);
          locationManager.removeUpdates(this);
          isLocationLoading = false;
          binding.swipeRefreshLy.setRefreshing(isDataLoading || isLocationLoading);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
      };

      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10f, locationListener);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, locationListener);

    }
  }

  private void showCaseSetup() {

    DismissListener fabDismissListener = new DismissListener() {
      @Override
      public void onDismiss(String id) {
        new FancyShowCaseView.Builder(getActivity())
            .focusOn(switchCompat)
            .focusShape(FocusShape.ROUNDED_RECTANGLE)
            .title("Turn on/off auto refresh.")
            .showOnce("auto_refresh_switch_showcase")
            .build()
            .show();
      }

      @Override
      public void onSkipped(String id) {

      }
    };

    new FancyShowCaseView.Builder(getActivity())
        .focusOn(binding.fabAdd)
        .focusShape(FocusShape.CIRCLE)
        .title("Add a route here.")
        .showOnce("fab_showcase")
        .dismissListener(fabDismissListener)
        .build()
        .show();
  }

  private void setupSwitch() {
    switchCompat = binding.getRoot().findViewById(R.id.auto_refresh_switch);
    switchCompat.setVisibility(View.VISIBLE);
    boolean auto = preference.getBoolean(AUTO_REFRESH_ENABLED, false);
    switchCompat.setChecked(auto);
    vm.setAutoRefreshEnabled(auto);
    switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
      vm.setAutoRefreshEnabled(isChecked);
      SharedPreferences.Editor prefsEditor = preference.edit();
      prefsEditor.putBoolean(AUTO_REFRESH_ENABLED, isChecked);
      prefsEditor.apply();
      if(isChecked) {
        snackbarMessage("Auto refresh every 20 seconds.", null);
        loadFromPreference();
      } else {
        snackbarMessage("Auto refresh turned off.", null);
      }
    });
  }

  private void snackbarMessage(String message, View.OnClickListener onClickListener) {
    if(getActivity() != null) {
      if(onClickListener != null) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
            .setAction("Undo", onClickListener)
            .show();
      } else {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
            .show();
      }
    }
  }

  private void attachOnCardSwipe() {
    final boolean[] autoEnabled = {false};
    CardSwipeController cardSwipeController = new CardSwipeController(getActivity(), CardSwipeController.SwipeAction.DELETE);
    cardSwipeController.setAction(new SwipeControllerActions() {
      @Override
      public void onDragged(int fromPos, int toPos) {
        adapter.swapDataPos(fromPos, toPos);
        List<RouteModel> list = getSharedPreferencesData();
        if(fromPos < toPos) {
          for(int i = fromPos; i < toPos; i++) {
            Collections.swap(list, i, i + 1);
          }
        } else {
          for(int i = fromPos; i > toPos; i--) {
            Collections.swap(list, i, i - 1);
          }
        }
        saveSharedPreferenceData(list);
      }

      @Override
      public void onSelected() {
        binding.swipeRefreshLy.setEnabled(false);
        if(vm.isAutoRefreshEnabled()) {
          vm.setAutoRefreshEnabled(false);
          autoEnabled[0] = true;
        }
      }

      @Override
      public void onSwiped(int position) {
        List<RouteModel> list = getSharedPreferencesData();
        deletePreferencesData(position);
        lastDeletedItem = new Pair<>(adapter.getItemInPos(position), position);
        lastDeletedRouteString = list.get(position);
        adapter.deleteData(position);
        list.remove(position);

        snackbarMessage(Uilt.getFullStationName(lastDeletedItem.first.fromStation.get()) + " -> " + Uilt.getFullStationName(lastDeletedItem.first.destination.get()) + " removed", v -> {
          adapter.addDataInPos(lastDeletedItem.first, lastDeletedItem.second);
          list.add(lastDeletedItem.second, lastDeletedRouteString);
          saveSharedPreferenceData(list);
        });
      }

      @Override
      public void onDragFinished() {
        binding.swipeRefreshLy.setEnabled(true);
        if(autoEnabled[0]) {
          vm.setAutoRefreshEnabled(true);
        }
        binding.manualRbt.setChecked(true);
        binding.distRbt.setChecked(false);
        SharedPreferences.Editor prefsEditor = preference.edit();
        prefsEditor.putBoolean(IS_USING_DISTANCE_TO_SORT, false);
        prefsEditor.apply();
      }
    });
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(cardSwipeController);
    itemTouchHelper.attachToRecyclerView(binding.recylerView);
  }

  private void loadFromPreference() {

    List<RouteModel> list = getSharedPreferencesData();

    if(list.size() == 0) {
      binding.swipeRefreshLy.setRefreshing(false);
    } else {
      dashboardRouteList.clear();
      dashboardRouteList.addAll(list);

      if(binding.distRbt.isChecked()) {
        setUpGetCurrentLocation();
      }
//      vm.getRoutesEstimateTime(dashboardRouteList);
      vm.autoRefreshGetData(dashboardRouteList);
      vm.getAccurateEstTime(dashboardRouteList);
    }
  }

  private void setUpAdapter() {
    List<DashboardRecyclerViewItemVM> itemList = new ArrayList<>();

    List<RouteModel> list = getSharedPreferencesData();

    for(int i = 0; i < list.size(); ++i) {
      String fromStation = list.get(i).getFrom().getAbbr();
      String toStation = list.get(i).getTo().getAbbr();
      Log.d("dashboard", "From " + fromStation + " to " + toStation);
      DashboardRecyclerViewItemVM viewItemModel = new DashboardRecyclerViewItemVM(new ArrayList<>(), fromStation, toStation);
      viewItemModel.setItemClickListener((f, t, x, l) -> {
        new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()), R.style.MyDialog)
            .setTitle("Loading")
            .setMessage(R.string.noInternetErrorMessage)
            .setPositiveButton("Ok", null)
            .show();

      });
      itemList.add(viewItemModel);
    }


    adapter =
        new DashboardRecyclerViewAdapter(itemList, binding.recylerView.getId(),
            R.layout.dashboard_single_recycler_view_item);
    binding.recylerView.setAdapter(adapter);
    binding.recylerView.setLayoutManager(new LinearLayoutManager(getActivity()));

  }

  private void setupFab() {
    binding.recylerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if(dy > 0 && binding.fabAdd.getVisibility() == View.VISIBLE) {
          binding.fabAdd.hide();
        } else if(dy < 0 && binding.fabAdd.getVisibility() != View.VISIBLE) {
          binding.fabAdd.show();
        }
      }
    });

    binding.fabAdd.setOnClickListener(view -> {
      binding.fabAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_anim));
      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.MyDialog);
      View mView = getLayoutInflater().inflate(R.layout.add_route_dialog_layout, null);
      builder.setView(mView);
      Spinner fromSpinner = mView.findViewById(R.id.dialog_from_spinner);
      Spinner toSpinner = mView.findViewById(R.id.dialog_to_spinner);
      ConstraintLayout warnningLayout = mView.findViewById(R.id.dialog_error_layout);
      CheckBox returnRouteCheckbox = mView.findViewById((R.id.dialog_return_route_checkbox));

      warnningLayout.setVisibility(View.GONE);
      ArrayAdapter<Station> spinnerAdapter =
          new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, MainActivity.stationInfoList);
      spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      fromSpinner.setAdapter(spinnerAdapter);
      toSpinner.setAdapter(spinnerAdapter);

      builder.setPositiveButton("Add", null);

      builder.setNegativeButton("Cancel", null);

      AlertDialog alertDialog = builder.create();

      alertDialog.setTitle("Add A Route");

      alertDialog.setOnShowListener(dialogInterface -> {
        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(view1 -> {
          Station origin = MainActivity.stationInfoList.get(fromSpinner.getSelectedItemPosition());
          Station destination = MainActivity.stationInfoList.get(toSpinner.getSelectedItemPosition());

          if(!origin.getAbbr().equals(destination.getAbbr())) {
            List<RouteModel> dashboardList = getSharedPreferencesData();
            if(!dashboardList.contains(new RouteModel(origin, destination))) {
              addPreferencesData(new RouteModel(origin, destination));
            }
            if(returnRouteCheckbox.isChecked() && !dashboardList.contains(new RouteModel(origin, destination))) {
              addPreferencesData(new RouteModel(destination, origin));
            }
            dialogInterface.dismiss();
            setUpAdapter();
            loadFromPreference();
          } else {
            warnningLayout.setVisibility(View.VISIBLE);
          }
        });
      });
      alertDialog.show();
    });
  }

  private void handleError(Throwable throwable) {
    binding.swipeRefreshLy.setRefreshing(false);
    if(getActivity() != null) {
      Snackbar.make(binding.getRoot(), "Error on loading, cannot get route(s) info!", Snackbar.LENGTH_LONG).show();
    }
  }

  private void goToDetail(String from, String to, int color, View view) {
    if(getActivity() != null) {
      TextView textViewTo = view.findViewById(R.id.destination);
      TextView textViewFrom = view.findViewById(R.id.dashboard_from_tv);

      RouteDetailFragment fragment = new RouteDetailFragment();

      fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));
      fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));

      Bundle arg = new Bundle();
      arg.putString(MainActivity.BUDDLE_ARG_FROM, from);
      arg.putString(MainActivity.BUDDLE_ARG_TO, to);
      arg.putInt("color", color);
      fragment.setArguments(arg);
      getActivity().getSupportFragmentManager()
          .beginTransaction()
          .setReorderingAllowed(true)
          .addSharedElement(textViewTo, getString(R.string.text_to_transition))
          .addSharedElement(textViewFrom, getString(R.string.text_from_transition))
          .replace(R.id.main_frame_layout, fragment)
          .addToBackStack(null)
          .commit();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    vm.onCleared();
  }
}
