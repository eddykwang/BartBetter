package com.example.eddystudio.bartable.Repository;


import android.util.Log;

import com.example.eddystudio.bartable.Repository.Response.Bart;

import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
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

    public io.reactivex.Observable<Bart> getResponse(){
        return io.reactivex.Observable.fromCallable(() -> bartService.bartEstmate().execute())
                .map(response -> response.body())
                .doOnError(error -> error(error))
                .subscribeOn(Schedulers.io());
    }

    private void error(Throwable e) {
        Log.e("error", "error", e);
    }


}
