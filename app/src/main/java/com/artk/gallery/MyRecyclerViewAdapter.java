package com.artk.gallery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<Picture> mData;
    private LayoutInflater mInflater;
    private Context context;

    MyRecyclerViewAdapter(Context context, List<Picture> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picture picture = mData.get(position);

        Glide.with(context)
                .load(picture.getUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.corrupt_file)
                )
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if(mLoadedListener != null)
                            mLoadedListener.onImageLoaded(false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if(mLoadedListener != null)
                            mLoadedListener.onImageLoaded(true);
                        return false;
                    }
                })
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    Picture getItem(int id) {
        return mData.get(id);
    }

    public void setData(List<Picture> data){
        this.mData = data;
        notifyDataSetChanged();
    }

    ////////////
    // CALLBACKS

    private ItemClickListener mClickListener;
    private PictureLoadedListener mLoadedListener;

    // allows clicks events to be caught
    void setOnClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    void setOnPictureLoadedListener(PictureLoadedListener listener){
        this.mLoadedListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface PictureLoadedListener {
        void onImageLoaded(boolean success);
    }

}