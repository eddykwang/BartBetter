package com.example.eddystudio.bartable.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eddystudio.bartable.Model.Repository;
import com.example.eddystudio.bartable.Model.Response.DelayReport.DelayReport;
import com.example.eddystudio.bartable.Model.Response.ElevatorStatus.ElevatorStatus;
import com.example.eddystudio.bartable.DI.Application;
import com.example.eddystudio.bartable.ViewModel.NotificationViewModel;
import com.example.eddystudio.bartable.databinding.FragmentNotificationBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class NotificationFragment extends android.support.v4.app.Fragment {

  private FragmentNotificationBinding binding;
  private final NotificationViewModel viewModel = new NotificationViewModel();
  private static final String savedViewMorePreference = "VIEW_MORE_PREFERENCE";
  private static boolean isErrorShowed;
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  @Inject
  public Repository repository;
  @Inject
  public SharedPreferences sharedPreferences;

  public NotificationFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentNotificationBinding.inflate(inflater, container, false);
    ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) binding.appToolbar);
    Application.getAppComponet().inject(this);
    binding.appToolbar.setTitle("Notifications");
    binding.setVm(viewModel);
    binding.swipeRefreshLy.setOnRefreshListener(this::init);
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    viewModel.isViewDetailChecked.set(sharedPreferences.getBoolean(savedViewMorePreference, false));
    init();
  }

  @Override
  public void onStop() {
    super.onStop();
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(savedViewMorePreference, viewModel.isViewDetailChecked.get());
    editor.apply();
    compositeDisposable.clear();
  }

  private void init() {
    isErrorShowed = false;
    Disposable delayDisposable = repository.getDelayReport()
        .doOnSubscribe(ignored -> {
          binding.swipeRefreshLy.setRefreshing(true);
          viewModel.isDelayReportProgressVisible.set(true);
        })
        .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(delayReport -> {
          convertToVM(delayReport);
          binding.swipeRefreshLy.setRefreshing(false);
          viewModel.isDelayReportProgressVisible.set(false);
        }, this::onError);
    compositeDisposable.add(delayDisposable);

    Disposable statusDisposable = repository.getElevatorStatus()
        .doOnSubscribe(ignored -> {
          binding.swipeRefreshLy.setRefreshing(true);
          viewModel.isElevatorProgressVisible.set(true);
        })
        .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(elevatorStatus -> {
          convertToVM(elevatorStatus);
          binding.swipeRefreshLy.setRefreshing(false);
          viewModel.isElevatorProgressVisible.set(false);
        }, this::onError);
    compositeDisposable.add(statusDisposable);
  }

  private void onError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    binding.swipeRefreshLy.setRefreshing(false);
    viewModel.isElevatorProgressVisible.set(false);
    viewModel.isDelayReportProgressVisible.set(false);
    if (!isErrorShowed) {
      Snackbar.make(binding.swipeRefreshLy, "Error on loading", Snackbar.LENGTH_LONG).show();
      isErrorShowed = true;
    }
  }

  private void convertToVM(ElevatorStatus elevatorStatus) {
    String station = elevatorStatus.getRoot().getBsa().get(0).getStation();
    viewModel.elevatorStation.set(station.equals("") ? "All" : station);
    viewModel.elevatorStatus.set(
        elevatorStatus.getRoot().getBsa().get(0).getDescription().getCdataSection());
    viewModel.isElevaorWorking.set(!station.equals("BART"));
  }

  private <R> R convertToVM(DelayReport delayReport) {
    viewModel.delayDescription.set(
        delayReport.getRoot().getBsa().get(0).getDescription().getCdataSection());
    viewModel.reportDate.set(delayReport.getRoot().getDate());
    viewModel.reportTime.set(delayReport.getRoot().getTime());
    String delayStation = delayReport.getRoot().getBsa().get(0).getStation();
    viewModel.reportStation.set(delayStation.equals("") ? "None" : delayStation);
    viewModel.isNotDelay.set(delayStation.equals(""));
    return null;
  }
}
