package com.artk.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.artk.gallery.GalleryActivity.data;
import static com.artk.gallery.GalleryActivity.favorites;
import static com.artk.gallery.GalleryActivity.gson;
import static com.artk.gallery.GalleryActivity.spanCount;

public class MainFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {

    static MyRecyclerViewAdapter adapter;
    static RecyclerView recyclerView;
    static Calendar cal = Calendar.getInstance();
    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/";
    private static final String[] ROVERS = {"curiosity", "opportunity" /*, "spirit" - нет фото с 2010*/};
    private static final String KEY = "Qh5l5EUypdjnMp9Wd2Wq856F9qezwozXolND0Fw5";
    // контроль загрузки новых картинок
    static boolean loading = false;
    static int picsToLoad = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = view.findViewById(R.id.rvGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        adapter = new MyRecyclerViewAdapter(getActivity(), data, true);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (!recyclerView.canScrollVertically(1) && !loading)
//                    loading = true;
//                    Log.i("hello", "calling load data from on scroll state changed");
//                    loadData();
//            }
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
//                super.onScrolled(recyclerView, dx, dy);
                boolean canScroll = recyclerView.canScrollVertically(1);
                if (!canScroll && !loading) {
                    loading = true;
                    loadData(); // загрузить новые картинки если долистали до конца
                }
            }
        });
        cal.setTime(new Date());
        loadData();
        return view;
    }

    static void loadData(){
//        loading = true;
        cal.add(Calendar.DATE, -1); // меняем дату запроса
        Date reqDate = cal.getTime();
        Format formatter = new SimpleDateFormat("yyyy-M-d");
        String d = formatter.format(reqDate);

        Thread thread = new Thread(() -> {  // network on main thread exception
            try {
                JsonParser parser = new JsonParser();
                for (String ROVER : ROVERS) {
                    String request = BASE_URL + ROVER + "/photos?earth_date=" + d + "&api_key=" + KEY;
                    String json = readJsonFromUrl(request);
                    JsonObject rootObj = parser.parse(json).getAsJsonObject();
                    JsonArray arr = rootObj.get("photos").getAsJsonArray();
                    if (arr != null) {
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject o = arr.get(i).getAsJsonObject();
                            int id = o.get("id").getAsInt();
                            String url = o.get("img_src").getAsString();
                            String date = o.get("earth_date").getAsString();
                            String rover = o.get("rover").getAsJsonObject().get("name").getAsString();
                            String camera = o.get("camera").getAsJsonObject().get("full_name").getAsString();
                            Picture picture = new Picture(id, url, date, rover, camera);
                            data.add(picture);
                            picsToLoad++;
                        }
                    }
                    loading = false;
                    recyclerView.post(() -> adapter.notifyDataSetChanged());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }

    @Override
    public void onItemClick(View view, int position) {
        if(position >= 0) {
            Picture picture = adapter.getItem(position);
            if (picture != null) {
                Intent intent = new Intent(getActivity(), PictureActivity.class);
                for (Picture favorite : favorites) {
                    if (picture.getId() == favorite.getId())
                        picture.setFavorite(true);
                }
                intent.putExtra("Picture", gson.toJson(picture));
                startActivity(intent);
            }
        }
    }

    public static String readJsonFromUrl(String url) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            return sb.toString();
        }
    }


}
