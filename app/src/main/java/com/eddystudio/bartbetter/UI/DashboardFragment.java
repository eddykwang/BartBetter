package com.eddystudio.bartbetter.UI;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import com.eddystudio.bartbetter.ViewModel.DashboardRecyclerViewItemVM;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.ViewModel.DashboardViewModel;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.eddystudio.bartbetter.UI.MainActivity.AUTO_REFRESH_ENABLED;

public class DashboardFragment extends BaseFragment {

  private FragmentDashboardBinding binding;
  private DashboardRecyclerViewAdapter adapter;
  private DashboardViewModel vm;
  private CollapsingToolbarLayout collapsingToolbarLayout;
  List<Pair<String, String>> stationPairList = new ArrayList<>();

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
    setUpAdapter();
    init();
    setupFab();
    return binding.getRoot();
  }

  private void setupSwitch() {
    SwitchCompat switchCompat = binding.getRoot().findViewById(R.id.auto_refresh_switch);
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
        snackbarMessage("Auto refresh every 20 seconds.");
        loadFromPreference();
      } else {
        snackbarMessage("Auto refresh turned off.");
      }
    });
  }

  private void snackbarMessage(String message) {
    if(getActivity() != null) {
      Snackbar.make(binding.getRoot(),
          message,
          Snackbar.LENGTH_LONG).show();
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
    CardSwipeController cardSwipeController = new CardSwipeController(new SwipeControllerActions() {
      @Override
      public void onRightClicked(int position) {
        super.onRightClicked(position);
        deletePreferencesData(position);
        snackbarMessage(Uilt.getFullStationName(stationPairList.get(position).first) + " -> " + Uilt.getFullStationName(stationPairList.get(position).second) + " removed");
        adapter.deleteData(position);
      }
    });
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(cardSwipeController);
    itemTouchHelper.attachToRecyclerView(binding.recylerView);
    binding.recylerView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        cardSwipeController.onDraw(c, "Remove", "#D32F2F");
      }
    });
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
      vm.getRoutesEstimateTime(stationPairList);
      vm.autoRefreshGetData(stationPairList);
//      vm.getAccurateEstTime(stationPairList);
    }
  }

  private void init() {
    vm.getEventsSubject()
        .observeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(event -> Observable.merge(
            event.ofType(Events.LoadingEvent.class).doOnNext(data -> binding.swipeRefreshLy.setRefreshing(data.isLoad())),
            event.ofType(Events.ErrorEvent.class).doOnNext(error -> handleError(error.getError())),
            event.ofType(Events.GoToDetailEvent.class).doOnNext(data -> goToDetail(data.getFrom(), data.getTo(), data.getRouteColor(), data.getView())),
            event.ofType(Events.GetEtdEvent.class).doOnNext(data -> adapter.modifyData(((Pair<DashboardRecyclerViewItemVM, Integer>) (data.getEtdStations())).first, ((Pair<DashboardRecyclerViewItemVM, Integer>) (data.getEtdStations())).second))
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
    binding.recylerView.setNestedScrollingEnabled(false);
    binding.recylerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

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
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
      TextView textViewFrom = view.findViewById(R.id.textView18);

      RouteDetailFragment fragment = new RouteDetailFragment();

      fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));

      Bundle arg = new Bundle();
      arg.putString(MainActivity.BUDDLE_ARG_FROM, from);
      arg.putString(MainActivity.BUDDLE_ARG_TO, to);
      arg.putInt("color", color);
      fragment.setArguments(arg);
      getActivity().getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.main_frame_layout, fragment)
          .setReorderingAllowed(true)
          .addSharedElement(textViewTo, getString(R.string.textTransition))
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
