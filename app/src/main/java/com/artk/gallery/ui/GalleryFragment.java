package com.artk.gallery.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artk.gallery.R;
import com.artk.gallery.data.Picture;
import com.google.gson.Gson;

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
        adapter = new GalleryAdapter(getContext(), spanCount);
        recyclerView.setAdapter(adapter);

        adapter.setOnClickListener(this);

        progressBar = view.findViewById(R.id.progressGallery);

        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position < 0) return;
        Picture picture = adapter.getItem(position);
        if (picture == null) return;

        Bundle b = new Bundle();
        b.putInt("pictureId", picture.getId());

        PictureDialogFragment dialog = new PictureDialogFragment();
        dialog.setArguments(b);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        dialog.show(ft, null);
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
