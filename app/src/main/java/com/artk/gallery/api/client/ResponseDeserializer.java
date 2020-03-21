package com.artk.gallery.api.client;

import com.artk.gallery.data.Picture;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResponseDeserializer implements JsonDeserializer<CallResponse> {

    @Override
    public CallResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject rootObj = json.getAsJsonObject();
        JsonArray arr = rootObj.get("photos").getAsJsonArray();
        if (arr != null) {
            List<Picture> list = new ArrayList();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                int id = o.get("id").getAsInt();
                String url = o.get("img_src").getAsString();
                String date = o.get("earth_date").getAsString();
                String rover = o.get("rover").getAsJsonObject().get("name").getAsString();
                String camera = o.get("camera").getAsJsonObject().get("full_name").getAsString();
                Picture picture = new Picture(id, url, date, rover, camera);
                list.add(picture);
            }
            return new CallResponse(Collections.unmodifiableList(list));
        }
        return new CallResponse(Collections.emptyList()); // immutable empty list
    }
}
