package com.example.btl1server.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGeoCoordinates {
    @GET("map/api/geocode/json")
    Call<String> getGeoCode(@Query("address") String address); // địa chỉ

    @GET("map/api/geocode/json")
    Call<String> getDirections(@Query("origin") String origin, @Query("destinatioon") String destination); // nơi đến


}
