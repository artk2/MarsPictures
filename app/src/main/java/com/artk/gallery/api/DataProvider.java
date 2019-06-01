package com.artk.gallery.api;

/**
 * Request DataProviders to get one a class implementing this interface and receive pictures
 */
public interface DataProvider {

    /**
     * requests new pictures from server
     */
    void loadNext();

}
