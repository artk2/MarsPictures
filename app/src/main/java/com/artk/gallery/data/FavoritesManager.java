package com.artk.gallery.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * a class that manages list of favorite pictures
 */
public class FavoritesManager {

    private final String FAV_FILE = "favorites";
    private final Gson gson = new Gson();

    private List<Picture> favorites = new ArrayList<>();

    public FavoritesManager(Context context) {
        // load the list once
        if (context.getFileStreamPath(FAV_FILE).length() > 0) {
            String fav_json = FileUtils.readJsonFromFile(context, FAV_FILE);
            Type listType = new TypeToken<List<Picture>>() {}.getType();
            favorites = gson.fromJson(fav_json, listType);
        }
    }

    public List<Picture> getFavorites() {
        return favorites;
    }

    public List<Picture> addToFavorites(Context context, Picture picture){
        if(!favorites.contains(picture)) {
            favorites.add(picture);
            Collections.sort(favorites, (b, a) -> a.getId() - b.getId());
            save(context);
        }
        return favorites;
    }

    public List<Picture> removeFromFavorites(Context context, Picture picture){
        Iterator<Picture> iterator = favorites.iterator();
        while (iterator.hasNext()){
            if(iterator.next().equals(picture)){
                iterator.remove();
                save(context);
                break;
            }
        }
        return favorites;
    }

    private void save(Context context){
        String json = gson.toJson(favorites);
        FileUtils.writeFile(context, FAV_FILE, json);
    }

}
