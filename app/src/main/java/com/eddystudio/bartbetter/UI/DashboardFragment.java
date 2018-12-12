package com.eddystudio.bartbetter.UI;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.eddystudio.bartbetter.Adapter.CardSwipeController;
import com.eddystudio.bartbetter.Adapter.DashboardRecyclerViewAdapter;
import com.eddystudio.bartbetter.Adapter.SwipeControllerActions;
import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Uilt;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.ViewModel.DashboardRecyclerViewItemVM;
import com.eddystudio.bartbetter.ViewModel.DashboardViewModel;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.toptas.fancyshowcase.DismissListener;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

import static com.eddystudio.bartbetter.UI.MainActivity.AUTO_REFRESH_ENABLED;

public class DashboardFragment extends BaseFragment {

  private FragmentDashboardBinding binding;
  private DashboardRecyclerViewAdapter adapter;
  private DashboardViewModel vm;
  private CollapsingToolbarLayout collapsingToolbarLayout;
  private List<Pair<String, String>> stationPairList = new ArrayList<>();
  private SwitchCompat switchCompat;
  private Pair<DashboardRecyclerViewItemVM, Integer> lastDeletedItem = null;
  private String lastDeletedRouteString = null;

  public DashboardFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    binding = FragmentDashboardBinding.inflate(inflater, container, false);
    Toolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    getActivity().setTitle("My Routes");

    vm = new DashboardViewModel();
//    vm = ViewModelProviders.of(this).get(DashboardViewModel.class);
    Application.getAppComponet().inject(this);

    AppBarLayout appBarLayout = binding.appbarLayout;
    appBarLayout.addOnOffsetChangedListener((appbarLayout, offset) -> {
      int scrollRange = -1;
      if(scrollRange == -1) {
        scrollRange = appBarLayout.getTotalScrollRange();
      }
      if(scrollRange + offset == 0) {
        switchCompat.animate().translationX(0);
      } else {
        switchCompat.postDelayed(() -> switchCompat.animate().translationX(switchCompat.getWidth() + 20), 1000);
      }
    });

    setUpAdapter();
    init();
    setupFab();
    showCaseSetup();
    return binding.getRoot();
  }

  private void showCaseSetup() {

    DismissListener fabDismissListener = new DismissListener() {
      @Override
      public void onDismiss(String id) {
        new FancyShowCaseView.Builder(getActivity())
            .focusOn(switchCompat)
            .focusShape(FocusShape.ROUNDED_RECTANGLE)
            .title("Turn on/off auto refresh.")
            .showOnce("auto_refresh_switch_showcase")
            .build()
            .show();
      }

      @Override
      public void onSkipped(String id) {

      }
    };

    new FancyShowCaseView.Builder(getActivity())
        .focusOn(binding.fabAdd)
        .focusShape(FocusShape.CIRCLE)
        .title("Add a route here.")
        .showOnce("fab_showcase")
        .dismissListener(fabDismissListener)
        .build()
        .show();
  }

  private void setupSwitch() {
    switchCompat = binding.getRoot().findViewById(R.id.auto_refresh_switch);
    switchCompat.setVisibility(View.VISIBLE);
    boolean auto = preference.getBoolean(AUTO_REFRESH_ENABLED, false);
    switchCompat.setChecked(auto);
    vm.setAutoRefreshEnabled(auto);
    switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
      vm.setAutoRefreshEnabled(isChecked);
      SharedPreferences.Editor prefsEditor = preference.edit();
      prefsEditor.putBoolean(AUTO_REFRESH_ENABLED, isChecked);
      prefsEditor.apply();
      if(isChecked) {
        snackbarMessage("Auto refresh every 20 seconds.", null);
        loadFromPreference();
      } else {
        snackbarMessage("Auto refresh turned off.", null);
      }
    });
  }

  private void snackbarMessage(String message, View.OnClickListener onClickListener) {
    if(getActivity() != null) {
      if(onClickListener != null) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
            .setAction("Undo", onClickListener)
            .show();
      } else {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
            .show();
      }
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    setupSwitch();
    loadFromPreference();
    binding.swipeRefreshLy.setOnRefreshListener(this::loadFromPreference);
    attachOnCardSwipe();
  }

  private void attachOnCardSwipe() {
    final boolean[] autoEnabled = {false};
    CardSwipeController cardSwipeController = new CardSwipeController(getActivity(), CardSwipeController.SwipeAction.DELETE);
    cardSwipeController.setAction(new SwipeControllerActions() {
      @Override
      public void onDragged(int fromPos, int toPos) {
        adapter.swapDataPos(fromPos, toPos);
        List<String> list = getSharedPreferencesData();
        if(fromPos < toPos) {
          for(int i = fromPos; i < toPos; i++) {
            Collections.swap(list, i, i + 1);
          }
        } else {
          for(int i = fromPos; i > toPos; i--) {
            Collections.swap(list, i, i - 1);
          }
        }
        saveSharedPreferenceData(list);
      }

      @Override
      public void onSelected() {
        binding.swipeRefreshLy.setEnabled(false);
        if(vm.isAutoRefreshEnabled()) {
          vm.setAutoRefreshEnabled(false);
          autoEnabled[0] = true;
        }
      }

      @Override
      public void onSwiped(int position) {
        List<String> list = getSharedPreferencesData();
        deletePreferencesData(position);
        lastDeletedItem = new Pair<>(adapter.getItemInPos(position), position);
        lastDeletedRouteString = list.get(position);
        adapter.deleteData(position);
        list.remove(position);

        snackbarMessage(Uilt.getFullStationName(lastDeletedItem.first.fromStation.get()) + " -> " + Uilt.getFullStationName(lastDeletedItem.first.destination.get()) + " removed", v -> {
          adapter.addDataInPos(lastDeletedItem.first, lastDeletedItem.second);
          list.add(lastDeletedItem.second, lastDeletedRouteString);
          saveSharedPreferenceData(list);
        });
      }

      @Override
      public void onDragFinished() {
        binding.swipeRefreshLy.setEnabled(true);
        if(autoEnabled[0]) {
          vm.setAutoRefreshEnabled(true);
        }
      }
    });
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(cardSwipeController);
    itemTouchHelper.attachToRecyclerView(binding.recylerView);
  }

  private void loadFromPreference() {
    List<String> list = getSharedPreferencesData();
    if(list.size() == 0) {
      binding.swipeRefreshLy.setRefreshing(false);
    } else {
      stationPairList.clear();
      for(int i = 0; i < list.size(); ++i) {
        String fromStation = list.get(i).split("-", 2)[0];
        String toStation = list.get(i).split("-", 2)[1];
        stationPairList.add(new Pair<>(fromStation, toStation));
      }
//      vm.getRoutesEstimateTime(stationPairList);
      vm.autoRefreshGetData(stationPairList);
      vm.getAccurateEstTime(stationPairList);
    }
  }

  private void init() {
    vm.getEventsSubject()
        .observeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(event -> Observable.merge(
            event.ofType(Events.LoadingEvent.class).doOnNext(data -> {
              binding.swipeRefreshLy.setRefreshing(data.isLoad());
              if(getActivity() != null && binding.recylerView.findViewHolderForAdapterPosition(0) != null) {
                new FancyShowCaseView.Builder(getActivity())
                    .focusOn(binding.recylerView.findViewHolderForAdapterPosition(0).itemView)
                    .title("Tap to see more schedules,\nswipe left to deleted,\npress and drag to rearrange position. ")
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .showOnce("recyclerview_item_showcase")
                    .delay(500)
                    .build()
                    .show();
              }
            }),
            event.ofType(Events.ErrorEvent.class).doOnNext(error -> handleError(error.getError())),
            event.ofType(Events.GoToDetailEvent.class).doOnNext(data -> goToDetail(data.getFrom(), data.getTo(), data.getRouteColor(), data.getView())),
            event.ofType(Events.GetDataEvent.class).doOnNext(data -> adapter.modifyData((DashboardRecyclerViewItemVM) data.getData()))
        ))
        .subscribe();
  }

  private void setUpAdapter() {
    List<DashboardRecyclerViewItemVM> itemList = new ArrayList<>();

    List<String> list = getSharedPreferencesData();

    for(int i = 0; i < list.size(); ++i) {
      String fromStation = list.get(i).split("-", 2)[0];
      String toStation = list.get(i).split("-", 2)[1];
      Log.d("dashboard", "From " + fromStation + " to " + toStation);
      DashboardRecyclerViewItemVM viewItemModel = new DashboardRecyclerViewItemVM(new ArrayList<>(), fromStation, toStation);
      viewItemModel.setItemClickListener((f, t, x, l) -> {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder
            .setTitle("Loading")
            .setMessage(R.string.noInternetErrorMessage)
            .setPositiveButton("Ok", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
      });
      itemList.add(viewItemModel);
    }


    adapter =
        new DashboardRecyclerViewAdapter(itemList, binding.recylerView.getId(),
            R.layout.dashboard_single_recycler_view_item);
    binding.recylerView.setAdapter(adapter);
    binding.recylerView.setLayoutManager(new LinearLayoutManager(getActivity()));

  }

  private void setupFab() {
    binding.recylerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if(dy > 0 && binding.fabAdd.getVisibility() == View.VISIBLE) {
          binding.fabAdd.hide();
        } else if(dy < 0 && binding.fabAdd.getVisibility() != View.VISIBLE) {
          binding.fabAdd.show();
        }
      }
    });

    binding.fabAdd.setOnClickListener(view -> {
      binding.fabAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_anim));
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.custom_dailog_style);
      View mView = getLayoutInflater().inflate(R.layout.add_route_dialog_layout, null);
      builder.setView(mView);
      Spinner fromSpinner = mView.findViewById(R.id.dialog_from_spinner);
      Spinner toSpinner = mView.findViewById(R.id.dialog_to_spinner);
      ConstraintLayout warnningLayout = mView.findViewById(R.id.dialog_error_layout);
      CheckBox returnRouteCheckbox = mView.findViewById((R.id.dialog_return_route_checkbox));

      warnningLayout.setVisibility(View.GONE);
      ArrayAdapter<String> spinnerAdapter =
          new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, MainActivity.stationList);
      spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      fromSpinner.setAdapter(spinnerAdapter);
      toSpinner.setAdapter(spinnerAdapter);

      builder.setPositiveButton("Add", null);

      builder.setNegativeButton("Cancel", null);

      AlertDialog alertDialog = builder.create();
      alertDialog.setTitle("Add A Route");

      alertDialog.setOnShowListener(dialogInterface -> {
        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(view1 -> {
          String origin = MainActivity.stationListSortcut.get(fromSpinner.getSelectedItemPosition());
          String destination = MainActivity.stationListSortcut.get(toSpinner.getSelectedItemPosition());

          if(!origin.equals(destination)) {
            List<String> dashboardList = getSharedPreferencesData();
            if(!dashboardList.contains(origin + "-" + destination)) {
              addPreferencesData(origin + "-" + destination);
            }
            if(returnRouteCheckbox.isChecked() && !dashboardList.contains(destination + "-" + origin)) {
              addPreferencesData(destination + "-" + origin);
            }
            dialogInterface.dismiss();
            setUpAdapter();
            loadFromPreference();
          } else {
            warnningLayout.setVisibility(View.VISIBLE);
          }
        });
      });
      alertDialog.show();
    });
  }

  private void handleError(Throwable throwable) {
    binding.swipeRefreshLy.setRefreshing(false);
    if(getActivity() != null) {
      Snackbar.make(binding.getRoot(), "Error on loading", Snackbar.LENGTH_LONG).show();
    }
  }

  private void goToDetail(String from, String to, int color, View view) {
    if(getActivity() != null) {
      TextView textViewTo = view.findViewById(R.id.destination);
      TextView textViewFrom = view.findViewById(R.id.dashboard_from_tv);

      RouteDetailFragment fragment = new RouteDetailFragment();

      fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));
      fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));

      Bundle arg = new Bundle();
      arg.putString(MainActivity.BUDDLE_ARG_FROM, from);
      arg.putString(MainActivity.BUDDLE_ARG_TO, to);
      arg.putInt("color", color);
      fragment.setArguments(arg);
      getActivity().getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.main_frame_layout, fragment)
          .setReorderingAllowed(true)
          .addSharedElement(textViewTo, getString(R.string.text_to_transition))
          .addSharedElement(textViewFrom, getString(R.string.text_from_transition))
          .addToBackStack(null)
          .commit();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    vm.onCleared();
  }
}
