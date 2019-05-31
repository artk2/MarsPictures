package com.artk.gallery.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainFragment extends GalleryFragment
        implements GalleryAdapter.ItemClickListener, GalleryAdapter.BottomOfListListener {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getAdapter().setBottomOfListListener(this);

        getViewModel().getPictures().observe(this, pictures -> {
            if(pictures == null) return;
            Log.v("artk2", "MainFragment: total pictures: " + pictures.size());
            getProgressBar().setVisibility(View.GONE);
            getAdapter().setData(pictures);
        });

        getViewModel().getMessage().observe(this, msg ->{
            Log.v("artk2","MainFragment message: " + msg);
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBottomReached() {
        getViewModel().loadNextPictures();
    }
}
