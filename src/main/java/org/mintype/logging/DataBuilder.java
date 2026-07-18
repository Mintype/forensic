package org.mintype.logging;

import com.google.gson.JsonObject;

public class DataBuilder {

    public static JsonObject block(String block) {
        JsonObject json = new JsonObject();
        json.addProperty("block", block);
        return json;
    }

    public static JsonObject explosion(String cause, String block) {
        JsonObject json = new JsonObject();
        json.addProperty("cause", cause);
        json.addProperty("block", block);
        return json;
    }
}