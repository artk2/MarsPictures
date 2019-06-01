package com.artk.gallery.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainFragment extends GalleryFragment
        implements GalleryAdapter.ItemClickListener, GalleryAdapter.BottomOfListListener, GalleryAdapter.PictureLoadedListener {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getAdapter().setBottomOfListListener(this);
        getAdapter().setOnPictureLoadedListener(this);
        getProgressBar().setVisibility(View.VISIBLE);
        getViewModel().getPictures().observe(this, pictures -> {
            if(pictures == null) return;
            Log.v("artk2", "MainFragment: total pictures: " + pictures.size());
//            getProgressBar().setVisibility(View.GONE);
            getAdapter().setData(pictures);
        });

        getViewModel().getMessage().observe(this, msg ->{
            Log.v("artk2","MainFragment message: " + msg);
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBottomReached() {
        Log.v("artk2", "MainFragment: bottom reached");
        getViewModel().loadNextPictures();
    }

    @Override
    public void onImageLoaded(boolean success) {
        // We hide the progress bar after the first image has been loaded.
        // It shouldn't be done when picture list is received, because some time is needed
        // to load and display the actual bitmap from the server.
        // Until then the screen remains empty so the progress bar should be visible.
        getProgressBar().setVisibility(View.GONE);
        getAdapter().setOnPictureLoadedListener(null);
    }
}
