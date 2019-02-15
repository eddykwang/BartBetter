package com.eddystudio.bartbetter.ViewModel;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.RouteModel;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class DashboardViewModel {
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

  public void autoRefreshGetData(List<RouteModel> routes) {
    disposable.add(
        Observable
            .interval(20, TimeUnit.SECONDS)
            .takeWhile(ignored -> autoRefreshEnabled)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ignored -> getAccurateEstTime(routes)));
  }

//  public void getRoutesEstimateTime(List<Pair<String, String>> routes) {
//    AtomicInteger counter = new AtomicInteger();
//    disposable.add(
//        repository.getRouteSchedules(routes)
//            .doOnSubscribe(ignored -> eventsSubject.onNext(new Events.LoadingEvent(true)))
//            .observeOn(AndroidSchedulers.mainThread())
//            .map(this::getRoutesInfoToVm)
//            .subscribe(data -> {
//                  eventsSubject.onNext(new Events.GetDataEvent(new Pair<>(data, counter.get())));
//                  counter.getAndIncrement();
//                },
//                this::handleError,
//                this::onComplete)
//    );
//  }

  public void setAutoRefreshEnabled(boolean autoRefreshEnabled) {
    this.autoRefreshEnabled = autoRefreshEnabled;
  }

  public boolean isAutoRefreshEnabled() {
    return autoRefreshEnabled;
  }

  public void getAccurateEstTime(List<RouteModel> routes) {
    disposable.add(
        repository.getAccurateEtdTime(routes)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(ignored -> eventsSubject.onNext(new Events.LoadingEvent(true)))
            .compose(result -> Observable.merge(
                result.ofType(Repository.OnSuccess.class)
                    .map(etdResult -> getRoutesInfoToVm(etdResult.getEtdResult().getEtd(), etdResult.getEtdResult().getOrigin(), etdResult.getEtdResult().getDestination()))
                    .doOnNext(etd -> eventsSubject.onNext(new Events.GetDataEvent(etd))),
                result.ofType(Repository.OnError.class).doOnNext(onError -> {
//                  DashboardRecyclerViewItemVM vm = new DashboardRecyclerViewItemVM(new ArrayList<Etd>(), onError.getFrom(), onError.getTo());
//                  eventsSubject.onNext(new Events.GetDataEvent(vm));
                    throw new RuntimeException(onError.getError());
                })
            ))
            .subscribe(etd -> {},
                this::handleError,
                this::onComplete
            )
    );
  }

  private void onComplete() {
    eventsSubject.onNext(new Events.LoadingEvent(false));
  }

  private DashboardRecyclerViewItemVM getRoutesInfoToVm(List<Etd> etds, String origin, String dest) {

    DashboardRecyclerViewItemVM vm = new DashboardRecyclerViewItemVM(etds, origin, dest);
    vm.setItemClickListener((f, t, s, v) -> eventsSubject.onNext(new Events.GoToDetailEvent(f, t, s, v)));
    return vm;
  }

  private void handleError(Throwable throwable) {
    eventsSubject.onNext(new Events.ErrorEvent(throwable));
  }

  public void onCleared() {
    disposable.clear();
  }
}
