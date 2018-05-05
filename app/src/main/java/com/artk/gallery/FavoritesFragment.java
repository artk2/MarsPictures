package com.artk.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import static com.artk.gallery.GalleryActivity.FAV_FILE;
import static com.artk.gallery.GalleryActivity.favorites;
import static com.artk.gallery.GalleryActivity.gson;
import static com.artk.gallery.GalleryActivity.spanCount;

public class FavoritesFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {

    static RecyclerView recyclerView;
    static MyRecyclerViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        if (getContext().getFileStreamPath(FAV_FILE).length() > 0) {
            String fav_json = readJsonFromFile(getContext(), FAV_FILE);
            Type listType = new TypeToken<List<Picture>>() {}.getType();
            favorites = gson.fromJson(fav_json, listType);
        }

        recyclerView = view.findViewById(R.id.rvGallery);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        adapter = new MyRecyclerViewAdapter(getActivity(), favorites, false);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    static String readJsonFromFile(Context context, String fileName){
        String json = "";
        try{
            FileInputStream in = context.openFileInput(fileName);
            InputStreamReader is = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            json = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void onItemClick(View view, int position) {
        if(position >= 0) {
            Picture picture = adapter.getItem(position);
            if (picture != null) {
                Intent intent = new Intent(getActivity(), PictureActivity.class);
                for (Picture favorite : favorites) {
                    if (picture.getId() == favorite.getId())
                        picture.setFavorite(true);
                }
                intent.putExtra("Picture", gson.toJson(picture));
                startActivity(intent);
            }
        }
    }

}
