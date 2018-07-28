package com.artk.gallery;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.net.UnknownHostException;
import java.util.List;


public class GalleryViewModel extends AndroidViewModel {

    private PictureManager pictureManager;
    private MutableLiveData<List<Picture>> pictures;
    private MutableLiveData<List<Picture>> favorites;

    private volatile boolean loading = false; // for private use
    private volatile int picsToLoad = 0;

    private MutableLiveData<Boolean> dataLoading; // for UI
    private MutableLiveData<String> message;

    public GalleryViewModel(@NonNull Application application){
        super(application);
        pictureManager = new PictureManager();
    }

    public LiveData<List<Picture>> getPictures(){
        if (pictures == null){
            pictures = new MutableLiveData<>();
            loadPictures();
        }
        return pictures;
    }

    public LiveData<List<Picture>> getFavorites(){
        if (favorites == null){
            favorites = new MutableLiveData<>();
            favorites.setValue(pictureManager.loadFavorites(getApplication()));
        }
        return favorites;
    }

    public void imageLoaded(){
        picsToLoad--;
        if(dataLoading.getValue() != null && dataLoading.getValue() == true){
            dataLoading.setValue(false);
        }
    }

    public boolean finishedLoading(){
        return picsToLoad <= 0;
    }

    public void loadNext(){
        if(!loading) { // preventing multiple calls to api
            loadPictures();
        }
    }

    private void loadPictures(){
        loading = true;
        pictureManager.loadData(new PictureManager.PictureLoaderCallback() {
            @Override
            public void onDataLoaded(List<Picture> updatedList, int amountAdded) {
                pictures.setValue(updatedList);
                if(picsToLoad > 0) picsToLoad += amountAdded;
                else picsToLoad = amountAdded; // the value became negative if user scrolled up
                loading = false;
            }

            @Override
            public void onFailedToLoad(@NonNull Exception e) {
                String msg;
                if(e instanceof UnknownHostException){
                    msg = getApplication().getString(R.string.error_server_unavailable);
                } else {
                    msg = getApplication().getString(R.string.error_failed_to_load);
                }
                loading = false;
                if(dataLoading.getValue() != null && dataLoading.getValue() == true){
                    dataLoading.setValue(false);
                }
                if(message != null){
                    String oldMsg = message.getValue();
                    if(oldMsg == null)
                        message.setValue(msg);
                    else if(!oldMsg.equals(msg))
                        message.setValue(msg);
                }
            }
        });
    }

    public void updateFavorites(Picture picture) {
        List<Picture> list = favorites.getValue();
        int id = picture.getId();
        if(picture.isFavorite()){
            boolean exists = false;
            for(Picture pic : list){
                if(pic.getId() == id){
                    exists = true;
                    break;
                }
            }
            if(!exists) list.add(picture);
        } else {
            int idToRemove = -1;
            for(int i = 0; i < list.size(); i++){
                if(list.get(i).getId() == id){
                    idToRemove = i;
                }
            }
            if(idToRemove != -1)
                list.remove(idToRemove);
        }
        pictureManager.saveFavorites(getApplication(), list);
        favorites.setValue(list);
    }

    public LiveData<Boolean> isLoading(){
        if(dataLoading == null){
            dataLoading = new MutableLiveData<>();
            dataLoading.setValue(true);
        }
        return dataLoading;
    }

    public LiveData<String> getMessage(){
        if(message == null){
            message = new MutableLiveData<>();
        }
        return message;
    }

    private boolean canShowMessage = true;
    public boolean canShowMessage(){
        return canShowMessage;
    }

    public void messageShown() {
        canShowMessage = false;
    }
}
