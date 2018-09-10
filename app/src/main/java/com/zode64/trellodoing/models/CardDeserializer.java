package com.zode64.trellodoing.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.util.Map;

public class CardDeserializer implements JsonDeserializer<Card> {

    private static final String ID = "id";

    @Override
    public Card deserialize( JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        Card card = new Card();
        for ( Map.Entry<String, JsonElement> cardAttr : json.getAsJsonObject().entrySet() ) {
            switch ( cardAttr.getKey() ) {
                case ID:
                    card.setServerId( cardAttr.getValue().getAsString() );
                    break;
            }
        }
        return card;
    }
}
