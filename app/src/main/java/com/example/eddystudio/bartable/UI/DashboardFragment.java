package com.example.eddystudio.bartable.UI;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import android.widget.Toast;
import com.example.eddystudio.bartable.Adapter.DashboardRecyclerViewAdapter;
import com.example.eddystudio.bartable.ViewModel.DashboardRecyclerViewItemModel;
import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.Model.Repository;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Bart;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Etd;
import com.example.eddystudio.bartable.Adapter.CardSwipeController;
import com.example.eddystudio.bartable.Adapter.SwipeControllerActions;
import com.example.eddystudio.bartable.DI.Application;
import com.example.eddystudio.bartable.databinding.FragmentDashboardBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.example.eddystudio.bartable.MainActivity.DASHBOARDROUTS;
import static com.example.eddystudio.bartable.MainActivity.dashboardRouts;

public class DashboardFragment extends Fragment {

  private FragmentDashboardBinding binding;
  private String originStation;
  private List<DashboardRecyclerViewItemModel> itemList = new ArrayList<>();
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
    ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) binding.appToolbar);
    binding.appToolbar.setTitle("Dashboard");

    Application.getAppComponet().inject(this);
    setUpAdapter();
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
    loadFromPrerence();
    binding.swipeRefreshLy.setOnRefreshListener(this::loadFromPrerence);
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
        Snackbar.make(binding.recylerView, "Removed", Snackbar.LENGTH_LONG).show();
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

  private void loadFromPrerence() {
    dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());
    ArrayList<String> list = new ArrayList<>(dashboardRouts);
    Log.d("dashboard", list.toString());
    //dashboardVmList.clear();

    if (list.size() == 0) {
      binding.swipeRefreshLy.setRefreshing(false);
    } else {
      adapter.clearList();
      List<Pair<String, String>> stationPairList = new ArrayList<>();
      for (int i = 0; i < list.size(); ++i) {
        String fromStation = list.get(i).split("-", 2)[0];
        String toStation = list.get(i).split("-", 2)[1];
        Log.d("dashboard", "From " + fromStation + " to " + toStation);
        stationPairList.add(new Pair<>(fromStation, toStation));
      }
      init(stationPairList);
    }
  }

  private void init(List<Pair<String, String>> pairList) {

    Disposable disposable = repository.getEstimate(pairList)
        .doOnSubscribe(ignored -> binding.swipeRefreshLy.setRefreshing(true))
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::getEtd)
        .concatMap(Observable::fromArray)
        .map(etds -> convertToVM(etds.first, etds.second))
        .subscribe(data ->
                adapter.setData(data),
            this::handleError,
            this::onComplete);
    compositeDisposable.add(disposable);
  }

  private void onComplete() {
    binding.swipeRefreshLy.setRefreshing(false);
  }

  private void setUpAdapter() {
    int resId = R.anim.layout_animation_fall_down;
    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
    binding.recylerView.setLayoutAnimation(animation);
    adapter =
        new DashboardRecyclerViewAdapter(itemList, binding.recylerView.getId(),
            R.layout.dashboard_single_recycler_view_item);
    binding.recylerView.setAdapter(adapter);
    binding.recylerView.setNestedScrollingEnabled(false);
    binding.recylerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
  }

  private void handleError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    binding.swipeRefreshLy.setRefreshing(false);
    Snackbar.make(binding.recylerView, "Error on loading", Snackbar.LENGTH_LONG).show();
  }

  private DashboardRecyclerViewItemModel convertToVM(List<Etd> etd, String toStation) {
    for (int i = 0; i < etd.size(); ++i) {
      if (etd.get(i).getAbbreviation().equals(toStation)) {
        DashboardRecyclerViewItemModel vm = new DashboardRecyclerViewItemModel(etd.get(i), originStation);
        vm.setItemClickListener((from, to)->{
          Toast.makeText(getContext(), from  + " to "+ to, Toast.LENGTH_LONG).show();
        });
        return vm;
      }
    }

    return null;
  }

  private Pair<List<Etd>, String> getEtd(Pair<Bart, String> bart) {
    originStation = bart.first.getRoot().getStation().get(0).getName();
    return new Pair<>(bart.first.getRoot().getStation().get(0).getEtd(), bart.second);
  }

  @Override public void onStop() {
    super.onStop();
    compositeDisposable.clear();
  }
}
