package com.artk.gallery;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;

import java.util.List;

import static com.artk.gallery.MainActivity.FAV_FILE;
import static com.artk.gallery.MainActivity.favorites;
import static com.artk.gallery.MainActivity.tag;

public class PictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private ProgressBar progressBar;
    private ToggleButton toggleButton;
    int screen_height;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        screen_height = getResources().getDisplayMetrics().heightPixels;

        imageView = findViewById(R.id.imgFullPic);
        textView = findViewById(R.id.textViewDesc);
        progressBar = findViewById(R.id.progress);
        toggleButton = findViewById(R.id.favorite);

        Intent intent = getIntent();
        String json = intent.getStringExtra("Picture");
        final Picture picture = gson.fromJson(json, Picture.class);

        textView.setText(picture.toString());
        toggleButton.setChecked(picture.isFavorite());

        textView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            int imgBottom = imageView.getBottom();
            int tvHeight = textView.getLineCount() * textView.getLineHeight();

            if(imageView.getHeight() > 0) {
                if (imgBottom + tvHeight < screen_height) {
                    textView.setY(imgBottom);
                }
            }

        });

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int id = picture.getId();
            for(Picture pic : MainActivity.data){
                if(pic.getId() == id){
                    pic.setFavorite(isChecked);
                    break;
                }
            }
            if(isChecked){
                favorites.add(Picture.copyOf(picture));
                Log.i(tag, "added picture to favorites");
            } else {
                int index = -1;
                for(Picture pic : favorites){
                    if (pic.getId() == id) {
                        index = favorites.indexOf(pic);
                        break;
                    }
                }
                if(index != -1) {
                    favorites.remove(index);
                    Log.i(tag, "removed picture from favorites");
                }
            }
//            picture.setFavorite(isChecked);
            MainActivity.writeFile(getApplicationContext(), FAV_FILE, gson.toJson(favorites));
            Log.i(tag, "pictures in favorites: " + favorites.size());
        });

        Glide.with(this)
                .load(picture.getUrl())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .fitCenter()
                        .error(R.drawable.corrupt_file))
                .listener(new RequestListener<Drawable>() {
                    @Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                    @Override public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        toggleButton.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imageView);

    }

}
