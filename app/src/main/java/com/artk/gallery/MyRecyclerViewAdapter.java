package com.artk.gallery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<Picture> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item, parent, false);

        return new ViewHolder(view);
    }

    private void postProcessPic(){
        MainActivity.picsToLoad--;
        if(MainActivity.picsToLoad == 0){
            if(!MainActivity.recyclerView.canScrollVertically(1)){
                MainActivity.loadData(); // загрузить новые картинки, если еще есть место на экране (в самом начале)
            }
            else MainActivity.loading = false;
            MainActivity.recyclerView.post(() -> MainActivity.adapter.notifyDataSetChanged());
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picture picture = mData.get(position);

        Glide.with(MainActivity.context)
                .load(picture.getUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.corrupt_file)
                )
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        postProcessPic();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        postProcessPic();
                        return false;
                    }
                })
                .into(holder.imageView);



//        Bitmap bmp = pic.getBmp();
//
//        if(bmp != null) {
//            int width = bmp.getWidth();
//            int height = bmp.getHeight();
//            if(height != width) {
//                int crop = Math.abs((width - height) / 2);
//                boolean vertical = width < height;
//                Bitmap cropBmp;
//                if (vertical) cropBmp = Bitmap.createBitmap(bmp, 0, crop, width, width);
//                else cropBmp = Bitmap.createBitmap(bmp, crop, 0, height, height);
//                holder.imageView.setImageBitmap(cropBmp);
//            } else holder.imageView.setImageBitmap(bmp);
//        }
//        new DownloadImageTask(holder.imageView).execute(pic.getUrl());

    }




    // total number of rows
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

    // convenience method for getting data at click position
    Picture getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}