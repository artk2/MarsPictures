package com.artk.gallery.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.artk.gallery.R;
import com.artk.gallery.app.Log;
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

    private boolean loading = false;

    public GalleryViewModel(@NonNull Application application){
        super(application);
        pictureList = new ArrayList<>();

        favoritesManager = new FavoritesManager(getApplication());
        pictureManager = new PictureManager();
        pictureManager.setPictureReceiver(this);

        // Load favorites immediately.
        // Why: if user browses new pictures and clicks on one to open full view,
        // we need to know if it's already marked as favorite,
        // so the list might be necessary before user visits favorites tab
        favorites = new MutableLiveData<>();
        loadFavorites();
    }

    public LiveData<List<Picture>> getPictures(){
        Log.d("requested pictures");
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
        }
    }

    private void loadPictures(){
        loading = true;
        pictureManager.loadNext();
    }

    private void loadFavorites() {
        favorites.setValue(favoritesManager.getFavorites());
    }

    @Override
    public void onNext(List<Picture> pictures) {
        Log.i("received " + pictures.size() + " pictures");
        pictureList.addAll(pictures);
        this.pictures.setValue(pictureList);
        loading = false;
    }

    @Override
    public void onError(Throwable t) {
        Log.e(t);

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

    @Override
    protected void onCleared() {
        super.onCleared();
        pictureManager.removeReceiver();
    }
}
