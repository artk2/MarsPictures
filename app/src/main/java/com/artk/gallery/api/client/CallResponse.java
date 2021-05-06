package com.artk.gallery.api.client;

import androidx.annotation.NonNull;

import com.artk.gallery.data.Picture;

import java.util.List;

public class CallResponse {

    private List<Picture> pictureList;

    public CallResponse(@NonNull List<Picture> pictureList) {
        this.pictureList = pictureList;
    }

    @NonNull
    public List<Picture> pictures() {
        return pictureList;
    }
}
