package com.zode64.trellodoing;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.zode64.trellodoing.models.Data;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Map;

/**
 * Created by john on 9/15/15.
 */
public class ActionDataDeserializer implements JsonDeserializer<Data> {

    @Override
    public Data deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Map.Entry<String, JsonElement> entry = obj.entrySet().iterator().next();
        if ( entry == null ) return null;
        Long value = entry.getValue().getAsLong();
        return new Data( date, value );
    }
}
