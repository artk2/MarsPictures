package com.artk.gallery;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    static MyRecyclerViewAdapter adapter;
    static RecyclerView recyclerView;
    static List<Picture> data = new ArrayList<>();
    static boolean loading = false;
    static int picsToLoad = 0;
    int spanCount, screenWidth;
    static Context context;
    static Date reqDate = new Date();

    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/";
    private static final String[] ROVERS = {"curiosity", "opportunity" /*, "spirit" - нет фото с 2010*/};
    private static final String KEY = "Qh5l5EUypdjnMp9Wd2Wq856F9qezwozXolND0Fw5";

    static final String tag = "artk2";
    static final String FAV_FILE = "favorites";
    static List<Picture> favorites = new ArrayList<>();
    private static final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        context = this;

        recyclerView = findViewById(R.id.rvGallery);

        spanCount = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? 3 : 5;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        adapter = new MyRecyclerViewAdapter(this, data);
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

        if (getFileStreamPath(FAV_FILE).length() > 0) {
            String fav_json = readJsonFromFile(context, FAV_FILE);
            Type listType = new TypeToken<List<Picture>>() {}.getType();
            favorites = gson.fromJson(fav_json, listType);
        }

        loadData();
    }

    static void loadData(){
//        loading = true;

        Calendar cal = Calendar.getInstance();
        cal.setTime(reqDate);
        cal.add(Calendar.DATE, -1);
        reqDate = cal.getTime();

        Format formatter = new SimpleDateFormat("yyyy-M-d");
        String d = formatter.format(reqDate);
        Log.i(tag, d);

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
        Picture picture = adapter.getItem(position);
        if(picture != null) {
            Intent intent = new Intent(MainActivity.this, PictureActivity.class);
            for(Picture favorite : favorites){
                if (picture.getId() == favorite.getId())
                    picture.setFavorite(true);
            }
            intent.putExtra("Picture", gson.toJson(picture));
            startActivity(intent);
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


    static String readJsonFromFile(Context context, String fileName){
        String json = "";
        try{
            FileInputStream in = context.openFileInput(fileName);
            InputStreamReader is = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            json = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    static void writeFile(Context context, String fileName, String content){
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
            Log.i(tag, "file written: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
