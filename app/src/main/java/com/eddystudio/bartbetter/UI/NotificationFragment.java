package com.eddystudio.bartbetter.UI;

import android.transition.Fade;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import static com.eddystudio.bartbetter.UI.MainActivity.THEME_MODE_PREFERENCE;

public class NotificationFragment extends BaseFragment {

  private FragmentNotificationBinding binding;
  private NotificationViewModel viewModel;
  private static final String VIEW_MORE_PREFERENCE = "VIEW_MORE_PREFERENCE";
  private static boolean isErrorShowed;

  @Inject
  public Repository repository;
  @Inject
  public SharedPreferences sharedPreferences;

  public NotificationFragment() {
    // Required empty public constructor
    setAllowEnterTransitionOverlap(false);
    setAllowReturnTransitionOverlap(false);
    setEnterTransition(new Fade());
    setExitTransition(new Fade());
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



    binding.switchThemeCheckbox.setChecked( AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
    binding.switchThemeCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
      if (b) {
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
      }else{
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
      }
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putInt(THEME_MODE_PREFERENCE, AppCompatDelegate.getDefaultNightMode());
      editor.apply();
      getActivity().recreate();
    });
    setupEvent();
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    viewModel.isViewDetailChecked.set(sharedPreferences.getBoolean(VIEW_MORE_PREFERENCE, false));
    isErrorShowed = false;
    binding.getRoot().postDelayed(() -> viewModel.init(), 300);
  }

  @Override
  public void onStop() {
    super.onStop();
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(VIEW_MORE_PREFERENCE, viewModel.isViewDetailChecked.get());
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
