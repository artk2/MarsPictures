package com.artk.gallery.api.client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface API {

    @GET("/mars-photos/api/v1/rovers/{rover}/photos")
    Call<CallResponse> getPictures(@Path("rover") String rover, @Query("earth_date") String date, @Query("api_key") String key);

}
