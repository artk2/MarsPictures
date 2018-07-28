package com.artk.gallery;

import android.content.Context;
import android.os.AsyncTask;

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
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.artk.gallery.GalleryActivity.gson;

public class PictureManager {

    private final List<Picture> data = new ArrayList<>();
//    private List<Picture> favorites = new ArrayList<>();

    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/";
    private static final String[] ROVERS = {"curiosity", "opportunity" /*, "spirit" - нет фото с 2010*/};

    private final Calendar cal = Calendar.getInstance();
    private final String FAV_FILE = "favorites";
    private static final String KEY = "DEMO_KEY";

    public PictureManager(){
        cal.setTime(new Date());
    }

    public void loadData(PictureLoaderCallback callback){

        cal.add(Calendar.DATE, -1); // меняем дату запроса
        Date reqDate = cal.getTime();
        Format formatter = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String d = formatter.format(reqDate);

        String urls[] = new String[ROVERS.length];
        for(int i = 0; i < ROVERS.length; i++){
            urls[i] = BASE_URL + ROVERS[i] + "/photos?earth_date=" + d + "&api_key=" + KEY;
        }
        new PictureLoader(callback).execute(urls);

    }

    public interface PictureLoaderCallback{

        void onDataLoaded(List<Picture> updatedList, int amountAdded);

        void onFailedToLoad(Exception exception);

    }

    private class PictureLoader extends AsyncTask<String, Integer, List<Picture>>{

        private final PictureLoaderCallback callback;
        Exception exception = null;

        PictureLoader(PictureLoaderCallback callback){
            this.callback = callback;
        }

        @Override
        protected List<Picture> doInBackground(String... urls) {

            List<Picture> newPictures = new ArrayList<>();
            JsonParser parser = new JsonParser();
            for (String request : urls) {
//                String request = BASE_URL + ROVER + "/photos?earth_date=" + d + "&api_key=" + KEY;
                try {
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
                            newPictures.add(picture);
    //                        picsToLoad++;
                        }
                    }
                } catch (UnknownHostException e){
                    exception = e;
                } catch (IOException e) {
                    e.printStackTrace();
                    exception = e;
                }
            }
//                loading = false;
            return newPictures;
        }

        @Override
        protected void onPostExecute(List<Picture> newPictures) {
            data.addAll(newPictures);
            if(exception == null) {
                callback.onDataLoaded(data, newPictures.size());
            } else {
                callback.onFailedToLoad(exception);
            }
        }
    }

    public List<Picture> loadFavorites(Context context) {
        if (context.getFileStreamPath(FAV_FILE).length() > 0) {
            String fav_json = readJsonFromFile(context, FAV_FILE);
            Type listType = new TypeToken<List<Picture>>() {}.getType();
            return /*favorites =*/ gson.fromJson(fav_json, listType);
        }
        return new ArrayList<>();
    }

    public void saveFavorites(Context context, List<Picture> pictures){
        String json = gson.toJson(pictures);
        writeFile(context, FAV_FILE, json);
    }

    // util methods

    private static String readJsonFromUrl(String url) throws IOException, UnknownHostException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            return sb.toString();
        }
    }

    private static String readJsonFromFile(Context context, String fileName){
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

    private static void writeFile(Context context, String fileName, String content){
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
