package com.akurmatov.apps.gallery.api.service;

import com.artk.gallery.api.client.RetrofitClient;
import com.artk.gallery.api.service.DataProvider;
import com.artk.gallery.api.service.NasaProvider;
import com.artk.gallery.app.Log;
import com.artk.gallery.data.Picture;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class NasaProviderTest {

    private static DataProvider dataProvider;

    @BeforeClass
    public static void prepare() {
        dataProvider = new NasaProvider() {

            @Override
            protected void makeApiCall(String date, String rover) {
                CompletableFuture
                        .supplyAsync(() -> getTestListWithDelay(date, rover))
                        .thenAccept(this::onDataReceived)
                        .exceptionally(throwable -> {
                                    Log.e(throwable.getLocalizedMessage());
                                    return null;
                                }
                        );
            }

            private List<Picture> getTestListWithDelay(String date, String rover) {
                Log.d("requested picture: " + date + ", rover");
                try {
                    Thread.sleep(500);
                    List<Picture> list = new ArrayList<>();
                    Picture pic1 = new Picture(0, "url", date, rover, "camera");
                    list.add(pic1);
                    return list;
                } catch (InterruptedException e) {
                    return new ArrayList<>();
                }
            }
        };
    }

    @Test
    public void loadMultipleTest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        final int requests = 3;
        DataProvider.Callback callback = new DataProvider.Callback() {
            int iterations = 0;
            int totalPicsReceived;

            @Override
            public void onDataLoaded(String date, List<Picture> pictures) {
                totalPicsReceived += pictures.size();
                for (Picture pic : pictures) System.out.println(pic);
                if (++iterations == requests) future.complete(totalPicsReceived);
            }

            @Override
            public void onFailedToLoad(String date, Throwable throwable) {
                Log.e(throwable.getLocalizedMessage());
            }
        };

        for (int i = 0; i < requests; i++) {
            dataProvider.loadNext(callback);
        }

        int expected = requests * RetrofitClient.ROVERS.length;
        assertEquals((Integer) expected, future.get());

    }

}