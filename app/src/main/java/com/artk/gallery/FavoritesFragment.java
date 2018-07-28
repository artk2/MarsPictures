package com.artk.gallery;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.artk.gallery.GalleryActivity.OPEN_PICTURE_CODE;
import static com.artk.gallery.GalleryActivity.gson;
import static com.artk.gallery.GalleryActivity.spanCount;

public class FavoritesFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {

    GalleryViewModel viewModel;
    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(GalleryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = view.findViewById(R.id.rvGallery);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        adapter = new MyRecyclerViewAdapter(getContext(), new ArrayList<>());
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);

        viewModel.getFavorites().observe(this, favorites -> {
            adapter.setData(favorites);
        });

        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        if(position >= 0) {
            Picture picture = adapter.getItem(position);
            if (picture != null) {
                Intent intent = new Intent(getActivity(), PictureActivity.class);
                List<Picture> favorites = viewModel.getFavorites().getValue();
                if (favorites != null) {
                    for (Picture favorite : viewModel.getFavorites().getValue()) {
                        if (picture.getId() == favorite.getId())
                            picture.setFavorite(true);
                    }
                }
                intent.putExtra("Picture", gson.toJson(picture));
                startActivityForResult(intent, OPEN_PICTURE_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == OPEN_PICTURE_CODE){
            if(resultCode == RESULT_OK) {
                String json = data.getStringExtra("picture");
                Picture picture = gson.fromJson(json, Picture.class);
                viewModel.updateFavorites(picture);
            }
        }
    }



}
