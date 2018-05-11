package com.example.eddystudio.bartable.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.eddystudio.bartable.Adapter.RouteDetailRecyclerViewAdapter;
import com.example.eddystudio.bartable.Model.Repository;
import com.example.eddystudio.bartable.Model.Response.Schedule.ScheduleFromAToB;
import com.example.eddystudio.bartable.Model.Response.Schedule.Trip;
import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.ViewModel.RouteDetailRecyclerViewModel;
import com.example.eddystudio.bartable.ViewModel.RouteDetailViewModel;
import com.example.eddystudio.bartable.databinding.FragmentRoutDetailBinding;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class RouteDetailFragment extends Fragment {
    private FragmentRoutDetailBinding binding;
    private String from;
    private String to;
    private int color;
    private RouteDetailRecyclerViewAdapter adapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    public Repository repository = new Repository();

    public RouteDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoutDetailBinding.inflate(inflater, container, false);

        ImageView imageView = getActivity().findViewById(R.id.toolbar_imageView);
        CollapsingToolbarLayout collapsingToolbarLayout = getActivity().findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar);

        Bundle arg = getArguments();
        if (arg != null) {
            from = arg.getString(MainActivity.BUDDLE_ARG_FROM);
            to = arg.getString(MainActivity.BUDDLE_ARG_TO);
            color = arg.getInt("color");
        }
        imageView.setTransitionName(getString(R.string.goToDetailTransition));

        setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        //binding.linearLayout.setTransitionName(transName);
        binding.textView18.setTransitionName(getString(R.string.textTransition));
        //if (binding.appToolbar !=null)
        //binding.appToolbar.setBackgroundColor(color);
        binding.setVm(new RouteDetailViewModel(from, to, color));


        imageView.setVisibility(View.VISIBLE);
        imageView.setBackgroundColor(color);
        collapsingToolbarLayout.setTitleEnabled(true);
        appBarLayout.setExpanded(true, true);
        collapsingToolbarLayout.setTitle(from + " -> " + to);
        collapsingToolbarLayout.setPadding(0, 0, 0, 0);


        setupAdapter();
        return binding.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();
        getRoutesInfo();
        binding.swipeRefreshLy.setOnRefreshListener(this::getRoutesInfo);

    }

    private void getRoutesInfo() {
        List<Pair<String, String>> routes = new ArrayList<>();
        routes.add(new Pair<>(from, to));
        adapter.clearAllData();
        compositeDisposable.add(
                repository.getRouteSchedules(routes)
                        .doOnSubscribe(ignored -> binding.swipeRefreshLy.setRefreshing(true))
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(this::getTrips)
                        .subscribe(this::addToAdapter, this::handleError, this::onComplete)
        );
    }

    private void addToAdapter(List<Trip> trips) {
        for (Trip trip : trips){
            RouteDetailRecyclerViewModel vm = new RouteDetailRecyclerViewModel(trip);
            adapter.addData(vm);
        }
    }

    private List<Trip> getTrips(ScheduleFromAToB schedule) {
        return schedule.getRoot().getSchedule().getRequest().getTrip();
    }

    private void handleError(Throwable throwable) {
    }

    private void onComplete() {
        binding.swipeRefreshLy.setRefreshing(false);
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
    }
}
