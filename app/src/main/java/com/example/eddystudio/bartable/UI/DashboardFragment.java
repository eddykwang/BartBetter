package com.example.eddystudio.bartable.UI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.HashSet;
import java.util.List;

import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.example.eddystudio.bartable.UI.MainActivity.DASHBOARDROUTS;
import static com.example.eddystudio.bartable.UI.MainActivity.dashboardRouts;

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
    //((AppCompatActivity) getActivity()).setSupportActionBar(container.findViewById(R.id.toolbar));
    //binding.appToolbar.setTitle("My Routes");
    getActivity().findViewById(R.id.toolbar_imageView).setVisibility(View.GONE);
    CollapsingToolbarLayout collapsingToolbarLayout = getActivity().findViewById(R.id.toolbar_layout);
    collapsingToolbarLayout.setTitleEnabled(false);
    collapsingToolbarLayout.setTitle("My Routes");

    Application.getAppComponet().inject(this);
    setUpAdapter();
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
      init(stationPairList);
    }
  }

  private void init(List<Pair<String, String>> pairList) {
    AtomicInteger counter = new AtomicInteger();
    Disposable disposable = repository.getEstimate(pairList)
        .doOnSubscribe(ignored -> binding.swipeRefreshLy.setRefreshing(true))
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::getEtd)
        .concatMap(Observable::fromArray)
        .map(etds -> convertToVM(etds.first, etds.second))
        .subscribe(data ->{
                adapter.modifyData(data, counter.get());
                counter.getAndIncrement();},
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
    List<DashboardRecyclerViewItemModel> itemList = new ArrayList<>();

    dashboardRouts = preference.getStringSet(DASHBOARDROUTS, new HashSet<>());
    ArrayList<String> list = new ArrayList<>(dashboardRouts);

      for (int i = 0; i < list.size(); ++i) {
        String fromStation = list.get(i).split("-", 2)[0];
        String toStation = list.get(i).split("-", 2)[1];
        Log.d("dashboard", "From " + fromStation + " to " + toStation);
        DashboardRecyclerViewItemModel viewItemModel =  new DashboardRecyclerViewItemModel(new Etd(), fromStation, toStation);
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

  private void handleError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    binding.swipeRefreshLy.setRefreshing(false);
    Snackbar.make(getActivity().findViewById(R.id.main_activity_coordinator_layout), "Error on loading", Snackbar.LENGTH_LONG).show();
  }

  private DashboardRecyclerViewItemModel convertToVM(List<Etd> etd, String toStation) {
    for (int i = 0; i < etd.size(); ++i) {
      if (etd.get(i).getAbbreviation().equals(toStation)) {
        DashboardRecyclerViewItemModel vm = new DashboardRecyclerViewItemModel(etd.get(i), originStation, toStation);
        vm.setItemClickListener((from, to, color, view)->{
          //Toast.makeText(getContext(), from  + " to "+ to, Toast.LENGTH_LONG).show();
          goToDetail(from, to, color, view);
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
