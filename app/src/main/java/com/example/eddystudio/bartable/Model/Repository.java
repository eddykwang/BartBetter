package com.example.eddystudio.bartable.Model;

import android.util.Pair;
import com.example.eddystudio.bartable.Model.Response.DelayReport.DelayReport;
import com.example.eddystudio.bartable.Model.Response.ElevatorStatus.ElevatorStatus;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Bart;
import com.example.eddystudio.bartable.Model.Response.Stations.BartStations;
import com.example.eddystudio.bartable.DI.Application;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Repository {
  private BartService bartService;

  @Inject
  Retrofit retrofit;

  @Inject
  public Repository() {
    Application.getAppComponet().inject(this);
    bartService = retrofit.create(BartService.class);
  }

  //?cmd=etd&orig={fromStation}&key=MW9S-E7SL-26DU-VV8V&json=y"
  public io.reactivex.Observable<Pair<Bart, String>> getEstimate(List<Pair<String, String>> fromStation) {
    return io.reactivex.Observable
        .fromIterable(fromStation)
        .flatMap(pair ->
            Observable.fromCallable(
                () -> bartService.bartEstmate("etd", pair.first, "MW9S-E7SL-26DU-VV8V", "y")
                    .execute())
                .map(Response::body)
                .map(bart -> new Pair<>(bart, pair.second))
            .onErrorResumeNext(Observable.empty())
        )
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

}
