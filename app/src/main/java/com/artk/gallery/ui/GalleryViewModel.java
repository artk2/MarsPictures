package com.artk.gallery.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.artk.gallery.R;
import com.artk.gallery.data.FavoritesManager;
import com.artk.gallery.data.Picture;
import com.artk.gallery.data.PictureManager;
import com.artk.gallery.data.PictureReceiver;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class GalleryViewModel extends AndroidViewModel implements PictureReceiver {

    private PictureManager pictureManager;
    private FavoritesManager favoritesManager;
    private List<Picture> pictureList;
    private MutableLiveData<List<Picture>> pictures;
    private MutableLiveData<List<Picture>> favorites;
    private MutableLiveData<String> message;

    private /*volatile*/ boolean loading = false;

    public GalleryViewModel(@NonNull Application application){
        super(application);
        pictureList = new ArrayList<>();
        pictureManager = new PictureManager(this);
        favoritesManager = new FavoritesManager(getApplication());
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
            favorites.setValue(favoritesManager.getFavorites());
        }
        return favorites;
    }

    public LiveData<String> getMessage(){
        if(message == null){
            message = new MutableLiveData<>();
        }
        return message;
    }

    public void loadNextPictures(){
        if(!loading) { // preventing multiple calls
            loadPictures();
        } else {
            Log.v("artk2", "ViewModel loadNextPictures(): already loading");
        }
    }

    private void loadPictures(){
        Log.v("artk2", "ViewModel: loadPictures() called");
        loading = true;
        pictureManager.loadPictures();
    }

    @Override
    public void onDataLoaded(List<Picture> pictures) {
        pictureList.addAll(pictures);
        this.pictures.setValue(pictureList);
        loading = false;
    }

    @Override
    public void onFailedToLoad(Throwable t) {
        String msg;
        if(t instanceof UnknownHostException){
            msg = getApplication().getString(R.string.error_server_unavailable);
        } else {
            msg = getApplication().getString(R.string.error_failed_to_load);
        }
        loading = false;

        if(message != null){
            String oldMsg = message.getValue();
            if(oldMsg == null || !oldMsg.equals(msg))
                message.setValue(msg);
        }
    }

    public void updateFavorites(Picture picture) {
        List<Picture> updatedList;
        if(picture.isFavorite()){
            // the picture has been marked as favorite, update the list
            updatedList = favoritesManager.addToFavorites(getApplication(), picture);
        } else {
            updatedList = favoritesManager.removeFromFavorites(getApplication(), picture);
        }
        favorites.setValue(updatedList);
    }
}
