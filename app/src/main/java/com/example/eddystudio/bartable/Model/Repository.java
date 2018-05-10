package com.example.eddystudio.bartable.Model;

import android.util.Pair;

import com.example.eddystudio.bartable.Model.Response.DelayReport.DelayReport;
import com.example.eddystudio.bartable.Model.Response.ElevatorStatus.ElevatorStatus;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Bart;
import com.example.eddystudio.bartable.Model.Response.Schedule.ScheduleFromAToB;
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
    private static final String KEY = "MW9S-E7SL-26DU-VV8V";

    @Inject
    Retrofit retrofit;

    @Inject
    public Repository() {
        Application.getAppComponet().inject(this);
        bartService = retrofit.create(BartService.class);
    }

    //?cmd=etd&orig={fromStation}&key=MW9S-E7SL-26DU-VV8V&json=y"
    public io.reactivex.Observable<Results> getEstimate(
            List<Pair<String, String>> fromStation) {
        return io.reactivex.Observable
                .fromIterable(fromStation)
                .flatMap(pair ->
                                Observable.fromCallable(
                                        () -> bartService.bartEstmate("etd", pair.first, KEY, "y")
                                                .execute())
//                .map(Response::body)
                                        .map(bartResponse -> {
                                            if (bartResponse != null)
//                      return bartResponse.body();
                                                return new OnSuccess(new Pair<>(bartResponse.body(), pair.second));
                                            else
                                                return new OnFail();
                                        })
//                .map(bart -> new Pair<>(bart, pair.second))
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

    //https://api.bart.gov/api/sched.aspx? cmd=depart  &orig=DALY&  dest=FRMT&  date=now&  key=MW9S-E7SL-26DU-VV8V&  b=0  &a=3  &l=1&  json=y
    public Observable<ScheduleFromAToB> getRouteSchedules(List<Pair<String, String>> routes) {
        return Observable
                .fromIterable(routes)
                .flatMap(pair ->
                        Observable.fromCallable(
                                () -> bartService.routeSchedules("depart", pair.first, pair.second, "now", KEY, "0", "3", "1", "y")
                                        .execute())
                                .map(Response::body)
                )

                .subscribeOn(Schedulers.io());
    }

    interface Results {
    }

    public class OnSuccess implements Results {
        private final Pair<Bart, String> pair;

        public OnSuccess(Pair<Bart, String> pair) {
            this.pair = pair;
        }

        public Pair<Bart, String> getPair() {
            return pair;
        }
    }

    public class OnFail implements Results {

    }

}
