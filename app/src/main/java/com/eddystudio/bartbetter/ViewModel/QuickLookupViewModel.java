package com.eddystudio.bartbetter.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Bart;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class QuickLookupViewModel extends ViewModel {
  public final ObservableBoolean showSpinnerProgess = new ObservableBoolean(false);
  private ArrayList<QuickLookupRecyclerViewItemVM> bartList = new ArrayList<>();
  private final CompositeDisposable disposable = new CompositeDisposable();
  private String selectedStation;

  @Inject
  public Repository repository;

  private Subject<Events> eventsSubject = PublishSubject.create();

  @Inject
  public QuickLookupViewModel() {
    Application.getAppComponet().inject(this);
  }

  public Observable<Events> getEventsSubject() {
    return eventsSubject.subscribeOn(Schedulers.io()).hide();
  }

  public void getData(String stationShort) {
    selectedStation = stationShort;
    List<Pair<String, String>> stations = new ArrayList<>();
    stations.add(new Pair<>(stationShort, ""));
    disposable.add(repository.getEstimate(stations)
        .doOnSubscribe(ignored -> eventsSubject.onNext(new LoadingEvent(true)))
        .observeOn(AndroidSchedulers.mainThread())
        .ofType(Repository.OnSuccess.class)
        .map(bart -> getEtd(bart.getPair().first))
        .concatMap(Observable::fromArray)
        .map(this::convertToVM)
        .subscribe(data -> bartList = data,
            this::handleError,
            this::onComplete));
  }

  @Override
  protected void onCleared() {
    disposable.clear();
    super.onCleared();
  }

  private List<Etd> getEtd(Bart bart) {
    Log.d("destination", bart.toString());
    return bart.getRoot().getStation().get(0).getEtd();
  }

  private void handleError(Throwable throwable) {
    eventsSubject.onNext(new ErrorEvent(throwable));
  }

  private void onComplete() {
    eventsSubject.onNext(new CompleteEvent(bartList));
  }

  private ArrayList<QuickLookupRecyclerViewItemVM> convertToVM(List<Etd> stations) {
    ArrayList<String> etdStations = new ArrayList<>();
    etdStations.clear();
    ArrayList<QuickLookupRecyclerViewItemVM> vmList = new ArrayList<>();
    for(int i = 0; i < stations.size(); ++i) {
      QuickLookupRecyclerViewItemVM vm =
          new QuickLookupRecyclerViewItemVM(selectedStation, stations.get(i));
      vm.setItemClickListener((f, t, r, v) -> eventsSubject.onNext(new GoToDetailEvent(f, t, r, v)));
      vmList.add(vm);
      etdStations.add(stations.get(i).getAbbreviation());
    }
    eventsSubject.onNext(new GetEtdEvent(etdStations));
    return vmList;
  }

  public interface Events {}

  public class LoadingEvent implements Events {
    private final boolean load;

    public LoadingEvent(boolean load) {this.load = load;}

    public boolean isLoad() {
      return load;
    }
  }

  public class ErrorEvent implements Events {
    private final Throwable error;

    public ErrorEvent(Throwable error) {this.error = error;}

    public Throwable getError() {
      return error;
    }
  }

  public class CompleteEvent implements Events {
    private final ArrayList<QuickLookupRecyclerViewItemVM> bartList;

    public CompleteEvent(ArrayList<QuickLookupRecyclerViewItemVM> bartList) {this.bartList = bartList;}

    public ArrayList<QuickLookupRecyclerViewItemVM> getBartList() {
      return bartList;
    }
  }

  public class GoToDetailEvent implements Events {
    private final String from;
    private final String to;
    private final int routeColor;
    private final View view;

    public GoToDetailEvent(String from, String to, int routeColor, View view) {
      this.from = from;
      this.to = to;
      this.routeColor = routeColor;
      this.view = view;
    }

    public String getFrom() {
      return from;
    }

    public String getTo() {
      return to;
    }

    public int getRouteColor() {
      return routeColor;
    }

    public View getView() {
      return view;
    }
  }

  public class GetEtdEvent implements Events {
    private final ArrayList<String> etdStations;

    public GetEtdEvent(ArrayList<String> etdStations) {this.etdStations = etdStations;}

    public ArrayList<String> getEtdStations() {
      return etdStations;
    }
  }
}
