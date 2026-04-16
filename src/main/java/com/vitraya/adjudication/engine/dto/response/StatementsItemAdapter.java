package com.vitraya.adjudication.engine.dto.response;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class StatementsItemAdapter extends TypeAdapter<List<StatementsItem>> {

    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, List<StatementsItem> value) throws IOException {
        out.beginArray();
        if (value != null) {
            for (StatementsItem item : value) {
                gson.toJson(item, StatementsItem.class, out);
            }
        }
        out.endArray();
    }

    @Override
    public List<StatementsItem> read(JsonReader reader) throws IOException {
        List<StatementsItem> result = new ArrayList<>();

        JsonElement element = JsonParser.parseReader(reader);

        if (!element.isJsonArray()) {
            return result;
        }

        JsonArray arr = element.getAsJsonArray();

        for (JsonElement el : arr) {
            if (el.isJsonObject()) {
                result.add(gson.fromJson(el, StatementsItem.class));
            } else if (el.isJsonArray()) {
                // flatten nested arrays
                for (JsonElement inner : el.getAsJsonArray()) {
                    result.add(gson.fromJson(inner, StatementsItem.class));
                }
            }
        }

        return result;
    }
}

