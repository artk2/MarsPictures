package com.artk.gallery;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.artk.gallery.GalleryActivity.OPEN_PICTURE_CODE;
import static com.artk.gallery.GalleryActivity.gson;
import static com.artk.gallery.GalleryActivity.spanCount;

public class MainFragment extends Fragment
        implements MyRecyclerViewAdapter.ItemClickListener, MyRecyclerViewAdapter.PictureLoadedListener {

    private GalleryViewModel viewModel;
    private MyRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    public static final int SCROLL_DIRECTION_DOWN = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(GalleryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = view.findViewById(R.id.rvGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        adapter = new MyRecyclerViewAdapter(getContext(), new ArrayList<>());
        adapter.setOnClickListener(this);
        adapter.setOnPictureLoadedListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (!recyclerView.canScrollVertically(1) && !loading)
//                    loading = true;
//                    Log.i("hello", "calling load data from on scroll state changed");
//                    loadData();
//            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if(!recyclerView.canScrollVertically(SCROLL_DIRECTION_DOWN)){
                    viewModel.loadNext(); // load more image when scrolled to the end
                }
            }
        });

        viewModel.getPictures().observe(this, pictures -> adapter.setData(pictures));

        ProgressBar progressBar = view.findViewById(R.id.progressGallery);
        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            viewModel.isLoading().removeObservers(getActivity()); // show progress bar only once
        });

        viewModel.getMessage().observe(this, msg ->{
            if(viewModel.canShowMessage()){
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                viewModel.messageShown(); // show message only once
                viewModel.getMessage().removeObservers(getActivity());
            }
        });

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

    @Override
    public void onImageLoaded(boolean success) {
        viewModel.imageLoaded();
        if(viewModel.finishedLoading()){
            if(!recyclerView.canScrollVertically(SCROLL_DIRECTION_DOWN)){
                viewModel.loadNext(); // load more pictures if the screen is not full (at the beginning)
            }
        }
    }
}
