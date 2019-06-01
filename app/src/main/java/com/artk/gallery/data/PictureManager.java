package com.artk.gallery.data;

import android.util.Log;

import com.artk.gallery.api.DataProvider;
import com.artk.gallery.api.DataProviderCallback;
import com.artk.gallery.api.DataProviders;

import java.util.List;

/**
 * calls the api layer and returns pictures from server
 */
public class PictureManager{

    private DataProvider dataProvider;
    private PictureReceiver receiver;

    public PictureManager(PictureReceiver receiver) {
        this.receiver = receiver;
        dataProvider = DataProviders.create(dataProviderCallback);
    }

    /**
     * load next pile of pictures
     * the result will be passed to callback provided in constructor
     */
    public void loadPictures(){
        Log.v("artk2", "PictureManager: loadPictures() called");
        dataProvider.loadNext();
    }

    private DataProviderCallback dataProviderCallback = new DataProviderCallback() {
        @Override
        public void onDataLoaded(List<Picture> pictures) {
            Log.v("artk2", "PictureManager: received " + pictures.size() + " pictures");
            if(pictures.size() == 0){
                // if empty list was received, load next
                loadPictures();
            } else {
                receiver.onDataLoaded(pictures);
            }
        }

        @Override
        public void onFailedToLoad(Throwable throwable) {
            Log.v("artk2", "PictureManager: " +  throwable.getLocalizedMessage());
            receiver.onFailedToLoad(throwable);
        }
    };
}
