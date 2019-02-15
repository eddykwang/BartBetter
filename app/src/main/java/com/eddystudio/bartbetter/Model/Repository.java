package com.eddystudio.bartbetter.Model;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Response.DelayReport.DelayReport;
import com.eddystudio.bartbetter.Model.Response.ElevatorStatus.ElevatorStatus;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Bart;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Response.Fares.Fares;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.Model.Response.Stations.BartStations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Repository {
  private BartService bartService;
  private static final String KEY = "MW9S-E7SL-26DU-VV8V";

  @Inject
  Retrofit retrofit;

  @Inject
  public Repository() {
    Application.getAppComponet().inject(this);
    bartService = retrofit.create(BartService.class);

    RxJavaPlugins.setErrorHandler(e -> {
      if(e instanceof UndeliverableException) {
        e = e.getCause();
      }
      if(e instanceof IOException) {
        // fine, irrelevant network problem or API that throws on cancellation
        return;
      }
      if(e instanceof InterruptedException) {
        // fine, some blocking code was interrupted by a dispose call
        return;
      }
      if((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
        // that's likely a bug in the application
        Thread.currentThread().getUncaughtExceptionHandler()
            .uncaughtException(Thread.currentThread(), e);
        return;
      }
      if(e instanceof IllegalStateException) {
        // that's a bug in RxJava or in a custom operator
        Thread.currentThread().getUncaughtExceptionHandler()
            .uncaughtException(Thread.currentThread(), e);
        return;
      }
      Log.e("RxError", "Undeliverable exception received, not sure what to do", e);
    });
  }

  //?cmd=etd&orig={fromStation}&key=MW9S-E7SL-26DU-VV8V&json=y"
  public Observable<Pair<Bart, String>> getListEstimate(
      List<Pair<String, String>> fromStation) {
    return Observable
        .fromIterable(fromStation)
        .concatMap(pair ->
            Observable.fromCallable(
                () -> bartService.bartEstmate("etd", pair.first, KEY, "y")
                    .execute())
                .map(Response::body)
                .map(bart -> new Pair<>(bart, pair.second))
        )
        .subscribeOn(Schedulers.io());
  }


  //?cmd=etd&orig={fromStation}&key=MW9S-E7SL-26DU-VV8V&json=y"
  public Observable<Bart> getEstimate(String fromStation) {
    return Observable.fromCallable(
        () -> bartService.bartEstmate("etd", fromStation, KEY, "y")
            .execute())
        .subscribeOn(Schedulers.io())
        .onErrorResumeNext(Observable.error(new Throwable()))
        .map(Response::body);
  }

  public Observable<BartStations> getStations() {
    return Observable.fromCallable(() -> bartService.bartStations().execute())
        .map(Response::body)
        .subscribeOn(Schedulers.io());
  }

  public Observable<DelayReport> getDelayReport() {
    return Observable.fromCallable(() -> bartService.delayReport().execute())
        .map(Response::body)
        .subscribeOn(Schedulers.io());
  }

  public Observable<ElevatorStatus> getElevatorStatus() {
    return Observable.fromCallable(() -> bartService.elevatorStatus().execute())
        .map(Response::body)
        .subscribeOn(Schedulers.io());
  }

  //https://api.bart.gov/api/sched.aspx? cmd=depart  &orig=DALY&  dest=FRMT&  date=now&  key=MW9S-E7SL-26DU-VV8V&  b=0  &a=4  &l=1&  json=y
  public Observable<ScheduleFromAToB> getRouteSchedules(List<RouteModel> routes) {
    return Observable
        .fromIterable(routes)
        .onErrorResumeNext(Observable.empty())
        .concatMap(pair ->
            Observable.fromCallable(
                () -> bartService.routeSchedules("depart", pair.getFrom().getAbbr(), pair.getTo().getAbbr(), "now", "now", KEY, "1", "3", "0", "y")
                    .execute())
                .subscribeOn(Schedulers.io())
                .map(Response::body)
        )

        .subscribeOn(Schedulers.io());
  }

  //https://api.bart.gov/api/sched.aspx? cmd=depart  &orig=DALY&  dest=FRMT&  date=now&  key=MW9S-E7SL-26DU-VV8V&  b=0  &a=4  &l=1&  json=y
  public Observable<ScheduleFromAToB> getOneRouteSchedules(Pair<String, String> route, String date, String time, boolean isDepart) {
    if(date == null) {
      date = "now";
    }

    if(time == null) {
      time = "now";
    }

    String finalTime = time;
    String finalDate = date;
    String depart = isDepart ? "depart" : "arrive";
    return Observable.fromCallable(
        () -> bartService.routeSchedules(depart, route.first, route.second, finalTime, finalDate, KEY, "0", "4", "0", "y")
            .execute())
        .map(Response::body)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Fares> getRouteFares(String origin, String dest, String date) {
    if(date == null) {
      date = "today";
    }
    String finalD = date;
    return Observable.fromCallable(() ->
        bartService.routeFares(origin, dest, finalD, KEY, "y")
            .execute())
        .map(Response::body)
        .subscribeOn(Schedulers.io());
  }

  @SuppressLint("NewApi")
  public Observable<AccurateEtdResult> getAccurateEtdTime(List<RouteModel> routes) {
    return getRouteSchedules(routes)
        .retryWhen(throwableObservable -> throwableObservable.zipWith(Observable.range(1, 3), (n, i) -> i))
        .concatMap(r -> {
          List<Trip> trips = r.getRoot().getSchedule().getRequest().getTrip();
          final List<Etd> etdList = new ArrayList<>();
          final Throwable[] error = new Throwable[1];
          return getEstimate(trips.get(0).getLeg().get(0).getOrigin())
              .retryWhen(throwableObservable -> throwableObservable.zipWith(Observable.range(1, 3), (n, i) -> i))
              .onErrorResumeNext(err -> {
                error[0] = err;
                return Observable.just(new Bart());
              })
              .map(d -> {
                    if(d.getRoot() != null) {
                      for(Etd etd : d.getRoot().getStation().get(0).getEtd()) {
                        for(Trip trip : trips) {
                          if(trip.getLeg().get(0).getTrainHeadStation().equalsIgnoreCase("San Francisco International Airport")) {
                            trip.getLeg().get(0).setTrainHeadStation("SFO/Millbrae");
                          }
                          if(trip.getLeg().get(0).getTrainHeadStation().equals(etd.getDestination())) {
                            etdList.add(etd);
                          }
                        }
                      }
                    }
                    if(etdList.isEmpty()) {
                      return new OnError(new Throwable("Unable to get response!"), trips.get(0).getOrigin(), trips.get(0).getDestination());
                    } else {
                      etdList.sort((t1, t2) -> convertStringToInt(t1.getEstimate().get(0).getMinutes()) - convertStringToInt(t2.getEstimate().get(0).getMinutes()));
                    }
                    return new OnSuccess(new EtdResult(etdList, trips.get(0).getOrigin(), trips.get(0).getDestination()));
                  }
              );
        });
  }

  private Integer convertStringToInt(String time) {
    return time.equalsIgnoreCase("Leaving") ? 0 : Integer.parseInt(time);
  }

  public interface AccurateEtdResult {}

  public class OnSuccess implements AccurateEtdResult {
    private final EtdResult etdResult;

    public OnSuccess(EtdResult etdResult) {this.etdResult = etdResult;}

    public EtdResult getEtdResult() {
      return etdResult;
    }
  }

  public class OnError implements AccurateEtdResult {
    private final Throwable error;
    private final String from;
    private final String to;

    public OnError(Throwable error, String from, String to) {
      this.error = error;
      this.from = from;
      this.to = to;
    }

    public Throwable getError() {
      return error;
    }

    public String getFrom() {
      return from;
    }

    public String getTo() {
      return to;
    }
  }

}
