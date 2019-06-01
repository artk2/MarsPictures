package com.artk.gallery.api;

import android.os.CountDownTimer;
import android.util.Log;

import com.artk.gallery.data.Picture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Imitates calling server for pictures
 */
public class TestDataProvider implements DataProvider {

    private DataProviderCallback callback;

    TestDataProvider(DataProviderCallback callback) {
        this.callback = callback;
    }

    @Override
    public void loadNext() {
        returnWithDelay(2000);
    }

    private void returnWithDelay(int ms) {
        Log.v("artk2", "TestDataProvider: returnWithDelay " + ms);
        new CountDownTimer(ms, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                callback.onDataLoaded(generateList(1));
            }
        }.start();
        Log.v("artk2", "started timer");
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
