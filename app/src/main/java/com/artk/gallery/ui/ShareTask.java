package com.artk.gallery.ui;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * creates image share intent in background thread
 */
public class ShareTask extends AsyncTask<Bitmap, Boolean, Intent> {

    private Application app;
    private Callback callback;

    public ShareTask(Application app, Callback callback) {
        this.app = app;
        this.callback = callback;
    }

    @Override
    protected Intent doInBackground(Bitmap... bitmaps) {
        if(bitmaps == null || bitmaps.length != 1 || bitmaps[0] == null) return null;
        if(! putToCache(bitmaps[0])) return null;
        return createShareIntent();
    }

    @Override
    protected void onPostExecute(@Nullable Intent shareIntent) {
        callback.onComplete(shareIntent);
    }

    private static final String cacheDir = "images";
    private static final String fileName = "image.png";

    private boolean putToCache(Bitmap bitmap){
        try {
            File cachePath = new File(app.getCacheDir(), cacheDir);
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/" + fileName); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Intent createShareIntent(){
        File imagePath = new File(app.getCacheDir(), cacheDir);
        File newFile = new File(imagePath, fileName);
        Uri contentUri = FileProvider.getUriForFile(app.getBaseContext(),
                "com.artk.gallery.fileprovider", newFile);
        if (contentUri == null) return null;

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
        shareIntent.setDataAndType(contentUri, app.getContentResolver().getType(contentUri));
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        return shareIntent;
    }

    public interface Callback {
        void onComplete(@Nullable Intent shareIntent);
    }
}
