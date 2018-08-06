package com.eddystudio.bartbetter.Model;

import android.util.Log;
import android.util.Pair;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Response.DelayReport.DelayReport;
import com.eddystudio.bartbetter.Model.Response.ElevatorStatus.ElevatorStatus;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Bart;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.Model.Response.Stations.BartStations;

import java.io.IOException;
import java.net.SocketException;
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
  public Observable<ScheduleFromAToB> getRouteSchedules(List<Pair<String, String>> routes) {
    return Observable
        .fromIterable(routes)
        .onErrorResumeNext(Observable.empty())
        .concatMap(pair ->
            Observable.fromCallable(
                () -> bartService.routeSchedules("depart", pair.first, pair.second, "now", KEY, "0", "4", "0", "y")
                    .execute())
                .subscribeOn(Schedulers.io())
                .map(Response::body)
        )

        .subscribeOn(Schedulers.io());
  }

  //https://api.bart.gov/api/sched.aspx? cmd=depart  &orig=DALY&  dest=FRMT&  date=now&  key=MW9S-E7SL-26DU-VV8V&  b=0  &a=4  &l=1&  json=y
  public Observable<ScheduleFromAToB> getOneRouteSchedules(Pair<String, String> route) {
    return Observable.fromCallable(
        () -> bartService.routeSchedules("depart", route.first, route.second, "now", KEY, "0", "3", "0", "y")
            .execute())
        .map(Response::body)
        .subscribeOn(Schedulers.io());
  }

  public Observable<AccurateEtdResult> getAccurateEtdTime(List<Pair<String, String>> routes) {
    return getRouteSchedules(routes)
        .concatMap(r -> {
          Trip trip = r.getRoot().getSchedule().getRequest().getTrip().get(0);
          final Etd[] result = {new Etd()};
          final Throwable[] error = new Throwable[1];
          return getEstimate(trip.getLeg().get(0).getOrigin())
              .onErrorResumeNext(err -> {
                error[0] = err;
                return Observable.just(new Bart());
              })
              .map(d -> {
                    if(d.getRoot() != null) {
                      List<Etd> etds = d.getRoot().getStation().get(0).getEtd();
                      for(Etd etd : etds) {
                        if(etd.getAbbreviation().equals(trip.getLeg().get(0).getTrainHeadStation())) {
                          result[0] = etd;
                          break;
                        }
                      }
                      return new OnSuccess(new EtdResult(result[0], trip.getOrigin(), trip.getDestination()));
                    } else {
                      return new OnError(new Throwable(error[0]), r.getRoot().getOrigin(), r.getRoot().getDestination());
                    }

                  }
              );
        });
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
