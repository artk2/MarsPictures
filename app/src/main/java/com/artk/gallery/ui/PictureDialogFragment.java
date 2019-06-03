package com.artk.gallery.ui;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class PictureDialogFragment extends DialogFragment {

    private PhotoView imageView;
    private TextView textView;
    private ProgressBar progressBar;
    private ToggleButton toggleButton;
    private ImageButton imageButton;

    private GalleryViewModel viewModel;
    private Picture picture;
    private boolean wasFavorite; // whether or not this picture was favorite when opening dialog

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        viewModel = ViewModelProviders.of(getActivity()).get(GalleryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture_dialog, container, false);

        imageView = view.findViewById(R.id.imgFullPic);
        textView = view.findViewById(R.id.textViewDesc);
        progressBar = view.findViewById(R.id.progress);
        toggleButton = view.findViewById(R.id.btnFavorite);
        imageButton = view.findViewById(R.id.btnShare);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int id = getArguments().getInt("pictureId", -1);
        picture = viewModel.getPictureById(id);
        if (picture == null) {
            dismiss();
            return;
        }

        textView.setText(getPictureInfo(picture));
        wasFavorite = viewModel.isFavorite(picture.getId());
        toggleButton.setChecked(wasFavorite);

        Glide.with(this)
                .load(picture.getUrl())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .error(R.drawable.corrupt_file))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        toggleButton.setVisibility(View.VISIBLE);
                        imageButton.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imageView);

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> picture.setFavorite(isChecked));
        imageButton.setOnClickListener(v -> sharePicture());

        viewModel.getPictures().observe(this, pictures -> Log.v("artk2", "observing " + pictures.size()));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (picture == null) return;
        boolean favorite = toggleButton.isChecked();
        if (favorite && !wasFavorite) viewModel.addToFavorites(picture);
        else if (!favorite && wasFavorite) viewModel.removeFromFavorites(picture);
    }

    private void sharePicture() {
        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);

        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        new ShareTask(getActivity().getApplication(), shareIntent -> {
            progressBar.setVisibility(View.GONE);
            if (shareIntent == null) return;
            startActivity(Intent.createChooser(shareIntent, getContext().getString(R.string.msg_share_choose_app)));
        }).execute(bitmap);
    }

    private String getPictureInfo(Picture picture) {
        return getString(R.string.txtDateTaken) + ": " + picture.getEarthDate() + "\n" +
                getString(R.string.txtRover) + ": " + picture.getRover() + "\n" +
                getString(R.string.txtCamera) + ": " + picture.getCamera();
    }
}
