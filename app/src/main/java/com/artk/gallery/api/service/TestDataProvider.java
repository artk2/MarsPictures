package com.artk.gallery.api.service;

import android.os.CountDownTimer;

import com.artk.gallery.app.Log;
import com.artk.gallery.data.Picture;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Imitates calling server for pictures
 */
public class TestDataProvider implements DataProvider {

    private DataProvider.Callback callback;

    @Override
    public void loadNext(DataProvider.Callback callback) {
        this.callback = callback;
        errorWithDelay(8000);
    }

    @Override
    public void clear() {
        callback = null;
    }

    private void returnWithDelay(int ms) {
        Log.v("returnWithDelay " + ms);
        new CountDownTimer(ms, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                callback.onDataLoaded("2020-02-02", generateList(1));
                callback = null;
            }
        }.start();
        Log.v("started timer");
    }

    private void errorWithDelay(int ms) {
        new CountDownTimer(ms, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                callback.onFailedToLoad("2020-02-02", new UnknownHostException("Test"));
                callback = null;
            }
        }.start();
    }

    private List<Picture> generateList(int size) {
        List<Picture> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(generatePicture());
        }
        return list;
    }

    private Picture generatePicture() {
        Random r = new Random();
        int id = r.nextInt(1000000);
        String url = urls[r.nextInt(urls.length)];
        String date = "2019-06-01";
        String rover = "Curiosity";
        String camera = "camera";
        return new Picture(id, url, date, rover, camera);
    }

    private static final String[] urls = {
            "http://mars.jpl.nasa.gov/msl-raw-images/msss/02115/mhli/2115MH0007570000802473E01_DXXX.jpg",
            "http://mars.jpl.nasa.gov/msl-raw-images/msss/02114/mhli/2114MH0001930000802464S00_DXXX.jpg",
            "http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/02108/opgs/edr/ncam/NLB_584639903EDR_F0712876NCAM07753M_.JPG",
            "corrupt-image",
            "http://mars.jpl.nasa.gov/msl-raw-images/msss/02090/mcam/2090MR0111420010904140C00_DXXX.jpg"
    };

}
