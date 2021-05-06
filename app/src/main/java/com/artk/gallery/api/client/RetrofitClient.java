package com.artk.gallery.api.client;

import com.artk.gallery.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance;
    private static API api;
    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/";

    // query parameters
    public static final String KEY = BuildConfig.API_KEY;
    public static final String[] ROVERS = {
            "curiosity",
            "opportunity",
            "perseverance",
            //"spirit" - нет фото с 2010
    };

    /**
     * getInstance() and getAPI() methods are package private.
     * The only class in api package that the app needs to communicate with is DataProvider
     */
    public static RetrofitClient getInstance() {
        if (instance == null) instance = new RetrofitClient();
        return instance;
    }

    public API getAPI() {
        return api;
    }

    private RetrofitClient() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG) okHttpClientBuilder.addInterceptor(logging);

        okHttpClientBuilder.callTimeout(30, TimeUnit.SECONDS);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CallResponse.class, new ResponseDeserializer())
                .create();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson));

        Retrofit retrofit = builder.build();

        api = retrofit.create(API.class);
    }

}