package com.example.eddystudio.bartable.Notification;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eddystudio.bartable.Repository.Repository;
import com.example.eddystudio.bartable.Repository.Response.DelayReport.DelayReport;
import com.example.eddystudio.bartable.Repository.Response.ElevatorStatus.ElevatorStatus;
import com.example.eddystudio.bartable.databinding.FragmentNotificationBinding;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class NotificationFragment extends android.support.v4.app.Fragment {

    private FragmentNotificationBinding binding;
    private final NotificationViewModel viewModel = new NotificationViewModel();
    private final Repository repository = new Repository();

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) binding.appToolbar);
        binding.appToolbar.setTitle("Notifications");
        binding.setVm(viewModel);
        binding.swipeRefreshLy.setOnRefreshListener(()->{
            init();
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    private void init(){
        repository.getDelayReport()
                .doOnSubscribe(ignored->{
                    binding.swipeRefreshLy.setRefreshing(true);
                    viewModel.isDelayReportProgressVisible.set(true);
                })
                .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(delayReport -> convertToVM(delayReport))
                .doOnComplete(() -> {
                    binding.swipeRefreshLy.setRefreshing(false);
                    viewModel.isDelayReportProgressVisible.set(false);
                })
                .subscribe();

        repository.getElevatorStatus()
                .doOnSubscribe(ignored->{
                    binding.swipeRefreshLy.setRefreshing(true);
                    viewModel.isElevatorProgressVisible.set(true);
                })
                .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(delayReport -> convertToVM(delayReport))
                .doOnComplete(() -> {
                    binding.swipeRefreshLy.setRefreshing(false);
                    viewModel.isElevatorProgressVisible.set(false);
                })
                .subscribe();

    }

    private void convertToVM(ElevatorStatus elevatorStatus) {
        String station = elevatorStatus.getRoot().getBsa().get(0).getStation();
        viewModel.elevatorStation.set(station.equals("")? "All" : station);
        viewModel.elevatorStatus.set(elevatorStatus.getRoot().getBsa().get(0).getDescription().getCdataSection());
        viewModel.isElevaorWorking.set(!station.equals("BART"));
    }

    private <R> R convertToVM(DelayReport delayReport) {
        viewModel.delayDescription.set(delayReport.getRoot().getBsa().get(0).getDescription().getCdataSection());
        viewModel.reportDate.set(delayReport.getRoot().getDate());
        viewModel.reportTime.set(delayReport.getRoot().getTime());
        String delayStation = delayReport.getRoot().getBsa().get(0).getStation();
        viewModel.reportStation.set(delayStation.equals("")? "None": delayStation);
        viewModel.isNotDelay.set(delayStation.equals(""));
        return null;
    }

}
