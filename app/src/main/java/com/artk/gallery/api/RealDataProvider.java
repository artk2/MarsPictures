package com.artk.gallery.api;

import android.support.annotation.NonNull;

import com.artk.gallery.data.Picture;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Loads real pictures from the server.
 */
public class RealDataProvider implements DataProvider {

    /**
     * a calendar object is used to manage dates.
     * each time loadNext() is called, the date is decremented before making a request
     */
    private final Calendar calendar = Calendar.getInstance();
    private DataProvider.Callback callback;

    /**
     * a response counter
     * the data will be returned when amount of responses reaches the amount of calls
     */
    private volatile int responses;

    /**
     * a temporary list to store pictures until all calls are responded
     */
    private volatile List<Picture> newPictures;

    RealDataProvider(DataProvider.Callback callback) {
        calendar.setTime(new Date());
        this.callback = callback;
    }

    /**
     * load next pile of pictures
     * the result will be passed to callback provided in constructor
     */
    @Override
    public void loadNext() {
        calendar.add(Calendar.DATE, -1); // set calendar to previous day
        Date reqDate = calendar.getTime();
        Format formatter = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String date = formatter.format(reqDate);

        // reset list
        newPictures = new ArrayList<>();
        responses = 0;

        // make api calls
        for (String rover : RetrofitClient.ROVERS) {
            makeCall(date, rover);
        }
    }

    private void makeCall(String date, String rover) {
        RetrofitClient.getInstance()
                .getAPI()
                .getPictures(rover, date, RetrofitClient.KEY)
                .enqueue(new retrofit2.Callback<CallResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CallResponse> call, @NonNull Response<CallResponse> response) {
                        if (response.body() == null) {
                            RealDataProvider.this.onFailure(new NullPointerException("Null response body"));
                        }
                        onDataReceived(response.body().pictures());
                    }

                    @Override
                    public void onFailure(Call<CallResponse> call, Throwable t) {
                        RealDataProvider.this.onFailure(t);
                    }
                });
    }

    private synchronized void onDataReceived(List<Picture> pictures) {
        newPictures.addAll(pictures);
        if (++responses == RetrofitClient.ROVERS.length) {
            List<Picture> immutable = Collections.unmodifiableList(newPictures);
            callback.onDataLoaded(immutable);
        }
    }

    private synchronized void onFailure(Throwable t) {
        if (responses >= RetrofitClient.ROVERS.length) return;
        t.printStackTrace();
        callback.onFailedToLoad(t);

        responses = RetrofitClient.ROVERS.length; // this guarantees callbacks won't fire anymore
        calendar.add(Calendar.DATE, 1); // reset date
    }

}
