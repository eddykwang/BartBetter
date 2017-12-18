package com.example.eddystudio.bartable.Repository;


import android.util.Log;

import com.example.eddystudio.bartable.Repository.Response.EstimateResponse.Bart;
import com.example.eddystudio.bartable.Repository.Response.Stations.BartStations;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {
    private BartService bartService;

    {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.bart.gov/")
                .client(provideOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        bartService = retrofit.create(BartService.class);
    }

    private OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder();
        okhttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        okhttpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
        okhttpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);
        return okhttpClientBuilder.build();
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

    private void error(Throwable e) {
        Log.e("error", "error", e);
    }


}
