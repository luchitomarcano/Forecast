package com.example.luis.forecast2.Retrofit;

import com.example.luis.forecast2.model.Forecast;

import java.util.ArrayList;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IMyAPI {

    @GET("/data/2.5/weather")
    Observable<Forecast> getForecast(@Query("lat") double latitude,
                                     @Query("lon") double longitude,
                                     @Query("appid") String token,
                                     @Query("units") String units);

    @GET("/data/2.5/weather")
    Observable<Forecast> getForecast(@Query("q") String city,
                                     @Query("appid") String token,
                                     @Query("units") String units);
}