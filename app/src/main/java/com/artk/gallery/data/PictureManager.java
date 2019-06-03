package com.artk.gallery.data;

import com.artk.gallery.api.DataProvider;
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
        dataProvider.loadNext();
    }

    private DataProvider.Callback dataProviderCallback = new DataProvider.Callback() {
        @Override
        public void onDataLoaded(List<Picture> pictures) {
            if(pictures.size() == 0){
                // if empty list was received, load next
                loadPictures();
            } else {
                receiver.onDataLoaded(pictures);
            }
        }

        @Override
        public void onFailedToLoad(Throwable throwable) {
            receiver.onFailedToLoad(throwable);
        }
    };
}
