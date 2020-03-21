package com.artk.gallery.data;

import java.util.List;

public interface PictureReceiver {

    void onNext(List<Picture> pictures);

    void onError(Throwable throwable);

}
