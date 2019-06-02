package com.artk.gallery.api;

import com.artk.gallery.data.Picture;

import java.util.List;

/**
 * Request DataProviders to get one a class implementing this interface and receive pictures
 */
public interface DataProvider {

    /**
     * requests new pictures from server
     */
    void loadNext();

    /**
     * a callback to receive requested data
     */
    interface Callback {

        void onDataLoaded(List<Picture> pictures);

        void onFailedToLoad(Throwable throwable);

    }

}
