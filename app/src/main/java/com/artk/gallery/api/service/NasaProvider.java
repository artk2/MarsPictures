package com.artk.gallery.api.service;

import android.support.annotation.NonNull;

import com.artk.gallery.api.client.CallResponse;
import com.artk.gallery.api.client.RetrofitClient;
import com.artk.gallery.app.Log;
import com.artk.gallery.data.Picture;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Loads real pictures from the server.
 */
public class NasaProvider implements DataProvider {

    // a calendar object is used to manage dates.
    // each time loadNext() is called, the date is decremented before making a request
    private final Calendar calendar = Calendar.getInstance();
    private Format formatter = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
    private boolean loading = false;

    // a response counter
    // the data will be returned when amount of responses reaches the amount of calls
    private int responses;

    // a temporary list to store pictures until all calls are responded
    private volatile List<Picture> newPictures;

    // if loadNext() is called before previous request has been responded, the call will be queued
    private Queue<Callback> queue;
    private Call call;

    public NasaProvider() {
        calendar.setTime(new Date());
        queue = new LinkedList<>();
    }

    @Override
    public void loadNext(Callback callback) {
        queue.add(callback);
        Log.d("added request to queue");
        if (loading) return;

        loadNext();
    }

    @Override
    public void clear() {
        Log.i("clearing");
        if (call != null) call.cancel();
        queue.clear();
    }

    private void loadNext() {
        loading = true;
        newPictures = new ArrayList<>();
        responses = 0;

        Log.d("queue size: " + queue.size());

        String date = getNextDate();
        for (String rover : RetrofitClient.ROVERS) {
            makeApiCall(date, rover);
        }
    }

    private String getNextDate() {
        calendar.add(Calendar.DATE, -1); // set calendar to previous day
        return getCurrentDate();
    }

    private String getCurrentDate() {
        Date reqDate = calendar.getTime();
        return formatter.format(reqDate);
    }

    protected void makeApiCall(String date, String rover) {
        call = RetrofitClient.getInstance()
                .getAPI()
                .getPictures(rover, date, RetrofitClient.KEY);

        call.enqueue(new retrofit2.Callback<CallResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CallResponse> call, @NonNull Response<CallResponse> response) {
                        if (call.isCanceled()) Log.i("Call has been canceled");
                        else if (response.body() == null)
                            NasaProvider.this.onFailure(new NullPointerException("Null response body"));
                        else onDataReceived(response.body().pictures());
                    }

                    @Override
                    public void onFailure(@NonNull Call<CallResponse> call, @NonNull Throwable t) {
                        NasaProvider.this.onFailure(t);
                    }
                });
    }

    // The api hasn't been returning any new images since 28.10.2019.
    // This is temporary code that deals with this issue
    private boolean noPicturesReceivedYet = true;
    private int consecutiveDaysWithoutPictures = 0;

    private void rollbackIfNoPicturesLastWeek(int newPictures) {
        if (noPicturesReceivedYet) {
            if (newPictures == 0) {
                if (++consecutiveDaysWithoutPictures == 7) {
                    calendar.set(2019, 9 - 1, 29);
                }
            } else {
                noPicturesReceivedYet = false;
            }
        }
    }

    protected void onDataReceived(List<Picture> pictures) {
        newPictures.addAll(pictures);
        if (++responses == RetrofitClient.ROVERS.length) {
            loading = false;
            List<Picture> immutable = Collections.unmodifiableList(newPictures);
            rollbackIfNoPicturesLastWeek(immutable.size());
            Callback callback = queue.poll();
            if (callback != null) callback.onDataLoaded(getCurrentDate(), immutable);
            else Log.e("Unexpected: no callback for this request");

            if (!queue.isEmpty()) loadNext();
        }
    }

    protected void onFailure(Throwable t) {
        if (responses >= RetrofitClient.ROVERS.length) return;
        Log.e("Failure while requesting pictures", t);

        loading = false;
        responses = RetrofitClient.ROVERS.length; // this guarantees callbacks won't fire anymore
        calendar.add(Calendar.DATE, 1); // reset date

        Callback callback = queue.poll();
        if (callback != null) callback.onFailedToLoad(getCurrentDate(), t);
        else Log.e("Unexpected: no callback for this request");

        if (!queue.isEmpty()) loadNext();
    }

}
