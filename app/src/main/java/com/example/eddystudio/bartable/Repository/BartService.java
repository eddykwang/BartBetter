package com.example.eddystudio.bartable.Repository;


import com.example.eddystudio.bartable.Repository.Response.Bart;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BartService {
    @GET("api/etd.aspx?cmd=etd&orig=mont&key=MW9S-E7SL-26DU-VV8V&json=y")
    Call<Bart> bartEstmate();
}
