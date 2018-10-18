package com.eddystudio.bartbetter.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.eddystudio.bartbetter.Adapter.RouteDetailRecyclerViewAdapter;
import com.eddystudio.bartbetter.Model.Uilt;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.ViewModel.RouteDetailRecyclerViewModel;
import com.eddystudio.bartbetter.ViewModel.RouteDetailViewModel;
import com.eddystudio.bartbetter.databinding.FragmentRoutDetailBinding;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RouteDetailFragment extends Fragment {
  private FragmentRoutDetailBinding binding;
  private String from;
  private String to;
  private int color;
  private RouteDetailRecyclerViewAdapter adapter;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private AppBarLayout appBarLayout;
  private RouteDetailViewModel vm;

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

    setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));
    setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.slide_bottom));
    setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));

    vm = new RouteDetailViewModel(from, to, color);
    binding.setVm(vm);

    setupToolbar();
    setupAdapter();
    init();
    binding.swipeRefreshLy.setOnRefreshListener(() -> {
      adapter.clearAllData();
      vm.getRoutesInfo();
    });
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    adapter.clearAllData();
    vm.getRoutesInfo();

  }

  private void init() {
    vm.getEvents()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(event -> Observable.merge(
            event.ofType(Events.LoadingEvent.class).doOnNext(isLoading -> binding.swipeRefreshLy.setRefreshing(isLoading.isLoad())),
            event.ofType(Events.GetDataEvent.class).doOnNext(data -> adapter.addData((RouteDetailRecyclerViewModel) data.getData()))
        )).subscribe();
  }

  private void setupToolbar() {
    ImageView imageView = binding.getRoot().findViewById(R.id.toolbar_imageView);
    CollapsingToolbarLayout collapsingToolbarLayout = binding.getRoot().findViewById(R.id.toolbar_layout);
    appBarLayout = getActivity().findViewById(R.id.app_bar);
    Toolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

  @Override
  public void onStop() {
    super.onStop();
    compositeDisposable.clear();
    vm.onCleared();
  }
}
