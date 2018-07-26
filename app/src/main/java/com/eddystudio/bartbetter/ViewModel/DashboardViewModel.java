package com.eddystudio.bartbetter.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.util.Log;
import android.util.Pair;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.UI.MainActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class DashboardViewModel extends ViewModel {
  private Subject<Events> eventsSubject = PublishSubject.create();
  private final CompositeDisposable disposable = new CompositeDisposable();
  private boolean autoRefreshEnabled = false;

  @Inject
  public Repository repository;

  @Inject
  public DashboardViewModel() {
    Application.getAppComponet().inject(this);
  }

  public Observable<Events> getEventsSubject() {
    return eventsSubject.subscribeOn(Schedulers.io()).hide();
  }

  public void autoRefreshGetData(List<Pair<String, String>> routes) {
    disposable.add(
        Observable
            .interval(20, TimeUnit.SECONDS)
            .takeWhile(ignored-> autoRefreshEnabled)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ignored -> getRoutesEstimateTime(routes)));
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

  public void setAutoRefreshEnabled(boolean autoRefreshEnabled) {
    this.autoRefreshEnabled = autoRefreshEnabled;
  }

  public void getAccurateEstTime(List<Pair<String, String>> routes) {
    disposable.add(
        repository.getAccurateEtdTime(routes)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(etds -> {
              String last = etds.get(0).getAbbreviation();
              int j = 0;
              for(int i = 0; i < etds.size(); ++i) {
                if(i > 1 && etds.get(i).getAbbreviation().equals(last)) {
                  j++;
                  last = etds.get(i).getAbbreviation();
                }
                Log.d("accurate time", "\n" + etds.get(i).getAbbreviation() + " : " + etds.get(i).getEstimate().get(j).getMinutes());
              }
            })
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

  @Override
  protected void onCleared() {
    disposable.clear();
    super.onCleared();
  }
}
