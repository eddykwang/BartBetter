package com.example.eddystudio.bartable.HomePage;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;

import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.Repository.Repository;
import com.example.eddystudio.bartable.Repository.Response.EstimateResponse.Bart;
import com.example.eddystudio.bartable.Repository.Response.EstimateResponse.Etd;
import com.example.eddystudio.bartable.Repository.Response.Stations.BartStations;
import com.example.eddystudio.bartable.databinding.FragmentMainRecyclerViewBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainRecyclerViewFragment extends Fragment {

    //private final RecyclerViewItemModel viewModel = new RecyclerViewItemModel();
    private FragmentMainRecyclerViewBinding binding;
    private Repository repository;
    private static String selectedStation="DALY";
    private final ArrayList<String> stationList = new ArrayList<>();
    private final ArrayList<String> stationListSortcut = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private final MainViewModel mainViewModel= new MainViewModel();

    public MainRecyclerViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        repository = new Repository();
        //binding = DataBindingUtil.setContentView(getActivity(), R.layout.fragment_main_recycler_view);
        binding = FragmentMainRecyclerViewBinding.inflate(inflater, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) binding.appToolbar);
        binding.setVm(mainViewModel);
        spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, stationList);
        binding.swipeRefreshLy.setOnRefreshListener(() -> init(selectedStation));
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        init(selectedStation);
        getAllStations();

            mainViewModel.clicked
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(ignored -> selectedStation = stationListSortcut.get( mainViewModel.clickedPos.get()))
                    .subscribe();

    }

    private void init(String stationShort) {

        repository.getEstimate(stationShort)
                .doOnSubscribe(ignored -> binding.swipeRefreshLy.setRefreshing(true))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(bart -> getEtd(bart))
                .concatMap(Observable::fromArray)
                .map(etds -> convertToVM(etds))
                .doOnNext(data -> {
                    int resId = R.anim.layout_animation_fall_down;
                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                    binding.recylerView.setLayoutAnimation(animation);
                    RecyclerViewAdapter adapters = new RecyclerViewAdapter(data);
                    binding.recylerView.setAdapter(adapters);
                    binding.recylerView.setNestedScrollingEnabled(false);
                    binding.recylerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                })
                .doOnError(this::handleError)
                .doOnComplete(() -> binding.swipeRefreshLy.setRefreshing(false))
                .subscribe();
    }

    private void getAllStations(){
        repository.getStations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(station -> getAllStations(station))
                .concatMap(Observable::fromArray)
                .doOnNext(stations -> setupSinnper(stations))
                .doOnError(this::handleError)
                .subscribe();
    }

    private void setupSinnper(List<com.example.eddystudio.bartable.Repository.Response.Stations.Station> stations) {

        for (int i = 0; i < stations.size(); ++i){
            stationList.add(stations.get(i).getName());
            stationListSortcut.add(stations.get(i).getAbbr());
        }

        spinnerAdapter.notifyDataSetChanged();
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.stationSpinner.setAdapter(spinnerAdapter);

    }

    private List<com.example.eddystudio.bartable.Repository.Response.Stations.Station> getAllStations(BartStations station) {
        return station.getRoot().getStations().getStation();
    }

    private void handleError(Throwable throwable) {
        Log.e("error", "error on getting response", throwable);
        Snackbar.make(binding.recylerView, "error on loading", Snackbar.LENGTH_LONG).show();
    }

    private ArrayList<RecyclerViewItemModel> convertToVM(List<Etd> stations) {
        ArrayList<RecyclerViewItemModel> vmList = new ArrayList<>();
        for (int i = 0; i < stations.size(); ++i){
            vmList.add(new RecyclerViewItemModel(stations.get(i)));
        }
        return vmList;
    }

    private List<Etd> getEtd(Bart bart) {
        Log.d("destination", bart.toString());
        return bart.getRoot().getStation().get(0).getEtd();
    }

}
