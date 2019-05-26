package com.artk.gallery.api;

import android.support.annotation.NonNull;

import com.artk.gallery.data.Picture;

import java.util.List;

/**
 * the API wraps data into an object
 * so we need a pojo to receive it
 */
class CallResponse {

    private List<Picture> pictureList;

    CallResponse(@NonNull List<Picture> pictureList) {
        this.pictureList = pictureList;
    }

    @NonNull
    List<Picture> pictures(){
        return pictureList;
    }
}
