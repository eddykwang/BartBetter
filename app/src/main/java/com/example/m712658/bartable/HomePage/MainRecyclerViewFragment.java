package com.example.m712658.bartable.HomePage;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.m712658.bartable.R;
import com.example.m712658.bartable.Repository.Repository;
import com.example.m712658.bartable.Repository.Response.Bart;
import com.example.m712658.bartable.Repository.Response.Etd;
import com.example.m712658.bartable.Repository.StationModel;
import com.example.m712658.bartable.databinding.FragmentMainRecyclerViewBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainRecyclerViewFragment extends Fragment {

    private final MainRecyclerViewModel viewModel = new MainRecyclerViewModel();
    private FragmentMainRecyclerViewBinding binding;
    private Repository repository;

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

        binding.swipeRefreshLy.setOnRefreshListener(this::init);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    private void init() {

        repository.getResponse()
                .doOnSubscribe(ignored -> binding.swipeRefreshLy.setRefreshing(true))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(bart -> getEtd(bart))
                .concatMap(Observable::fromArray).map(etds -> convertToVM(etds))
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

    private void handleError(Throwable throwable) {
        Log.e("error", "error on getting response", throwable);
        Snackbar.make(binding.recylerView, "error on loading", Snackbar.LENGTH_LONG).show();
    }

    private ArrayList<MainRecyclerViewModel> convertToVM(List<Etd> stations) {
        String m = " minus";
        ArrayList<MainRecyclerViewModel> vmList = new ArrayList<>();
        for (int i = 0; i < stations.size(); ++i) {
            String first = "";
            String second = "";
            String third = "";
            if (stations.get(i).getEstimate().size() == 1) {
                first = stations.get(i).getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving" : stations.get(i).getEstimate().get(0).getMinutes() + m;
            } else if (stations.get(i).getEstimate().size() == 2) {
                first = stations.get(i).getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving" : stations.get(i).getEstimate().get(0).getMinutes() + m;
                second = stations.get(i).getEstimate().get(1).getMinutes() +m;
            } else {
                first = stations.get(i).getEstimate().get(0).getMinutes().equals("Leaving") ? "Leaving" : stations.get(i).getEstimate().get(0).getMinutes() +m;
                second = stations.get(i).getEstimate().get(1).getMinutes() + m;
                third = stations.get(i).getEstimate().get(2).getMinutes() + m;
            }

            vmList.add(new MainRecyclerViewModel(
                    stations.get(i).getDestination(),
                    first,
                    second,
                    third,
                    matchMaterialColor(stations.get(i).getEstimate().get(0).getColor())));
        }
        return vmList;
    }

    private int matchMaterialColor(String color){
        int mColor = 1;
        switch (color){
            case "GREEN": mColor = Color.parseColor("#388E3C"); break;
            case "BLUE": mColor = Color.parseColor("#1976D2") ;break;
            case "RED": mColor = Color.parseColor("#D32F2F") ; break;
            case "YELLOW": mColor = Color.parseColor("#FBC02D") ; break;
            default: mColor = R.color.routColor_yellow ;
        }
        return mColor;
    }

    private List<Etd> getEtd(Bart bart) {
        Log.d("destination", bart.toString());
        return bart.getRoot().getStation().get(0).getEtd();
    }

}
