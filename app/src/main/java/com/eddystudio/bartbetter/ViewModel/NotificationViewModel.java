package com.eddystudio.bartbetter.ViewModel;


import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.util.Log;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Repository;
import com.eddystudio.bartbetter.Model.Response.DelayReport.DelayReport;
import com.eddystudio.bartbetter.Model.Response.ElevatorStatus.ElevatorStatus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class NotificationViewModel extends ViewModel {
  public enum ClickedItemType {
    MAP, ABOUT
  }

  public final ObservableField<String> delayDescription = new ObservableField<>("");
  public final ObservableBoolean isViewDetailChecked = new ObservableBoolean(false);
  public final ObservableField<String> reportTime = new ObservableField<>("");
  public final ObservableField<String> reportDate = new ObservableField<>("");
  public final ObservableField<String> reportStation = new ObservableField<>("");
  public final ObservableBoolean isDelayReportProgressVisible = new ObservableBoolean(false);
  public final ObservableBoolean isElevatorProgressVisible = new ObservableBoolean(false);
  public final ObservableField<String> elevatorStatus = new ObservableField<>("");
  public final ObservableField<String> elevatorStation = new ObservableField<>("");
  public final ObservableBoolean isNotDelay = new ObservableBoolean(true);
  public final ObservableBoolean isElevaorWorking = new ObservableBoolean(true);

  private final CompositeDisposable compositeDisposable = new CompositeDisposable();
  private final Subject<Events> eventsSubject = PublishSubject.create();

  @Inject
  public Repository repository;

  @Inject
  public NotificationViewModel() {
    Application.getAppComponet().inject(this);
  }

  public void onMapClicked() {
    eventsSubject.onNext(new Events.GetDataEvent(ClickedItemType.MAP));
  }

  public void onAboutClicked() {
    eventsSubject.onNext(new Events.GetDataEvent(ClickedItemType.ABOUT));
  }

  public Observable<Events> getEventsSubject() {
    return eventsSubject.hide();
  }

  public void init() {
    compositeDisposable.add(repository.getDelayReport()
        .doOnSubscribe(ignored -> {
          eventsSubject.onNext(new Events.LoadingEvent(true));
          this.isDelayReportProgressVisible.set(true);
        })
        .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(delayReport -> {
          convertToVM(delayReport);
          eventsSubject.onNext(new Events.LoadingEvent(false));
          this.isDelayReportProgressVisible.set(false);
        }, this::onError));

    compositeDisposable.add(repository.getElevatorStatus()
        .doOnSubscribe(ignored -> {
          eventsSubject.onNext(new Events.LoadingEvent(true));
          this.isElevatorProgressVisible.set(true);
        })
        .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(elevatorStatus -> {
          convertToVM(elevatorStatus);
          eventsSubject.onNext(new Events.LoadingEvent(false));
          this.isElevatorProgressVisible.set(false);
        }, this::onError));
  }

  private void onError(Throwable throwable) {
    Log.e("error", "error on getting response", throwable);
    eventsSubject.onNext(new Events.LoadingEvent(false));
    this.isElevatorProgressVisible.set(false);
    this.isDelayReportProgressVisible.set(false);
    eventsSubject.onError(throwable);
  }

  private void convertToVM(ElevatorStatus elevatorStatus) {
    String station = elevatorStatus.getRoot().getBsa().get(0).getStation();
    this.elevatorStation.set(station.equals("") ? "All" : station);
    this.elevatorStatus.set(
        elevatorStatus.getRoot().getBsa().get(0).getDescription().getCdataSection());
    this.isElevaorWorking.set(!station.equals("BART"));
  }

  private <R> R convertToVM(DelayReport delayReport) {
    this.delayDescription.set(
        delayReport.getRoot().getBsa().get(0).getDescription().getCdataSection());
    this.reportDate.set(delayReport.getRoot().getDate());
    this.reportTime.set(delayReport.getRoot().getTime());
    String delayStation = delayReport.getRoot().getBsa().get(0).getStation();
    this.reportStation.set(delayStation.equals("") ? "None" : delayStation);
    this.isNotDelay.set(delayStation.equals(""));
    return null;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    compositeDisposable.clear();
  }
}
