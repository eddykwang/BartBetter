package com.eddystudio.bartbetter.ViewModel;

import android.databinding.ObservableBoolean;
import android.util.Log;

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

public class QuickLookupViewModel {
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
    disposable.add(repository.getEstimate(stationShort)
        .doOnSubscribe(ignored -> eventsSubject.onNext(new Events.LoadingEvent(true)))
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::getEtd)
        .concatMap(Observable::fromArray)
        .map(this::convertToVM)
        .subscribe(data -> bartList = data,
            this::handleError,
            this::onComplete));
  }

  public void onCleared() {
    disposable.clear();
  }

  private List<Etd> getEtd(Bart bart) {
    Log.d("destination", bart.toString());
    return bart.getRoot().getStation().get(0).getEtd();
  }

  private void handleError(Throwable throwable) {
    eventsSubject.onNext(new Events.ErrorEvent(throwable));
  }

  private void onComplete() {
    eventsSubject.onNext(new Events.CompleteEvent(bartList));
  }

  private ArrayList<QuickLookupRecyclerViewItemVM> convertToVM(List<Etd> stations) {
    ArrayList<String> etdStations = new ArrayList<>();
    etdStations.clear();
    ArrayList<QuickLookupRecyclerViewItemVM> vmList = new ArrayList<>();
    for(int i = 0; i < stations.size(); ++i) {
      QuickLookupRecyclerViewItemVM vm =
          new QuickLookupRecyclerViewItemVM(selectedStation, stations.get(i));
      vm.setItemClickListener((f, t, r, v) -> eventsSubject.onNext(new Events.GoToDetailEvent(f, t, r, v)));
      vmList.add(vm);
      etdStations.add(stations.get(i).getAbbreviation());
    }
    eventsSubject.onNext(new Events.GetDataEvent(etdStations));
    return vmList;
  }
}
