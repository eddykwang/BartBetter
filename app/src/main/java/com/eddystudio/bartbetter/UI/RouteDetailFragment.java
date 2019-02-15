package com.eddystudio.bartbetter.UI;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.eddystudio.bartbetter.Adapter.RouteDetailRecyclerViewAdapter;
import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.RouteModel;
import com.eddystudio.bartbetter.Model.Response.Stations.Station;
import com.eddystudio.bartbetter.Model.Uilt;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.ViewModel.RouteDetailRecyclerViewModel;
import com.eddystudio.bartbetter.ViewModel.RouteDetailViewModel;
import com.eddystudio.bartbetter.databinding.FragmentRoutDetailBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.toptas.fancyshowcase.FancyShowCaseView;

public class RouteDetailFragment extends BaseFragment {
  private FragmentRoutDetailBinding binding;
  private String from;
  private String to;
  private int color;
  private RouteDetailRecyclerViewAdapter adapter;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private AppBarLayout appBarLayout;
  private RouteDetailViewModel vm;
  private BottomSheetBehavior bottomSheetBehavior;

  public RouteDetailFragment() {
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           Bundle savedInstanceState) {
    binding = FragmentRoutDetailBinding.inflate(inflater, container, false);

    Bundle arg = getArguments();
    if(arg != null) {
      from = arg.getString(MainActivity.BUDDLE_ARG_FROM);
      to = arg.getString(MainActivity.BUDDLE_ARG_TO);
      color = arg.getInt("color");
    }

    Application.getAppComponet().inject(this);

    setEnterTransition(new Fade());
    setExitTransition(new Fade());

//    setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.explode));

    vm = new RouteDetailViewModel(from, to, color);
    binding.setVm(vm);
    setupToolbar();
    setupAdapter();
    setupBottomSheet();
    init();
    binding.swipeRefreshLy.setOnRefreshListener(() -> {
      adapter.clearAllData();
      vm.getRoutesInfo(null, null, true);
    });
    binding.bottomSheetFab.setOnClickListener(view -> bottomSheetClicked());
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    Objects.requireNonNull(getActivity()).findViewById(R.id.navigation).setVisibility(View.GONE);

    binding.getRoot().postDelayed(() -> {
      adapter.clearAllData();
      vm.getRoutesInfo(null, null, true);
    }, 300);

  }

  private void init() {
    addDisposable(
        vm.getEvents()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(event -> Observable.merge(
                event.ofType(Events.LoadingEvent.class).doOnNext(isLoading -> binding.swipeRefreshLy.setRefreshing(isLoading.isLoad())),
                event.ofType(Events.GetDataEvent.class).doOnNext(data -> handleEvents(data.getData()))
            )).subscribe(i -> {}, i -> {}, this::setupShowCase));
  }

  private void setupShowCase() {
    new FancyShowCaseView.Builder(getActivity())
        .title("Swipe to right to see more schedules.")
        .showOnce("route_detail_recycler_view_showcase")
        .build()
        .show();
  }

  private void setupToolbar() {
    ImageView imageView = binding.getRoot().findViewById(R.id.toolbar_imageView);
    CollapsingToolbarLayout collapsingToolbarLayout = binding.getRoot().findViewById(R.id.toolbar_layout);
    appBarLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.app_bar);
    Toolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    setHasOptionsMenu(true);
    imageView.setImageResource(Uilt.randomCityBgGenerator());
    collapsingToolbarLayout.setTitleEnabled(true);
    collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
    collapsingToolbarLayout.setTitle(Uilt.getFullStationName(to));
  }

  private void setupAdapter() {
    ArrayList<RouteDetailRecyclerViewModel> routeInfoList = new ArrayList<>();
    adapter = new RouteDetailRecyclerViewAdapter(routeInfoList, binding.routeDetailRecyclerview.getId(),
        R.layout.route_detail_single_recycler_view_item);
    binding.routeDetailRecyclerview.setAdapter(adapter);
    binding.routeDetailRecyclerview.setNestedScrollingEnabled(false);
    LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
    binding.routeDetailRecyclerview.setLayoutManager(manager);
    binding.pageIndicator.attachToRecyclerView(binding.routeDetailRecyclerview);
    SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(binding.routeDetailRecyclerview);
  }

  private void setupBottomSheet() {
    bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetView.getRoot());
    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View view, int i) {
        if(i == BottomSheetBehavior.STATE_DRAGGING || i == BottomSheetBehavior.STATE_EXPANDED) {
          vm.bottomSheetup();
        }
      }

      @Override
      public void onSlide(@NonNull View view, float v) {
        binding.bottomSheetFab.animate().scaleX(1 - v).scaleY(1 - v).setDuration(0).start();
      }
    });
  }

  @SuppressLint("RestrictedApi")
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.route_detail_menu, menu);
    if(menu instanceof MenuBuilder) {
      ((MenuBuilder) menu).setOptionalIconsVisible(true);
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.route_detail_delete:
        deleteRoute();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void deleteRoute() {

    Station froms = new Station();
    Station tos = new Station();
    froms.setAbbr(from);
    tos.setAbbr(to);
    RouteModel model = new RouteModel(froms, tos);

    if(deleteRouteInDashBoard(model)) {
      if(getFragmentManager() != null) {
        getFragmentManager().popBackStack();
      }
    } else {
      Snackbar.make(binding.getRoot(), "Something went wrong, please try again.", Snackbar.LENGTH_LONG).show();
    }
  }

  private void handleEvents(Object event) {
    if(event instanceof RouteDetailRecyclerViewModel) {
      adapter.addData((RouteDetailRecyclerViewModel) event);
    } else if(event instanceof RouteDetailViewModel.ClickEvents) {
      switch((RouteDetailViewModel.ClickEvents) event) {
        case BOTTOM_SHEET_CLICK:
          bottomSheetClicked();
          break;
        case DATA_CLICK:
          dataClicked();
          break;
        case TIME_CLICK:
          timeClicked();
          break;
        case SET_BUTTON_CLICK:
          setButtonClicked();
          break;
        default:
          break;
      }
    } else {
      Log.e("Route Detail", "unhandled event");
    }
  }

  private void bottomSheetClicked() {
    if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    } else {
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  private void dataClicked() {
    final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);

    DatePickerDialog datePickerDialog =
        new DatePickerDialog(Objects.requireNonNull(getActivity()), (datePicker, year, monthOfYear, dayOfMonth) -> vm.updateDate(year, monthOfYear + 1, dayOfMonth), mYear, mMonth, mDay);
    datePickerDialog.show();
  }

  private void timeClicked() {
    final Calendar c = Calendar.getInstance();
    int mHour = c.get(Calendar.HOUR_OF_DAY);
    int mMinute = c.get(Calendar.MINUTE);

    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
        (view, hourOfDay, minute) -> vm.updateTime(hourOfDay, minute), mHour, mMinute, false);
    timePickerDialog.show();
  }

  private void setButtonClicked() {
    adapter.clearAllData();
  }

  @Override
  public void onStop() {
    super.onStop();
    Objects.requireNonNull(getActivity()).findViewById(R.id.navigation).setVisibility(View.VISIBLE);
    compositeDisposable.clear();
    vm.onCleared();
  }
}
