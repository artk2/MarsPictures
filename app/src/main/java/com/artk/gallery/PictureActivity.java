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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;

public class PictureActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    ProgressBar progressBar;
    int screen_height;

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




        Intent intent = getIntent();
        String json = intent.getStringExtra("Picture");

        Picture picture = new Gson().fromJson(json, Picture.class);
//        if(picture.getBmp() != null){
//            Bitmap bitmap = picture.getBmp();
//            int imageWidth = bitmap.getWidth();
//            int imageHeight = bitmap.getHeight();
//            int newWidth = getResources().getDisplayMetrics().widthPixels;
//            float scaleFactor = (float) newWidth / (float) imageWidth;
//            int newHeight = (int) (imageHeight * scaleFactor);
//
//            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
//            imageView.setImageBitmap(bitmap);
//        }

        textView.setText(picture.toString());

        textView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            int imgBottom = imageView.getBottom();
            int tvHeight = textView.getLineCount() * textView.getLineHeight();

            if(imageView.getHeight() > 0) {
                if (imgBottom + tvHeight < screen_height) {
                    textView.setY(imgBottom);
                }
            }

        });

        Glide.with(this)
                .load(picture.getUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .fitCenter())
                .listener(new RequestListener<Drawable>() {
                    @Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                    @Override public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imageView);

    }

}
