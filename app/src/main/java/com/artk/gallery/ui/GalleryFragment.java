package com.artk.gallery.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.artk.gallery.R;
import com.artk.gallery.data.Picture;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.artk.gallery.ui.GalleryActivity.OPEN_PICTURE_CODE;
import static com.artk.gallery.ui.GalleryActivity.spanCount;

/**
 * A general gallery fragment with click event handler.
 * Subclasses must provide their own source of pictures.
 */
public abstract class GalleryFragment extends Fragment implements GalleryAdapter.ItemClickListener {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private GalleryAdapter adapter;
    private GalleryViewModel viewModel;

    private static final Gson gson = new Gson();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(GalleryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.rvGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        adapter = new GalleryAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnClickListener(this);

        progressBar = view.findViewById(R.id.progressGallery);

        return view;
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

    public GalleryViewModel getViewModel() {
        return viewModel;
    }

    public GalleryAdapter getAdapter() {
        return adapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
