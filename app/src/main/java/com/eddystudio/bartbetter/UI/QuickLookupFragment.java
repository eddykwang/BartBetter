package com.eddystudio.bartbetter.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eddystudio.bartbetter.Adapter.CardSwipeController;
import com.eddystudio.bartbetter.Adapter.QuickLookupRecyclerViewAdapter;
import com.eddystudio.bartbetter.Adapter.SwipeControllerActions;
import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Bart;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Response.Stations.BartStations;
import com.eddystudio.bartbetter.Model.Response.Stations.Station;
import com.eddystudio.bartbetter.ViewModel.HomePageRecyclerViewItemModel;
import com.eddystudio.bartbetter.ViewModel.QuickLookupViewModel;
import com.eddystudio.bartbetter.R;

import java.util.ArrayList;
import java.util.List;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import com.eddystudio.bartbetter.databinding.FragmentQuickLookupBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuickLookupFragment extends BaseFragment {

  private FragmentQuickLookupBinding binding;
  private static String selectedStation;
  private static int sinpperPos;
  private ArrayAdapter<String> spinnerAdapter;
  private final QuickLookupViewModel quickLookupViewModel = new QuickLookupViewModel();
  private final ArrayList<String> EtdStations = new ArrayList<>();

  private ArrayList<HomePageRecyclerViewItemModel> bartList = new ArrayList<>();
  private static boolean isInitOpen = true;
  private QuickLookupRecyclerViewAdapter adapters;
  private static final String lastSelectedStation = "LAST_SELECTED_STATION";
  private static final String lastSelectedSinperPosition = "LAST_SELECTED_SINPER_POSITION";
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  public QuickLookupFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    Application.getAppComponet().inject(this);

    //binding = DataBindingUtil.setContentView(getActivity(), R.layout.fragment_quick_lookup);
    binding = FragmentQuickLookupBinding.inflate(inflater, container, false);

    //((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) binding.appToolbar);
    binding.setVm(quickLookupViewModel);
    getActivity().findViewById(R.id.toolbar_imageView).setVisibility(View.GONE);
    CollapsingToolbarLayout collapsingToolbarLayout = getActivity().findViewById(R.id.toolbar_layout);
    collapsingToolbarLayout.setTitleEnabled(false);
    collapsingToolbarLayout.setTitle("Discover");
    if (getActivity() instanceof AppCompatActivity) {
      ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }
    setupSinnper();

    setUpAdapter();
    attachOnCardSwipe();

    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    if (binding.onErrorRelaticeLayout.getVisibility() == View.VISIBLE) {
      binding.onErrorRelaticeLayout.setVisibility(View.GONE);
      binding.recylerView.setVisibility(View.VISIBLE);
    }
    setLastSelectedStation();
  }

  @Override public void onStop() {
    super.onStop();
    compositeDisposable.clear();
  }

  private void setupSinnper() {
    spinnerAdapter =
        new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, MainActivity.stationList);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.stationSpinner.setAdapter(spinnerAdapter);
    binding.stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (isInitOpen) {
          init(selectedStation);
          isInitOpen = false;
        } else {
          selectedStation = MainActivity.stationListSortcut.get(i);
          sinpperPos = i;
          SharedPreferences.Editor editor = preference.edit();
          editor.putString(lastSelectedStation, selectedStation);
          editor.putInt(lastSelectedSinperPosition, sinpperPos);
          editor.commit();
          init(selectedStation);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    binding.swipeRefreshLy.setOnRefreshListener(() -> {
      init(selectedStation);
    });
  }

  private void setLastSelectedStation() {
    selectedStation = preference.getString(lastSelectedStation, "12TH");
    sinpperPos = preference.getInt(lastSelectedSinperPosition, 0);

    Log.d("lastStation", selectedStation + " : " + sinpperPos);
  }

  private void attachOnCardSwipe() {
    CardSwipeController cardSwipeController = new CardSwipeController(new SwipeControllerActions() {
      @Override
      public void onRightClicked(int position) {
        super.onRightClicked(position);
        String rout = selectedStation + "-" + EtdStations.get(position);
        addPreferencesData(rout);
        Snackbar.make(getActivity().findViewById(R.id.main_activity_coordinator_layout), "Added " + rout + " to dashboard", Snackbar.LENGTH_LONG)
            .show();
      }
    });
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(cardSwipeController);
    itemTouchHelper.attachToRecyclerView(binding.recylerView);
    binding.recylerView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        cardSwipeController.onDraw(c, "Add", "#FF4081");
      }
    });
  }

  private void init(String stationShort) {

    List<Pair<String, String>> stations = new ArrayList<>();
    stations.add(new Pair<>(stationShort, ""));
    Disposable disposable = repository.getEstimate(stations)
        .doOnSubscribe(ignored -> binding.swipeRefreshLy.setRefreshing(true))
        .observeOn(AndroidSchedulers.mainThread())
            .ofType(Repository.OnSuccess.class)
        .map(bart -> getEtd(bart.getPair().first))
        .concatMap(Observable::fromArray)
        .map(this::convertToVM)
        .subscribe(data -> bartList = data,
            this::handleError,
            this::onComplete);

    compositeDisposable.add(disposable);
  }

  private void onComplete() {
    adapters.setData(bartList);
    runLayoutAnimation(binding.recylerView);
    binding.swipeRefreshLy.setRefreshing(false);
  }

  private void setUpAdapter() {
    adapters = new QuickLookupRecyclerViewAdapter(bartList, binding.recylerView.getId(),
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

  private List<Station> getAllStations(
      BartStations station) {
    return station.getRoot().getStations().getStation();
  }

  private void handleError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    loadErrorIV();
    Snackbar.make(getActivity().findViewById(R.id.main_activity_coordinator_layout), "Error on loading", Snackbar.LENGTH_LONG).show();
    binding.swipeRefreshLy.setRefreshing(false);
    quickLookupViewModel.showSpinnerProgess.set(false);
  }

  private void loadErrorIV() {
    if (binding.onErrorRelaticeLayout.getVisibility() == View.GONE) {
      binding.recylerView.setVisibility(View.GONE);
      binding.onErrorRelaticeLayout.setVisibility(View.VISIBLE);
      Glide.with(this).load(R.drawable.wentwrong).into(binding.onErrorImageView);
    }
  }

  private ArrayList<HomePageRecyclerViewItemModel> convertToVM(List<Etd> stations) {
    EtdStations.clear();
    ArrayList<HomePageRecyclerViewItemModel> vmList = new ArrayList<>();
    for (int i = 0; i < stations.size(); ++i) {
      HomePageRecyclerViewItemModel vm =
          new HomePageRecyclerViewItemModel(selectedStation, stations.get(i));
      vm.setItemClickListener((from, to, color, view) -> {
        //Toast.makeText(getContext(), from + " to " + to, Toast.LENGTH_LONG).show();
        goToDetail(from, to, color, view);
      });
      vmList.add(vm);
      EtdStations.add(stations.get(i).getAbbreviation());
    }
    return vmList;
  }

  private List<Etd> getEtd(Bart bart) {
    Log.d("destination", bart.toString());
    return bart.getRoot().getStation().get(0).getEtd();
  }

  private void goToDetail(String from, String to, int color, View view) {
    if (getActivity() != null) {
      ImageView imageView = view.findViewById(R.id.boder_image_view);
      TextView textView = view.findViewById(R.id.destination);
      RouteDetailFragment fragment = new RouteDetailFragment();

      //fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));
      fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));
      fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_view_transition));

      Bundle arg = new Bundle();
      arg.putString(MainActivity.BUDDLE_ARG_FROM, from);
      arg.putString(MainActivity.BUDDLE_ARG_TO, to);
      arg.putInt("color", color);
      arg.putString("transitionName",getString(R.string.goToDetailTransition) );
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
}
