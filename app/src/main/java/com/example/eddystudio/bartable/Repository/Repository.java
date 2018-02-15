package com.example.eddystudio.bartable.Repository;


import android.util.Log;

import com.example.eddystudio.bartable.Repository.Response.DelayReport.DelayReport;
import com.example.eddystudio.bartable.Repository.Response.ElevatorStatus.ElevatorStatus;
import com.example.eddystudio.bartable.Repository.Response.EstimateResponse.Bart;
import com.example.eddystudio.bartable.Repository.Response.Stations.BartStations;
import com.example.eddystudio.bartable.application.Application;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {
    private BartService bartService;

    @Inject
    Retrofit retrofit;

    public Repository(){
        Application.getAppComponet().inject(this);
        bartService = retrofit.create(BartService.class);
    }

//?cmd=etd&orig={fromStation}&key=MW9S-E7SL-26DU-VV8V&json=y"

    public io.reactivex.Observable<Bart> getEstimate(String fromStation){
        return io.reactivex.Observable.fromCallable(() -> bartService.bartEstmate("etd", fromStation,"MW9S-E7SL-26DU-VV8V", "y").execute())
                .map(Response::body)
                .doOnError(this::error)
                .subscribeOn(Schedulers.io());
    }

    public Observable<BartStations> getStations(){
        return Observable.fromCallable(() -> bartService.bartStations().execute())
                .map(Response::body)
                .doOnError(this::error)
                .subscribeOn(Schedulers.io());
    }

    public Observable<DelayReport> getDelayReport(){
        return Observable.fromCallable(()-> bartService.delayReport().execute())
                .map(Response::body)
                .doOnError(this::error)
                .subscribeOn(Schedulers.io());
    }

    public Observable<ElevatorStatus> getElevatorStatus(){
        return Observable.fromCallable(()-> bartService.elevatorStatus().execute())
                .map(Response::body)
                .doOnError(this::error)
                .subscribeOn(Schedulers.io());
    }
    private void error(Throwable e) {
        Log.e("error", "error", e);
    }


}
