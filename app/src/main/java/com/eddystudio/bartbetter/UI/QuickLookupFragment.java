package com.eddystudio.bartbetter.UI;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eddystudio.bartbetter.Adapter.CardSwipeController;
import com.eddystudio.bartbetter.Adapter.QuickLookupRecyclerViewAdapter;
import com.eddystudio.bartbetter.Adapter.SwipeControllerActions;
import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Response.Stations.Station;
import com.eddystudio.bartbetter.Model.Uilt;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.ViewModel.QuickLookupRecyclerViewItemVM;
import com.eddystudio.bartbetter.ViewModel.QuickLookupViewModel;
import com.eddystudio.bartbetter.databinding.FragmentQuickLookupBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuickLookupFragment extends BaseFragment {

  private FragmentQuickLookupBinding binding;
  private static String selectedStation;
  private static int sinpperPos;
  private QuickLookupViewModel quickLookupViewModel;
  private ArrayList<String> etdStations;
  private GoogleMap googleMap;
  private SupportMapFragment mapView;

  private static boolean isInitOpen = true;
  private QuickLookupRecyclerViewAdapter adapters;
  private static final String LAST_SELECTED_STATION = "LAST_SELECTED_STATION";
  private static final String LAST_SELECTED_SINPER_POSITION = "LAST_SELECTED_SINPER_POSITION";
  private static final String TO_SHOW_MAP_VIEW = "TO_SHOW_MAP_VIEW";
  private static final int LOCATION_REQUEST_CODE = 4344;

  public QuickLookupFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    Application.getAppComponet().inject(this);
    quickLookupViewModel = new QuickLookupViewModel();
    binding = FragmentQuickLookupBinding.inflate(inflater, container, false);
    binding.setVm(quickLookupViewModel);

    Toolbar toolbar = binding.getRoot().findViewById(R.id.bb_toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    getActivity().setTitle("Discover");

    init();
    setupSinner();

    setUpAdapter();
    attachOnCardSwipe();


    mapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps_view);

    if(preference.getBoolean(TO_SHOW_MAP_VIEW, true)) {
      setupMapView();
      binding.getRoot().findViewById(R.id.expand_map_iv).setVisibility(View.GONE);
    }else{
      binding.getRoot().findViewById(R.id.expand_map_iv).setVisibility(View.VISIBLE);
      Objects.requireNonNull(mapView.getView()).setVisibility(View.GONE);
    }


    binding.mapHideIv.setOnClickListener(v -> {
      Objects.requireNonNull(mapView.getView()).setVisibility(View.GONE);
      binding.expandMapIv.setVisibility(View.VISIBLE);
      mapView.onDestroyView();
      SharedPreferences.Editor editor = preference.edit();
      editor.putBoolean(TO_SHOW_MAP_VIEW, false);
      editor.apply();
    });

    binding.expandMapIv.setOnClickListener(v -> {
      Objects.requireNonNull(mapView.getView()).setVisibility(View.VISIBLE);
      v.setVisibility(View.GONE);
      setupMapView();
      SharedPreferences.Editor editor = preference.edit();
      editor.putBoolean(TO_SHOW_MAP_VIEW, true);
      editor.apply();
    });

    binding.spinnerConstrainLy.setLayoutTransition(new LayoutTransition());
    binding.spinnerConstrainLy.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    binding.mainContentLinearLayout.setLayoutTransition(new LayoutTransition());
    binding.mainContentLinearLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    binding.gpsImageview.setOnClickListener(v -> setUpGetCurrentLocation());

    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    setLastSelectedStation();
  }

  private void init() {
    quickLookupViewModel.getEventsSubject()
        .compose(event -> Observable.mergeArray(
            event.ofType(Events.LoadingEvent.class).doOnNext(data -> binding.swipeRefreshLy.setRefreshing(data.isLoad())),
            event.ofType(Events.CompleteEvent.class).doOnNext(data -> onComplete(data.getBartList())),
            event.ofType(Events.ErrorEvent.class).doOnNext(error -> handleError(error.getError())),
            event.ofType(Events.GoToDetailEvent.class).doOnNext(data -> goToDetail(data.getFrom(), data.getTo(), data.getRouteColor(), data.getView())),
            event.ofType(Events.GetDataEvent.class).doOnNext(data -> etdStations = (ArrayList<String>) data.getData())
        ))
        .subscribe();
  }

  private void setupMapView() {
    mapView.getMapAsync(googleMap -> {
      this.googleMap = googleMap;

      int c = 0;
      for(Station station : MainActivity.stationInfoList) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
            .position(new LatLng(Double.parseDouble(station.getGtfsLatitude()), Double.parseDouble(station.getGtfsLongitude()))));
        marker.setTag(c);
        marker.setTitle(station.getName());
        c++;
      }
      googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.7749, -122.4194), 10f));
      googleMap.setOnMarkerClickListener(marker -> {
        binding.stationSpinner.setSelection((Integer) marker.getTag());
        return false;
      });
    });
  }

  private void setupSinner() {
    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, MainActivity.stationList);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.stationSpinner.setAdapter(spinnerAdapter);
    binding.stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(isInitOpen) {
          quickLookupViewModel.getData(selectedStation);
          isInitOpen = false;
        } else {
          selectedStation = MainActivity.stationListSortcut.get(i);
          sinpperPos = i;

          Station selectedS = MainActivity.stationInfoList.get(i);

          if(preference.getBoolean(TO_SHOW_MAP_VIEW, true)) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(Double.parseDouble(selectedS.getGtfsLatitude()),
                    Double.parseDouble(selectedS.getGtfsLongitude())), 13f));
          }


          SharedPreferences.Editor editor = preference.edit();
          editor.putString(LAST_SELECTED_STATION, selectedStation);
          editor.putInt(LAST_SELECTED_SINPER_POSITION, sinpperPos);
          editor.apply();
          quickLookupViewModel.getData(selectedStation);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    binding.textView17.setOnClickListener(v ->
        binding.stationSpinner.onTouch(binding.getRoot(), MotionEvent.obtain(1, 1, MotionEvent.ACTION_UP, 1, 1, 1)));

    binding.swipeRefreshLy.setOnRefreshListener(() -> quickLookupViewModel.getData(selectedStation));
  }

  private void setUpGetCurrentLocation() {
    quickLookupViewModel.showSpinnerProgess.set(true);
    binding.gpsImageview.setVisibility(View.INVISIBLE);
    LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
    if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(
          new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION},
          LOCATION_REQUEST_CODE);

    } else {
      if(locationManager != null) {

        LocationListener locationListener = new LocationListener() {
          @Override
          public void onLocationChanged(Location location) {

            if(preference.getBoolean(TO_SHOW_MAP_VIEW, true)) {
              googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                  .icon(BitmapDescriptorFactory.fromBitmap(Uilt.getBitmapFromVectorDrawable(getContext(), R.drawable.ic_radio_button_checked_black_24dp)))
                  .title("Current Location"));
              googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13f));
            }

            List<Station> stations = new ArrayList<>(MainActivity.stationInfoList);
            Collections.sort(stations, (o1, o2) -> {
              Location location1 = new Location("one");
              location1.setLatitude(Double.parseDouble(o1.getGtfsLatitude()));
              location1.setLongitude(Double.parseDouble(o1.getGtfsLongitude()));

              Location location2 = new Location("two");
              location2.setLatitude(Double.parseDouble(o2.getGtfsLatitude()));
              location2.setLongitude(Double.parseDouble(o2.getGtfsLongitude()));

              float dist1 = location.distanceTo(location1);
              float dist2 = location.distanceTo(location2);
              return Float.compare(dist1, dist2);
            });

            Station cloestStation = stations.get(0);
            for(int i = 0; i < MainActivity.stationListSortcut.size(); ++i) {
              if(MainActivity.stationListSortcut.get(i).equalsIgnoreCase(cloestStation.getAbbr())) {
                binding.stationSpinner.setSelection(i);
                locationManager.removeUpdates(this);
                quickLookupViewModel.showSpinnerProgess.set(false);
                binding.gpsImageview.setVisibility(View.VISIBLE);
                break;
              }
            }

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
  }

  private void setLastSelectedStation() {
    selectedStation = preference.getString(LAST_SELECTED_STATION, "12TH");
    sinpperPos = preference.getInt(LAST_SELECTED_SINPER_POSITION, 0);
    binding.stationSpinner.setSelection(sinpperPos);
    Log.d("lastStation", selectedStation + " : " + sinpperPos);
  }

  private void attachOnCardSwipe() {
    CardSwipeController cardSwipeController = new CardSwipeController(getContext(), CardSwipeController.SwipeAction.ADD);
    cardSwipeController.setAction(new SwipeControllerActions() {
      @Override
      public void onDragged(int fromPos, int toPos) {
      }

      @Override
      public void onSelected() {
      }

      @Override
      public void onSwiped(int position) {
        adapters.notifyItemChanged(position);
        String route = selectedStation + "-" + etdStations.get(position);
        List<String> dl = getSharedPreferencesData();
        if(!dl.contains(route)) {
          addPreferencesData(route);
        }
        if(getActivity() != null) {
          Snackbar.make(binding.getRoot(),
              "Added " + Uilt.getFullStationName(selectedStation) + " -> " +
                  Uilt.getFullStationName(etdStations.get(position)) + " to My Routes",
              Snackbar.LENGTH_LONG)
              .show();
        }
      }

      @Override
      public void onDragFinished() {
      }
    });
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(cardSwipeController);
    itemTouchHelper.attachToRecyclerView(binding.recylerView);
  }


  private void onComplete(ArrayList<QuickLookupRecyclerViewItemVM> bartList) {
    adapters.setData(bartList);
    runLayoutAnimation(binding.recylerView);
    binding.swipeRefreshLy.setRefreshing(false);
  }

  private void setUpAdapter() {
    adapters = new QuickLookupRecyclerViewAdapter(binding.recylerView.getId(),
        R.layout.home_page_single_recycler_view_item);
    binding.recylerView.setAdapter(adapters);
    binding.recylerView.setNestedScrollingEnabled(false);
    binding.recylerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
  }

  private void runLayoutAnimation(final RecyclerView recyclerView) {
    final Context context = recyclerView.getContext();
    final LayoutAnimationController controller =
        AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

    recyclerView.setLayoutAnimation(controller);
    recyclerView.getAdapter().notifyDataSetChanged();
    recyclerView.scheduleLayoutAnimation();
  }

  private void handleError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    adapters.clearData();
    Snackbar.make(binding.getRoot(), "Route info not available.", Snackbar.LENGTH_LONG).show();
    binding.swipeRefreshLy.setRefreshing(false);
    quickLookupViewModel.showSpinnerProgess.set(false);
  }

  private void goToDetail(String from, String to, int color, View view) {
    if(getActivity() != null) {
      TextView textView = view.findViewById(R.id.destination);
      RouteDetailFragment fragment = new RouteDetailFragment();

      //fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));
      fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));
      fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));

      Bundle arg = new Bundle();
      arg.putString(MainActivity.BUDDLE_ARG_FROM, from);
      arg.putString(MainActivity.BUDDLE_ARG_TO, to);
      arg.putInt("color", color);
      fragment.setArguments(arg);
      getActivity().getSupportFragmentManager()
          .beginTransaction()
          .addToBackStack(null)
          .replace(R.id.main_frame_layout, fragment)
          .setReorderingAllowed(true)
          .addSharedElement(textView, getString(R.string.text_to_transition))
          .commit();
    }
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

  @Override
  public void onStop() {
    super.onStop();
    quickLookupViewModel.onCleared();
  }
}
