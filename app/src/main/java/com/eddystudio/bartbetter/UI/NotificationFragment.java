package com.eddystudio.bartbetter.UI;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.ViewModel.NotificationViewModel;
import com.eddystudio.bartbetter.databinding.FragmentNotificationBinding;
import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NotificationFragment extends BaseFragment {

  private FragmentNotificationBinding binding;
  private NotificationViewModel viewModel;
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
    Application.getAppComponet().inject(this);
    Toolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    getActivity().setTitle("Notifications");
    viewModel = ViewModelProviders.of(this).get(NotificationViewModel.class);
    binding.setLifecycleOwner(this);
    binding.setVm(viewModel);
    binding.swipeRefreshLy.setOnRefreshListener(viewModel::init);
    setupEvent();
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    viewModel.isViewDetailChecked.set(sharedPreferences.getBoolean(savedViewMorePreference, false));
    isErrorShowed = false;
    binding.getRoot().postDelayed(() -> viewModel.init(), 300);
  }

  @Override
  public void onStop() {
    super.onStop();
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(savedViewMorePreference, viewModel.isViewDetailChecked.get());
    editor.apply();
  }

  private void setupEvent() {
    viewModel.getEventsSubject()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(event -> (
            Observable.merge(
                event.ofType(Events.LoadingEvent.class).doOnNext(data -> binding.swipeRefreshLy.setRefreshing(data.isLoad())),
                event.ofType(Events.ErrorEvent.class).doOnNext(data -> onError(data.getError())),
                event.ofType(Events.GetDataEvent.class).doOnNext(data -> {
                  switch((NotificationViewModel.ClickedItemType) data.getData()) {
                    case MAP:
                      onMapClicked();
                      break;
                    case ABOUT:
                      onAboutClicked();
                      break;
                  }
                })
            )
        )).subscribe();
  }

  private void onError(Throwable throwable) {
    if(!isErrorShowed && getActivity() != null) {
      Snackbar.make(binding.getRoot(), "Error on loading", Snackbar.LENGTH_LONG)
          .setAction("Retry", view -> viewModel.init())
          .show();
      isErrorShowed = true;
    }
  }

  private void onAboutClicked() {
    Intent intent = new Intent(getActivity(), AboutActivity.class);
    startActivity(intent);
  }

  private void onMapClicked() {
    Intent intent = new Intent(getActivity(), MapActivity.class);
    startActivity(intent);
  }

}
