package com.eddystudio.bartbetter.Model;

import android.util.Pair;

import com.eddystudio.bartbetter.DI.Application;
import com.eddystudio.bartbetter.Model.Response.DelayReport.DelayReport;
import com.eddystudio.bartbetter.Model.Response.ElevatorStatus.ElevatorStatus;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Bart;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Schedule.Trip;
import com.eddystudio.bartbetter.Model.Response.Stations.BartStations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Triple;
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
        .map(Response::body)
        .subscribeOn(Schedulers.io());
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
        .concatMap(pair ->
            Observable.fromCallable(
                () -> bartService.routeSchedules("depart", pair.first, pair.second, "now", KEY, "0", "4", "0", "y")
                    .execute())
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

//  public Observable<Etd> getAccurateEtdTime(List<Pair<String, String>> routes) {
//    return Observable.fromIterable(routes)
//        .flatMap(route -> getOneRouteSchedules(route).flatMap(r -> {
//              String from;
//              String dest;
//
//              dest = (r.getRoot().getSchedule().getRequest().getTrip().get(0).getLeg().get(0).getTrainHeadStation());
//              from = (r.getRoot().getSchedule().getRequest().getTrip().get(0).getOrigin());
//
//              return getEstimate(from).map(d -> {
//                    List<Etd> etds;
//                    etds = d.getRoot().getStation().get(0).getEtd();
//                    Etd est = null;
//                    for(Etd etd : etds) {
//                      if(etd.getAbbreviation().equals(dest)) {
//                        est = (etd);
//                        break;
//                      }
//                    }
//                    return est;
//                  }
//              );
//            })
//        )
//        .subscribeOn(Schedulers.io());
//  }

  public Observable<Triple<Etd, String,String>> getAccurateEtdTime(List<Pair<String, String>> routes) {
    return getRouteSchedules(routes)
        .concatMap(r -> {
          Trip trip = r.getRoot().getSchedule().getRequest().getTrip().get(0);

          final Etd[] result = {new Etd()};
          return getEstimate(trip.getLeg().get(0).getOrigin())
              .map(d -> {
                    List<Etd> etds = d.getRoot().getStation().get(0).getEtd();
                    for(Etd etd : etds) {
                      if(etd.getAbbreviation().equals(trip.getLeg().get(0).getTrainHeadStation())) {
                        result[0] = etd;
                        break;
                      }
                    }
                    return new Triple<>(result[0], trip.getOrigin(), trip.getDestination());
                  }
              );
        });
  }

}
