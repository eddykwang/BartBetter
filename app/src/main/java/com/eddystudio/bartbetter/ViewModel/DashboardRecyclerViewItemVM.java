package com.eddystudio.bartbetter.ViewModel;


import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.Model.Uilt;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class DashboardRecyclerViewItemVM extends ViewModel {
  public final ObservableField<String> destination = new ObservableField<>("");
  public final ObservableField<String> fromStation = new ObservableField<>("");
  public final ObservableField<String> firstTrain = new ObservableField<>("");
  public final ObservableField<String> secondTrain = new ObservableField<>("");
  public final ObservableField<String> thirdTrain = new ObservableField<>("");
  public final ObservableInt routeColor = new ObservableInt(Color.GRAY);
  public final ObservableInt routeColor2 = new ObservableInt(Color.GRAY);

  private final CompositeDisposable disposable = new CompositeDisposable();

  private String from = "";
  private String to = "";
  private ItemClickListener itemClickListener;
  private Subject<Events> eventsSubject = PublishSubject.create();


  @Inject
  public Repository repository;

  @Inject
  public DashboardRecyclerViewItemVM() {
    Application.getAppComponet().inject(this);
  }

  public DashboardRecyclerViewItemVM(List<Trip> trips, String from, String to) {
    this.from = from;
    this.to = to;
    try {
      updateUi(trips, from, to);
    } catch(ParseException e) {
      e.printStackTrace();
    }
  }

  public Observable<Events> getEventsSubject() {
    return eventsSubject.subscribeOn(Schedulers.io()).hide();
  }

  public void getRoutesEstimateTime(List<Pair<String, String>> routes) {
    AtomicInteger counter = new AtomicInteger();
    disposable.add(
        repository.getRouteSchedules(routes)
            .doOnSubscribe(ignored -> eventsSubject.onNext(new Events.LoadingEvent(true)))
            .observeOn(AndroidSchedulers.mainThread())
            .map(this::getRoutesInfoToVm)
            .subscribe(data -> {
                  eventsSubject.onNext(new Events.GetEtdEvent(new Pair<>(data, counter.get())));
                  counter.getAndIncrement();
                },
                this::handleError,
                this::onComplete)
    );
  }

  private void onComplete() {
    eventsSubject.onNext(new Events.LoadingEvent(false));
  }

  private DashboardRecyclerViewItemVM getRoutesInfoToVm(ScheduleFromAToB scheduleFromAToB) {
    List<Trip> trips = scheduleFromAToB.getRoot().getSchedule().getRequest().getTrip();

    DashboardRecyclerViewItemVM vm = new DashboardRecyclerViewItemVM(trips, scheduleFromAToB.getRoot().getOrigin(),
        scheduleFromAToB.getRoot().getDestination());
    vm.setItemClickListener((f, t, s, v) -> eventsSubject.onNext(new Events.GoToDetailEvent(f, t, s, v)));
    return vm;
  }

  private void handleError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    eventsSubject.onNext(new Events.ErrorEvent(throwable));
  }

  public void setItemClickListener(
      ItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public void onItemClicked(View view) {
    itemClickListener.onItemClicked(from, to, routeColor.get(), view);
  }

  private void updateUi(List<Trip> trips, String origin, String dest) throws ParseException {
    fromStation.set(Uilt.getFullStationName(origin));
    destination.set(Uilt.getFullStationName(dest));
    if(trips.size() > 0) {
      routeColor.set(Uilt.routeColorMatcher(trips.get(0).getLeg().get(0).getLine()));
      if(trips.get(0).getLeg().size() > 1) {
        routeColor2.set(Uilt.routeColorMatcher(trips.get(0).getLeg().get(1).getLine()));
      } else {
        routeColor2.set(routeColor.get());
      }

      firstTrain.set(Uilt.timeMinutesCalculator(trips.get(0).getLeg().get(0).getOrigTimeDate() + " " +
          trips.get(0).getLeg().get(0).getOrigTimeMin()));
      if(trips.get(1) != null) {
        secondTrain.set(Uilt.timeMinutesCalculator(trips.get(1).getLeg().get(0).getOrigTimeDate() + " " +
            trips.get(1).getLeg().get(0).getOrigTimeMin()));
        if(trips.get(2) != null) {
          thirdTrain.set(Uilt.timeMinutesCalculator(trips.get(2).getLeg().get(0).getOrigTimeDate() + " " +
              trips.get(2).getLeg().get(0).getOrigTimeMin()));
        }
      }
    }
  }

  @Override
  protected void onCleared() {
    disposable.clear();
    super.onCleared();
  }
}
