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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    int spanCount;
    int screenWidth;
    static Context context;
    static Date reqDate = new Date();

//    private LruCache<String, Bitmap> mMemoryCache;

//    private final Gson gson = new Gson();

    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/";
    private static final String[] ROVERS = {"curiosity", "opportunity" /*, "spirit" - нет фото с 2010*/};
    private static final String KEY = "Qh5l5EUypdjnMp9Wd2Wq856F9qezwozXolND0Fw5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        context = this;

//        // init memory cache
//        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
//        final int cacheSize = maxMemory / 8; // Use 1/8th of the available memory for this memory cache.
//        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
//            @Override
//            protected int sizeOf(String key, Bitmap bitmap) {
//                return bitmap.getByteCount() / 1024;
//            }
//        };


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
                    loadData();
                }
            }
        });

        Log.i("hello", "calling load data from oncreate");
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

        Log.i("hello", d);



            Thread thread = new Thread(() -> {
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
                                //                    Bitmap bmp = getBitmapFromMemCache(url);
                                //                    if (bmp != null) picture.setBmp(bmp);
                                //                    new DownloadImageTask(picture).execute(url);
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


//        String j0 = "{\"photos\":[]}";
//        String j1 = "{\"photos\":[{\"id\":103383,\"camera\":{\"full_name\":\"NavigationCamera\"},\"img_src\":\"https:\\/\\/static.bhphotovideo.com\\/explora\\/sites\\/default\\/files\\/styles\\/top_shot\\/public\\/TS-Night-Photography.jpg\",\"earth_date\":\"2015-05-30\",\"rover\":{\"name\":\"Curiosity\"}},{\"id\":103384,\"camera\":{\"full_name\":\"NavigationCamera\"},\"img_src\":\"http:\\/\\/www.zarias.com\\/wp-content\\/uploads\\/2015\\/08\\/Portrait-Photography-Tips-and-Ideas78.jpg\",\"earth_date\":\"2015-05-30\",\"rover\":{\"name\":\"Curiosity\"}},{\"id\":103385,\"camera\":{\"full_name\":\"NavigationCamera\"},\"img_src\":\"http:\\/\\/ianthearchitect.org\\/wp-content\\/uploads\\/2014\\/07\\/Fred-Murray.jpg\",\"earth_date\":\"2015-05-30\",\"rover\":{\"name\":\"Curiosity\"}}]}";


    }

    @Override
    public void onItemClick(View view, int position) {
        if(adapter.getItem(position) != null) {
//            if(adapter.getItem(position).getBmp() != null) {
                Intent intent = new Intent(MainActivity.this, PictureActivity.class);
                intent.putExtra("Picture", new Gson().toJson(adapter.getItem(position)));
                startActivity(intent);
//            }
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

//    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
//        if (getBitmapFromMemCache(key) == null) {
//            mMemoryCache.put(key, bitmap);
//        }
//    }
//
//    public Bitmap getBitmapFromMemCache(String key) {
//        return mMemoryCache.get(key);
//    }


//    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        Picture pic;
//
//        DownloadImageTask(Picture pic) {
//            this.pic = pic;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String url = urls[0];
//            Bitmap bmp = null;
////            Bitmap bmp = getBitmapFromMemCache(url);
////            if (bmp == null) { // if not found in cache
//                try {
//                    InputStream in = new java.net.URL(url).openStream();
//                    bmp = BitmapFactory.decodeStream(in);
//                } catch (Exception e) {
//                    Log.e("Error", e.getMessage());
//                    e.printStackTrace();
//                }
//                if (bmp != null) { // add small size bitmap to memory cache
//                    int width = bmp.getWidth();
//                    int height = bmp.getHeight();
//                    float ratio = ((float)width)/height;
//                    float newSize = screenWidth / spanCount / 4;
//                    boolean vertical = width < height;
//                    if((vertical && width < newSize) || (!vertical && height < newSize)){
//                        addBitmapToMemoryCache(url, bmp);
//                    } else {
//                        Bitmap smallBmp;
//                        if (vertical) smallBmp = Bitmap.createScaledBitmap(bmp, (int) newSize, (int) (newSize / ratio), false);
//                        else smallBmp = Bitmap.createScaledBitmap(bmp, (int) (ratio * newSize), (int) newSize, false);
//                        addBitmapToMemoryCache(String.valueOf(url), smallBmp);
//                    }
//                } else {
//                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.corrupt_file);
//                }
////            }
//            return bmp;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            if(result != null) {
//                pic.setBmp(result);
//            }
//            adapter.notifyDataSetChanged();
//            picsToLoad--;
//            if(picsToLoad == 0){
//                if(!recyclerView.canScrollVertically(1)) loadData();
//                else loading = false;
//            }
//        }
//    }

}
