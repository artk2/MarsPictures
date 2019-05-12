package com.artk.gallery.api;

import android.support.annotation.NonNull;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataProvider {

    /**
     * a calendar object is used to manage dates.
     * each time loadNext() is called, the date is decremented before making a request
     */
    private final Calendar calendar = Calendar.getInstance();
    private DataProviderCallback callback;

    public DataProvider(DataProviderCallback callback) {
        calendar.setTime(new Date());
        this.callback = callback;
    }

    /**
     * load next pile of pictures
     * the result will be passed to callback provided in constructor
     */
    public void loadNext(){
        calendar.add(Calendar.DATE, -1); // set calendar to previous day
        Date reqDate = calendar.getTime();
        Format formatter = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String date = formatter.format(reqDate);

        // make api calls
        // the callback will fire multiple times which is fine
        for(String rover : RetrofitClient.ROVERS){
            makeCall(date, rover, callback);
        }
    }

    private void makeCall(String date, String rover, DataProviderCallback callback){
        RetrofitClient.getInstance()
                .getAPI()
                .getPictures(rover, date, RetrofitClient.KEY)
                .enqueue(new Callback<CallResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CallResponse> call, @NonNull Response<CallResponse> response) {
                        if(response.body() == null) {
                            callback.onFailedToLoad(new NullPointerException("Null response body"));
                        }
                        callback.onDataLoaded(response.body().pictures());
                    }

                    @Override
                    public void onFailure(Call<CallResponse> call, Throwable t) {
                        callback.onFailedToLoad(new Exception(t));
                    }
                });
    }

}
