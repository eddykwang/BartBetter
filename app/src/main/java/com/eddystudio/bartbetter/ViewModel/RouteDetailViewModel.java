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
import com.eddystudio.bartbetter.Model.Response.Fares.Fares;
import com.eddystudio.bartbetter.Model.Response.Fares.RouteFares;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.Model.Uilt;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RouteDetailViewModel {
  public ObservableField<String> from = new ObservableField<>("");
  public ObservableField<String> to = new ObservableField<>("");
  public ObservableInt color = new ObservableInt();
  public ObservableBoolean isArrive = new ObservableBoolean(true);
  public ObservableField<String> date = new ObservableField<>("Today");
  public ObservableField<String> time = new ObservableField<>("Now");
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private Subject<Events> eventsSubject = PublishSubject.create();
  private final String fromShortcut;
  private final String toShortcut;

  private int mYear, mMonth, mDay, mHour, mMinutes;

  @Inject
  public Repository repository;

  public enum ClickEvents {
    DATA_CLICK, TIME_CLICK, SET_BUTTON_CLICK, BOTTOM_SHEET_CLICK
  }

  @Inject
  public RouteDetailViewModel(String from, String to, int color) {
    fromShortcut = from;
    toShortcut = to;
    this.from.set(Uilt.getFullStationName(from));
    this.to.set(Uilt.getFullStationName(to));
    this.color.set(color);
    Application.getAppComponet().inject(this);
  }

  public void bsOnClicked() {
    eventsSubject.onNext(new Events.GetDataEvent(ClickEvents.BOTTOM_SHEET_CLICK));
  }

  public void dateClicked() {
    eventsSubject.onNext(new Events.GetDataEvent(ClickEvents.DATA_CLICK));
  }

  public void timeClicked() {
    eventsSubject.onNext(new Events.GetDataEvent(ClickEvents.TIME_CLICK));
  }

  public void setScheduleClicked() {
    eventsSubject.onNext(new Events.GetDataEvent(ClickEvents.SET_BUTTON_CLICK));
    String date = mMonth + "/" + mDay + "/" + mYear;
    String time = formatTime(mHour, mMinutes);
    getRoutesInfo(date, time, isArrive.get());
  }

  public Observable<Events> getEvents() {
    return eventsSubject.hide();
  }

  public void bottomSheetup() {
    final Calendar c = Calendar.getInstance();
    mYear = c.get(Calendar.YEAR);
    mMonth = c.get(Calendar.MONTH);
    mDay = c.get(Calendar.DAY_OF_MONTH);
    mHour = c.get(Calendar.HOUR);
    mMinutes = c.get(Calendar.MINUTE);

    date.set(mMonth + "/" + mDay + "/" + mYear);
    time.set(formatTime(mHour, mMinutes));
  }

  public void updateDate(int y, int m, int d) {
    mYear = y;
    mMonth = m;
    mDay = d;
    date.set(mMonth + "/" + mDay + "/" + mYear);

  }

  public void updateTime(int h, int m) {
    mHour = h;
    mMinutes = m;
    time.set(formatTime(h, m));
  }

  private String formatTime(int hour, int minutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minutes);
    Format formatter = new SimpleDateFormat("hh:mm a");
    return formatter.format(calendar.getTime());
  }

  public void getRoutesInfo(String date, String time, boolean isDepart) {
    compositeDisposable.add(
        repository.getOneRouteSchedules(new Pair<>(fromShortcut, toShortcut), date, time, isDepart)
            .doOnSubscribe(ignored -> eventsSubject.onNext(new Events.LoadingEvent(true)))
            .map(this::getTrips)
            .subscribe(trips -> getTrainLength(trips, date), this::handleError)
    );
  }

  private void getTrainLength(List<Trip> trips, String date) {
    List<Pair<String, String>> list = new ArrayList<>();
    list.add(new Pair<>(fromShortcut, toShortcut));
    compositeDisposable.add(
        repository.getListEstimate(list)
            .map(bart -> getEtd(bart.first))
            .concatMap(Observable::fromArray)
            .subscribe(len -> getFares(fromShortcut, toShortcut, date, trips, len),
                this::handleError)
    );
  }

  private void getFares(String origin, String dest, String date, List<Trip> trips, List<Etd> etds) {
    compositeDisposable.add(
        repository.getRouteFares(origin, dest, date)
            .subscribe(routeFares -> addToAdapter(trips, etds, routeFares), this::handleError, this::onComplete));
  }

  private List<Etd> getEtd(Bart bart) {
    Log.d("destination", bart.toString());
    return bart.getRoot().getStation().get(0).getEtd();
  }

  private void addToAdapter(List<Trip> trips, List<Etd> etds, Fares routeFares) {
    for(int i = 0; i < trips.size(); ++i) {
      RouteDetailRecyclerViewModel vm = new RouteDetailRecyclerViewModel(trips.get(i), etds, routeFares);
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
