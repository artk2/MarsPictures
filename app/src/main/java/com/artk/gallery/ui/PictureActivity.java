package com.artk.gallery.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.artk.gallery.R;
import com.artk.gallery.data.Picture;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;

public class PictureActivity extends AppCompatActivity {

    private PhotoView imageView;
    private TextView textView;
    private ProgressBar progressBar;
    private ToggleButton toggleButton;
    private ImageButton imageButton;

    int screen_height;
    private static final Gson gson = new Gson();

    private Picture picture;

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
        picture = gson.fromJson(json, Picture.class);

        textView.setText(getPictureInfo(picture));
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

        // place text under the image; if not enough space, then at the bottom of the screen
        textView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int imgBottom = imageView.getBottom();
            int tvHeight = textView.getLineCount() * textView.getLineHeight();
            if(imageView.getHeight() > 0) {
                if (imgBottom + tvHeight < screen_height)
                    textView.setY(imgBottom);
                }
        });
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> picture.setFavorite(isChecked));
        imageButton.setOnClickListener(v -> sharePicture());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("picture", gson.toJson(picture));
        setResult(RESULT_OK, intent);
        finish();
    }

    private void sharePicture(){
        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);

        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        new ShareTask(getApplication(), shareIntent -> {
            progressBar.setVisibility(View.GONE);
            if(shareIntent == null) return;
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        }).execute(bitmap);
    }

    private String getPictureInfo(Picture picture){
        return getString(R.string.txtDateTaken) + ": " + picture.getEarthDate() + "\n" +
                getString(R.string.txtRover) + ": " + picture.getRover() + "\n" +
                getString(R.string.txtCamera) + ": " + picture.getCamera();
    }

}
