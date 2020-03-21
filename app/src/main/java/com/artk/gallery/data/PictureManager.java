package com.artk.gallery.data;

import android.os.Handler;

import com.artk.gallery.api.service.DataProvider;
import com.artk.gallery.api.service.NasaProvider;
import com.artk.gallery.app.Log;

import java.util.List;

/**
 * calls the api layer and returns pictures from server
 */
public class PictureManager {

    private DataProvider dataProvider;
    private PictureReceiver receiver;

    public PictureManager() {
        dataProvider = new NasaProvider();
    }

    /**
     * load next pile of pictures
     * the result will be passed to callback provided in constructor
     */
    public void loadNext() {
        Log.d("requested next pile of pictures");

        DataProvider.Callback callback = new DataProvider.Callback() {
            @Override
            public void onDataLoaded(String date, List<Picture> pictures) {
                if (pictures.size() == 0) {
                    Log.i(date + ": no pictures");
                    // if empty list was received, load next
//                    loadNext();
                    new Handler().postDelayed(() -> dataProvider.loadNext(this), 1100);
                } else {
                    receiver.onNext(pictures);
                }
            }

            @Override
            public void onFailedToLoad(String date, Throwable throwable) {
                receiver.onError(throwable);
            }
        };

        dataProvider.loadNext(callback);
    }

    public void setPictureReceiver(PictureReceiver receiver) {
        this.receiver = receiver;
    }

    public void removeReceiver() {
        dataProvider.clear();
        this.receiver = null;
    }

}
