package com.eddystudio.bartbetter.ViewModel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.util.Log;
import android.util.Pair;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Bart;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.Model.Uilt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RouteDetailViewModel {
  public ObservableField<String> from = new ObservableField<>("");
  public ObservableField<String> to = new ObservableField<>("");
  public ObservableInt color = new ObservableInt();
  public ObservableBoolean isArrive = new ObservableBoolean(true);
  public ObservableField<String> date = new ObservableField<>("10/11/2018");
  public ObservableField<String> time = new ObservableField<>("10:20 am");
  public ObservableBoolean expanded = new ObservableBoolean(true);
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private Subject<Events> eventsSubject = PublishSubject.create();
  private final String fromShortcut;
  private final String toShortcut;
  @Inject
  public Repository repository;

  @Inject
  public RouteDetailViewModel(String from, String to, int color) {
    fromShortcut = from;
    toShortcut = to;
    this.from.set(Uilt.getFullStationName(from));
    this.to.set(Uilt.getFullStationName(to));
    this.color.set(color);
    Application.getAppComponet().inject(this);
  }

  public void dateClicked() {

  }

  public void timeClicked() {

  }

  public void setScheduleClicked() {

  }

  public Observable<Events> getEvents() {
    return eventsSubject.hide();
  }


  public void getRoutesInfo() {
    compositeDisposable.add(
        repository.getOneRouteSchedules(new Pair<>(fromShortcut, toShortcut))
            .doOnSubscribe(ignored -> eventsSubject.onNext(new Events.LoadingEvent(true)))
            .map(this::getTrips)
            .subscribe(this::getTrainLength, this::handleError)
    );
  }

  private void getTrainLength(List<Trip> trips) {
    List<Pair<String, String>> list = new ArrayList<>();
    list.add(new Pair<>(fromShortcut, toShortcut));
    compositeDisposable.add(
        repository.getListEstimate(list)
            .map(bart -> getEtd(bart.first))
            .concatMap(Observable::fromArray)
            .subscribe(len -> addToAdapter(trips, len), this::handleError, this::onComplete)
    );
  }

  private List<Etd> getEtd(Bart bart) {
    Log.d("destination", bart.toString());
    return bart.getRoot().getStation().get(0).getEtd();
  }

  private void addToAdapter(List<Trip> trips, List<Etd> etds) {
    for(int i = 0; i < trips.size(); ++i) {
      RouteDetailRecyclerViewModel vm = new RouteDetailRecyclerViewModel(trips.get(i), etds);
      eventsSubject.onNext(new Events.GetDataEvent(vm));
    }
  }

  private List<Trip> getTrips(ScheduleFromAToB schedule) {
    return schedule.getRoot().getSchedule().getRequest().getTrip();
  }

  private void handleError(Throwable throwable) {
    Log.e("route detail", throwable.getMessage());
  }

  private void onComplete() {
    eventsSubject.onNext(new Events.LoadingEvent(false));
  }

  public void onCleared() {
    compositeDisposable.clear();
  }
}
