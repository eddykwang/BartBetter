package com.eddystudio.bartbetter.Model;


import com.eddystudio.bartbetter.Model.Response.DelayReport.DelayReport;
import com.eddystudio.bartbetter.Model.Response.ElevatorStatus.ElevatorStatus;
import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Bart;
import com.eddystudio.bartbetter.Model.Response.Schedule.ScheduleFromAToB;
import com.eddystudio.bartbetter.Model.Response.Stations.BartStations;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BartService {
    @GET("api/etd.aspx")
    Call<Bart> bartEstmate(@Query("cmd") String fromStation, @Query("orig") String origin, @Query("key") String key, @Query("json") String isJson);

    @GET("api/stn.aspx?cmd=stns&key=MW9S-E7SL-26DU-VV8V&json=y")
    Call<BartStations> bartStations();

    @GET("api/bsa.aspx?cmd=bsa&key=MW9S-E7SL-26DU-VV8V&json=y")
    Call<DelayReport> delayReport();

    @GET("api/bsa.aspx?cmd=elev&key=MW9S-E7SL-26DU-VV8V&json=y")
    Call<ElevatorStatus> elevatorStatus();

    //api for get schedule detail from A to B
    //https://api.bart.gov/api/sched.aspx? cmd=depart  &orig=DALY&  dest=FRMT&  date=now&  key=MW9S-E7SL-26DU-VV8V&  b=0  &a=3  &l=1&  json=y
    @GET("api/sched.aspx")
    Call<ScheduleFromAToB> routeSchedules(@Query("cmd") String cmd, @Query("orig") String origin,
                                          @Query("dest") String destination, @Query("data") String data, @Query("key") String key,
                                          @Query("b") String b, @Query("a") String a, @Query("l") String l, @Query("json") String isJson);

}
