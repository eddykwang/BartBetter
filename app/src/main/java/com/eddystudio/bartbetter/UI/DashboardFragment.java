package com.eddystudio.bartbetter.UI;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.eddystudio.bartbetter.Adapter.CardSwipeController;
import com.eddystudio.bartbetter.Adapter.DashboardRecyclerViewAdapter;
import com.eddystudio.bartbetter.Adapter.SwipeControllerActions;
import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.ViewModel.DashboardRecyclerViewItemModel;
import com.eddystudio.bartbetter.R;
import com.eddystudio.bartbetter.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.eddystudio.bartbetter.UI.MainActivity.DASHBOARDROUTS;
import static com.eddystudio.bartbetter.UI.MainActivity.dashboardRouts;

public class DashboardFragment extends Fragment {

  private FragmentDashboardBinding binding;
  private String originStation;
  private DashboardRecyclerViewAdapter adapter;
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  @Inject
  public Repository repository;
  @Inject
  public SharedPreferences preference;

  public DashboardFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    binding = FragmentDashboardBinding.inflate(inflater, container, false);
    getActivity().findViewById(R.id.toolbar_imageView).setVisibility(View.GONE);
    CollapsingToolbarLayout collapsingToolbarLayout = getActivity().findViewById(R.id.toolbar_layout);
    collapsingToolbarLayout.setTitleEnabled(false);
    collapsingToolbarLayout.setTitle("My Routes");

    Application.getAppComponet().inject(this);
    setUpAdapter();
    setupFab();
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    loadFromPreference();
    binding.swipeRefreshLy.setOnRefreshListener(this::loadFromPreference);
    attachOnCardSwipe();
  }

  private void attachOnCardSwipe() {
    CardSwipeController cardSwipeController = new CardSwipeController(new SwipeControllerActions() {
      @Override
      public void onRightClicked(int position) {
        super.onRightClicked(position);
        dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());
        ArrayList<String> arrayList = new ArrayList<>(dashboardRouts);
        arrayList.remove(position);
        SharedPreferences.Editor editor = preference.edit();
        editor.putStringSet(DASHBOARDROUTS, new HashSet<>(arrayList));
        editor.apply();
        Snackbar.make(getActivity().findViewById(R.id.main_activity_coordinator_layout), "Removed", Snackbar.LENGTH_LONG).show();
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

    dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());
    ArrayList<String> list = new ArrayList<>(dashboardRouts);
    Log.d("dashboard", list.toString());
    //dashboardVmList.clear();

    if (list.size() == 0) {
      binding.swipeRefreshLy.setRefreshing(false);
    } else {
      //adapter.clearList();
      List<Pair<String, String>> stationPairList = new ArrayList<>();
      for (int i = 0; i < list.size(); ++i) {
        String fromStation = list.get(i).split("-", 2)[0];
        String toStation = list.get(i).split("-", 2)[1];
        Log.d("dashboard", "From " + fromStation + " to " + toStation);
        //adapter.setData(new DashboardRecyclerViewItemModel(new Etd(), fromStation, toStation));
        stationPairList.add(new Pair<>(fromStation, toStation));
      }
      getRoutesEstimateTime(stationPairList);
    }
  }

  private void getRoutesEstimateTime(List<Pair<String, String>> routes){
    AtomicInteger counter = new AtomicInteger();
    compositeDisposable.add(
            repository.getRouteSchedules(routes)
            .doOnSubscribe(ignored -> binding.swipeRefreshLy.setRefreshing(true))
            .observeOn(AndroidSchedulers.mainThread())
            .map(this::getRoutesInfoToVm)
            .subscribe(data->{
              adapter.modifyData(data, counter.get());
              counter.getAndIncrement();
            }, this::handleError, this::onComplete)
    );
  }

  private DashboardRecyclerViewItemModel getRoutesInfoToVm(ScheduleFromAToB scheduleFromAToB) {
    List<Trip> trips = scheduleFromAToB.getRoot().getSchedule().getRequest().getTrip();

    DashboardRecyclerViewItemModel vm = new DashboardRecyclerViewItemModel(trips,scheduleFromAToB.getRoot().getOrigin(),
            scheduleFromAToB.getRoot().getDestination());
    vm.setItemClickListener((from, to, color, view)->{
      goToDetail(from, to, color, view);
    });
    return vm;
  }

  private void onComplete() {
    binding.swipeRefreshLy.setRefreshing(false);
  }

  private void setUpAdapter() {
    List<DashboardRecyclerViewItemModel> itemList = new ArrayList<>();

    dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());
    ArrayList<String> list = new ArrayList<>(dashboardRouts);

      for (int i = 0; i < list.size(); ++i) {
        String fromStation = list.get(i).split("-", 2)[0];
        String toStation = list.get(i).split("-", 2)[1];
        Log.d("dashboard", "From " + fromStation + " to " + toStation);
        DashboardRecyclerViewItemModel viewItemModel =  new DashboardRecyclerViewItemModel(new ArrayList<>(), fromStation, toStation);
        viewItemModel.setItemClickListener((f, t, x, l)->{
          AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
          alertDialogBuilder.setMessage(R.string.noInternetErrorMessage);
              alertDialogBuilder.setPositiveButton("Retry",
                  (arg0, arg1) -> loadFromPreference());

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

  private void setupFab(){
    binding.recylerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy > 0 && binding.fabAdd.getVisibility() == View.VISIBLE) {
          binding.fabAdd.hide();
        } else if (dy < 0 && binding.fabAdd.getVisibility() != View.VISIBLE) {
          binding.fabAdd.show();
        }
      }
    });

    binding.fabAdd.setOnClickListener(view ->{
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      View mView = getLayoutInflater().inflate(R.layout.add_route_dialog_layout, null);
      builder.setView(mView);
      Spinner fromSpinner = mView.findViewById(R.id.dialog_from_spinner);
      Spinner toSpinner = mView.findViewById(R.id.dialog_to_spinner);
      ConstraintLayout warnningLayout = mView.findViewById(R.id.dialog_error_layout);
      CheckBox returnRouteCheckbox = mView.findViewById((R.id.dialog_return_route_checkbox));

      warnningLayout.setVisibility(View.GONE);
      ArrayAdapter<String> spinnerAdapter =
              new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, MainActivity.stationList);
      spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      fromSpinner.setAdapter(spinnerAdapter);
      toSpinner.setAdapter(spinnerAdapter);

      builder.setPositiveButton("Add", null);

      builder.setNegativeButton("Cancel",null);

      AlertDialog alertDialog = builder.create();
      alertDialog.setTitle("Add A Route");

      alertDialog.setOnShowListener(dialogInterface -> {
        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(view1 -> {
          String origin = MainActivity.stationListSortcut.get(fromSpinner.getSelectedItemPosition());
          String destination = MainActivity.stationListSortcut.get(toSpinner.getSelectedItemPosition());

          if (!origin.equals(destination)) {
            dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());
            ArrayList<String> arrayList = new ArrayList<>(dashboardRouts);
            arrayList.add(origin + "-" + destination);
            if (returnRouteCheckbox.isChecked()) {
              arrayList.add(destination + "-" + origin);
            }

            SharedPreferences.Editor editor = preference.edit();
            editor.putStringSet(DASHBOARDROUTS, new HashSet<>(arrayList));
            editor.apply();
            dialogInterface.dismiss();
            setUpAdapter();
            loadFromPreference();
          }else {
            warnningLayout.setVisibility(View.VISIBLE);
          }
        });
      });
      alertDialog.show();
    });
  }

  private void handleError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    binding.swipeRefreshLy.setRefreshing(false);
    Snackbar.make(getActivity().findViewById(R.id.main_activity_coordinator_layout), "Error on loading", Snackbar.LENGTH_LONG).show();
  }

  private void goToDetail(String from, String to, int color, View view) {
    if (getActivity() != null) {
      ImageView imageView = view.findViewById(R.id.dashboard_color_block_iv);
      TextView textView =view.findViewById(R.id.destination);

      RouteDetailFragment fragment = new RouteDetailFragment();

      fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));
      fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));
      fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));

      Bundle arg = new Bundle();
      arg.putString(MainActivity.BUDDLE_ARG_FROM, from);
      arg.putString(MainActivity.BUDDLE_ARG_TO, to);
      arg.putInt("color", color);
      fragment.setArguments(arg);
      getActivity().getSupportFragmentManager()
          .beginTransaction()
          .addToBackStack(null)
          .replace(R.id.main_frame_layout, fragment)
          .setReorderingAllowed(true)
          .addSharedElement(imageView, getString(R.string.goToDetailTransition) )
          .addSharedElement(textView, getString(R.string.textTransition))
          .commit();
    }
  }

  @Override public void onStop() {
    super.onStop();
    compositeDisposable.clear();
  }
}
