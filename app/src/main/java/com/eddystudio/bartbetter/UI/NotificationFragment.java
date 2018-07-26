package com.eddystudio.bartbetter.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.DelayReport.DelayReport;
import com.eddystudio.bartbetter.Model.Response.ElevatorStatus.ElevatorStatus;
import com.eddystudio.bartbetter.ViewModel.NotificationViewModel;
import com.eddystudio.bartbetter.R;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import com.eddystudio.bartbetter.databinding.FragmentNotificationBinding;

public class NotificationFragment extends BaseFragment {

  private FragmentNotificationBinding binding;
  private final NotificationViewModel viewModel = new NotificationViewModel();
  private static final String savedViewMorePreference = "VIEW_MORE_PREFERENCE";
  private static boolean isErrorShowed;

  @Inject
  public Repository repository;
  @Inject
  public SharedPreferences sharedPreferences;

  public NotificationFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    binding = FragmentNotificationBinding.inflate(inflater, container, false);
    //((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) binding.toolbar);
    getActivity().findViewById(R.id.toolbar_imageView).setVisibility(View.GONE);
    Application.getAppComponet().inject(this);
    //binding.appToolbar.setTitle("Notifications");
    getActivity().findViewById(R.id.toolbar_imageView).setVisibility(View.GONE);
    if (getActivity() instanceof AppCompatActivity) {
      ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }
    CollapsingToolbarLayout collapsingToolbarLayout = getActivity().findViewById(R.id.toolbar_layout);
    collapsingToolbarLayout.setTitleEnabled(false);
    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("General");
    collapsingToolbarLayout.findViewById(R.id.auto_refresh_switch).setVisibility(View.GONE);

    viewModel.setItemClickListener(type -> {
      switch (type){
        case MAP: onMapClicked(); break;
        case ABOUT: onAboutClicked(); break;
      }
    });
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
  }

  private void init() {
    isErrorShowed = false;
    addDisposable(repository.getDelayReport()
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
        }, this::onError));

    addDisposable(repository.getElevatorStatus()
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
        }, this::onError));
  }

  private void onError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    binding.swipeRefreshLy.setRefreshing(false);
    viewModel.isElevatorProgressVisible.set(false);
    viewModel.isDelayReportProgressVisible.set(false);
    if (!isErrorShowed && getActivity() != null) {
      Snackbar.make(getActivity().findViewById(R.id.main_activity_coordinator_layout), "Error on loading", Snackbar.LENGTH_LONG)
          .setAction("Retry", view-> init())
          .show();
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

  private void onAboutClicked(){
      Intent intent = new Intent(getActivity(), AboutActivity.class);
      startActivity(intent);
  }

  private void onMapClicked(){
    Intent intent = new Intent(getActivity(), MapActivity.class);
    startActivity(intent);
  }

}
