package com.artk.gallery.api;

import com.artk.gallery.data.Picture;

import java.util.List;

public interface DataProviderCallback {

    void onDataLoaded(List<Picture> pictures);

    void onFailedToLoad(Throwable throwable);

}
