package com.artk.gallery.data;

import java.util.List;

public interface PictureReceiver {

    void onDataLoaded(List<Picture> pictures);

    void onFailedToLoad(Throwable throwable);

}
