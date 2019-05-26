package com.artk.gallery.api;

import com.artk.gallery.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance;
    private static API api;
    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/";

    // query parameters
    static final String KEY = "Qh5l5EUypdjnMp9Wd2Wq856F9qezwozXolND0Fw5"; // TODO: hide
    static final String[] ROVERS = {
            "curiosity",
            "opportunity",
            //"spirit" - нет фото с 2010
    };

    /**
     * getInstance() and getAPI() methods are package private.
     * The only class in api package that the app needs to communicate with is DataProvider
     */
    static RetrofitClient getInstance() {
        if (instance == null) instance = new RetrofitClient();
        return instance;
    }

    API getAPI() {
        return api;
    }

    private RetrofitClient() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG) okHttpClientBuilder.addInterceptor(logging);

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