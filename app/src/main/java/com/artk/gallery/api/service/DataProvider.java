package com.artk.gallery.api.service;

import com.artk.gallery.data.Picture;

import java.util.List;

/**
 * Protocol for a class that loads pictures from a remote source
 */
public interface DataProvider {

    /**
     * load next chunk of pictures
     */
    void loadNext(Callback callback);

    /**
     * clear all resources and cancel calls
     */
    void clear();

    interface Callback {

        void onDataLoaded(String date, List<Picture> pictures);

        void onFailedToLoad(String date, Throwable throwable);

    }

}
