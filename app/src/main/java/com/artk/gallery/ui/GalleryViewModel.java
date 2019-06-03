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

        // Load favorites immediately.
        // Why: if user browses new pictures and clicks on one to open full view,
        // we need to know if it's already marked as favorite,
        // so the list might be necessary before user visits favorites tab
        favorites = new MutableLiveData<>();
        loadFavorites();
    }

    public LiveData<List<Picture>> getPictures(){
        if (pictures == null){
            pictures = new MutableLiveData<>();
            loadPictures();
        }
        return pictures;
    }

    public LiveData<List<Picture>> getFavorites(){
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

    private void loadFavorites() {
        favorites.setValue(favoritesManager.getFavorites());
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

    public Picture getPictureById(int id) {
        for (Picture picture : pictureList) {
            if (picture.getId() == id) return picture;
        }
        for (Picture picture : favoritesManager.getFavorites()) {
            if (picture.getId() == id) return picture;
        }
        return null;
    }

    public boolean isFavorite(int pictureId) {
        for (Picture picture : favoritesManager.getFavorites()) {
            if (picture.getId() == pictureId) return true;
        }
        return false;
    }

    public void addToFavorites(Picture picture) {
        favoritesManager.addToFavorites(getApplication(), picture);
        favorites.setValue(favoritesManager.getFavorites());
    }

    public void removeFromFavorites(Picture picture) {
        favoritesManager.removeFromFavorites(getApplication(), picture);
        favorites.setValue(favoritesManager.getFavorites());
    }

}
