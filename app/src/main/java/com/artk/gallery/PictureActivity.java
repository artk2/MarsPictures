package com.artk.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

import static com.artk.gallery.GalleryActivity.FAV_FILE;
import static com.artk.gallery.GalleryActivity.favorites;

public class PictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private ProgressBar progressBar;
    private ToggleButton toggleButton;
    private ImageButton imageButton;
    int screen_height;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        screen_height = getResources().getDisplayMetrics().heightPixels;

        imageView = findViewById(R.id.imgFullPic);
        textView = findViewById(R.id.textViewDesc);
        progressBar = findViewById(R.id.progress);
        toggleButton = findViewById(R.id.btnFavorite);
        imageButton = findViewById(R.id.btnShare);

        String json = getIntent().getStringExtra("Picture");
        final Picture picture = gson.fromJson(json, Picture.class);

        StringBuilder sb = new StringBuilder();
//        sb.append("id: ").append(this.getId()).append("\n");
        sb.append(getString(R.string.txtDateTaken)).append(": ").append(picture.getEarthDate()).append("\n");
        sb.append(getString(R.string.txtRover)).append(": ").append(picture.getRover()).append("\n");
        sb.append(getString(R.string.txtCamera)).append(": ").append(picture.getCamera());
        textView.setText(sb.toString());
        toggleButton.setChecked(picture.isFavorite());

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
                        imageButton.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imageView);

        // располагаем текст под фото; если не помещается, то внизу экрана
        textView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int imgBottom = imageView.getBottom();
            int tvHeight = textView.getLineCount() * textView.getLineHeight();
            if(imageView.getHeight() > 0) {
                if (imgBottom + tvHeight < screen_height)
                    textView.setY(imgBottom);
                }
        });

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // обновляем фото в загруженных (если есть)
            int id = picture.getId();
            for(Picture pic : GalleryActivity.data){
                if(pic.getId() == id){
                    pic.setFavorite(isChecked);
                    break;
                }
            }
            // обновляем список избранных отдельно
            if(isChecked){
                favorites.add(Picture.copyOf(picture));
                Collections.sort(favorites, (o1, o2) -> Integer.compare(o2.getId(), o1.getId())); // по убыванию id
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
                }
            }
//            picture.setFavorite(isChecked);
            FavoritesFragment.adapter.notifyDataSetChanged();
            writeFile(getApplicationContext(), FAV_FILE, gson.toJson(favorites));
        });

        imageButton.setOnClickListener(v -> {
            progressBar.bringToFront();
            progressBar.setVisibility(View.VISIBLE);
            Thread thread = new Thread(() -> {
                // save bitmap to cache directory
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                if (bitmap != null) {
                    try {
                        File cachePath = new File(getApplication().getCacheDir(), "images");
                        cachePath.mkdirs(); // don't forget to make the directory
                        FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // sharing the image
                    File imagePath = new File(getApplication().getCacheDir(), "images");
                    File newFile = new File(imagePath, "image.png");
                    Uri contentUri = FileProvider.getUriForFile(getApplicationContext(),
                            "com.artk.gallery.fileprovider", newFile);

                    if (contentUri != null) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                        shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        startActivity(Intent.createChooser(shareIntent, "Choose an app"));
                        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    }

                }

            });
            thread.start();
        });

    }

    static void writeFile(Context context, String fileName, String content){
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
