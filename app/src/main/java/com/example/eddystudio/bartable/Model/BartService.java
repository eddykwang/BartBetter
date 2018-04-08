package com.example.eddystudio.bartable.Model;


import com.example.eddystudio.bartable.Model.Response.DelayReport.DelayReport;
import com.example.eddystudio.bartable.Model.Response.ElevatorStatus.ElevatorStatus;
import com.example.eddystudio.bartable.Model.Response.EstimateResponse.Bart;
import com.example.eddystudio.bartable.Model.Response.Stations.BartStations;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BartService {
    @GET("api/etd.aspx")
    Call<Bart> bartEstmate(@Query("cmd") String fromStation, @Query("orig") String origin, @Query("key") String key, @Query("json") String isJason);

    @GET("api/stn.aspx?cmd=stns&key=MW9S-E7SL-26DU-VV8V&json=y")
    Call<BartStations> bartStations();

    @GET("api/bsa.aspx?cmd=bsa&key=MW9S-E7SL-26DU-VV8V&json=y")
    Call<DelayReport> delayReport();

    @GET("api/bsa.aspx?cmd=elev&key=MW9S-E7SL-26DU-VV8V&json=y")
    Call<ElevatorStatus> elevatorStatus();
}
