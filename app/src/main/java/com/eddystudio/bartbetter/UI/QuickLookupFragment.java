package com.eddystudio.bartbetter.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eddystudio.bartbetter.Adapter.CardSwipeController;
import com.eddystudio.bartbetter.Adapter.QuickLookupRecyclerViewAdapter;
import com.eddystudio.bartbetter.Adapter.SwipeControllerActions;
import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Uilt;
import com.eddystudio.bartbetter.ViewModel.Events;
import com.eddystudio.bartbetter.ViewModel.QuickLookupRecyclerViewItemVM;
import com.eddystudio.bartbetter.ViewModel.QuickLookupViewModel;
import com.eddystudio.bartbetter.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import com.eddystudio.bartbetter.databinding.FragmentQuickLookupBinding;

import io.reactivex.Observable;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuickLookupFragment extends BaseFragment {

  private FragmentQuickLookupBinding binding;
  private static String selectedStation;
  private static int sinpperPos;
  private QuickLookupViewModel quickLookupViewModel;
  private ArrayList<String> etdStations;

  private static boolean isInitOpen = true;
  private QuickLookupRecyclerViewAdapter adapters;
  private static final String LAST_SELECTED_STATION = "LAST_SELECTED_STATION";
  private static final String LAST_SELECTED_SINPER_POSITION = "LAST_SELECTED_SINPER_POSITION";

  public QuickLookupFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    Application.getAppComponet().inject(this);
    quickLookupViewModel = new QuickLookupViewModel();
    binding = FragmentQuickLookupBinding.inflate(inflater, container, false);
    binding.setVm(quickLookupViewModel);

    Toolbar toolbar = binding.getRoot().findViewById(R.id.bb_toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    getActivity().setTitle("Discover");

    init();
    setupSinner();

    setUpAdapter();
    attachOnCardSwipe();

    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    if(binding.onErrorRelaticeLayout.getVisibility() == View.VISIBLE) {
      binding.onErrorRelaticeLayout.setVisibility(View.GONE);
      binding.recylerView.setVisibility(View.VISIBLE);
    }
    setLastSelectedStation();
  }

  private void init() {
    quickLookupViewModel.getEventsSubject()
        .compose(event -> Observable.mergeArray(
            event.ofType(Events.LoadingEvent.class).doOnNext(data -> binding.swipeRefreshLy.setRefreshing(data.isLoad())),
            event.ofType(Events.CompleteEvent.class).doOnNext(data -> onComplete(data.getBartList())),
            event.ofType(Events.ErrorEvent.class).doOnNext(error -> handleError(error.getError())),
            event.ofType(Events.GoToDetailEvent.class).doOnNext(data -> goToDetail(data.getFrom(), data.getTo(), data.getRouteColor(), data.getView())),
            event.ofType(Events.GetEtdEvent.class).doOnNext(data -> etdStations = (ArrayList<String>) data.getEtdStations())
        ))
        .subscribe();
  }

  private void setupSinner() {
    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, MainActivity.stationList);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.stationSpinner.setAdapter(spinnerAdapter);
    binding.stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(isInitOpen) {
          quickLookupViewModel.getData(selectedStation);
          isInitOpen = false;
        } else {
          selectedStation = MainActivity.stationListSortcut.get(i);
          sinpperPos = i;
          SharedPreferences.Editor editor = preference.edit();
          editor.putString(LAST_SELECTED_STATION, selectedStation);
          editor.putInt(LAST_SELECTED_SINPER_POSITION, sinpperPos);
          editor.apply();
          quickLookupViewModel.getData(selectedStation);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    binding.swipeRefreshLy.setOnRefreshListener(() -> {
      quickLookupViewModel.getData(selectedStation);
    });
  }

  private void setLastSelectedStation() {
    selectedStation = preference.getString(LAST_SELECTED_STATION, "12TH");
    sinpperPos = preference.getInt(LAST_SELECTED_SINPER_POSITION, 0);
    binding.stationSpinner.setSelection(sinpperPos);
    Log.d("lastStation", selectedStation + " : " + sinpperPos);
  }

  private void attachOnCardSwipe() {
    CardSwipeController cardSwipeController = new CardSwipeController(getContext(), CardSwipeController.SwipeAction.ADD);
    cardSwipeController.setAction(new SwipeControllerActions() {
      @Override
      public void onDragged(int fromPos, int toPos) {
      }

      @Override
      public void onSelected() {
      }

      @Override
      public void onSwiped(int position) {
        adapters.notifyItemChanged(position);
        String route = selectedStation + "-" + etdStations.get(position);
        List<String> dl = getSharedPreferencesData();
        if(!dl.contains(route)) {
          addPreferencesData(route);
        }
        if(getActivity() != null) {
          Snackbar.make(binding.getRoot(),
              "Added " + Uilt.getFullStationName(selectedStation) + " -> " +
                  Uilt.getFullStationName(etdStations.get(position)) + " to My Routes",
              Snackbar.LENGTH_LONG)
              .show();
        }
      }

      @Override
      public void onDragFinished() {
      }
    });
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(cardSwipeController);
    itemTouchHelper.attachToRecyclerView(binding.recylerView);
  }


  private void onComplete(ArrayList<QuickLookupRecyclerViewItemVM> bartList) {
    adapters.setData(bartList);
    runLayoutAnimation(binding.recylerView);
    binding.swipeRefreshLy.setRefreshing(false);
  }

  private void setUpAdapter() {
    adapters = new QuickLookupRecyclerViewAdapter(new ArrayList<>(), binding.recylerView.getId(),
        R.layout.home_page_single_recycler_view_item);
    binding.recylerView.setAdapter(adapters);
    binding.recylerView.setNestedScrollingEnabled(false);
    binding.recylerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
  }

  private void runLayoutAnimation(final RecyclerView recyclerView) {
    final Context context = recyclerView.getContext();
    final LayoutAnimationController controller =
        AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

    recyclerView.setLayoutAnimation(controller);
    recyclerView.getAdapter().notifyDataSetChanged();
    recyclerView.scheduleLayoutAnimation();
  }

  private void handleError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    loadErrorIV();
    Snackbar.make(binding.getRoot(), "Error on loading", Snackbar.LENGTH_LONG).show();
    binding.swipeRefreshLy.setRefreshing(false);
    quickLookupViewModel.showSpinnerProgess.set(false);
  }

  private void loadErrorIV() {
    if(binding.onErrorRelaticeLayout.getVisibility() == View.GONE) {
      binding.recylerView.setVisibility(View.GONE);
      binding.onErrorRelaticeLayout.setVisibility(View.VISIBLE);
      Glide.with(this).load(R.drawable.wentwrong).into(binding.onErrorImageView);
    }
  }

  private void goToDetail(String from, String to, int color, View view) {
    if(getActivity() != null) {
      TextView textView = view.findViewById(R.id.destination);
      RouteDetailFragment fragment = new RouteDetailFragment();

      //fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));
      fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));
      fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));

      Bundle arg = new Bundle();
      arg.putString(MainActivity.BUDDLE_ARG_FROM, from);
      arg.putString(MainActivity.BUDDLE_ARG_TO, to);
      arg.putInt("color", color);
      arg.putString("transitionName", getString(R.string.goToDetailTransition));
      fragment.setArguments(arg);
      getActivity().getSupportFragmentManager()
          .beginTransaction()
          .addToBackStack(null)
          .replace(R.id.main_frame_layout, fragment)
          .setReorderingAllowed(true)
          .addSharedElement(textView, getString(R.string.textTransition))
          .commit();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    quickLookupViewModel.onCleared();
  }
}
