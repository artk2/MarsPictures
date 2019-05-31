package com.artk.gallery.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public class FavoritesFragment extends GalleryFragment implements GalleryAdapter.ItemClickListener {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getViewModel().getFavorites().observe(this, favorites -> {
            getAdapter().setData(favorites);
        });
    }

}
