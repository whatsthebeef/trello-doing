package com.zode64.trellodoing;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Action.Status;
import com.zode64.trellodoing.models.Action.Type;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by john on 9/15/15.
 */
public class ActionDeserializer implements JsonDeserializer<Action> {

    private static final String TAG = ActionDeserializer.class.getName();

    private static final String CARD = "card";
    private static final String BOARD = "board";
    private static final String SHORT_LINK = "shortLink";
    private static final String NAME = "name";
    private static final String LIST_AFTER = "listAfter";
    private static final String LIST_BEFORE = "listBefore";
    private static final String ID = "id";

    private static final String PERSONAL_BOARD_NAME = "Personal";

    private static final String DOING_LIST = "Doing";

    @Override
    public Action deserialize( JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Iterator<Map.Entry<String, JsonElement>> entries = obj.getAsJsonObject( "data" ).entrySet().iterator();
        Action action = new Action();
        action.setId( obj.getAsJsonPrimitive( "id" ).getAsString());
        action.setType( Type.OTHER );
        while(entries.hasNext()) {
            Map.Entry<String, JsonElement> entry = entries.next();
            switch ( entry.getKey() ) {
                case CARD:
                    Iterator<Map.Entry<String, JsonElement>> card = entry.getValue().getAsJsonObject().entrySet().iterator();
                    while(card.hasNext()) {
                        Map.Entry<String, JsonElement> cardAttribute = card.next();
                        switch ( cardAttribute.getKey() ) {
                            case NAME:
                                action.setCardName( cardAttribute.getValue().getAsString() );
                                break;
                            case ID:
                                action.setCardId( cardAttribute.getValue().getAsString() );
                                break;
                        }
                    }
                    break;
                case BOARD:
                    Iterator<Map.Entry<String, JsonElement>> board = entry.getValue().getAsJsonObject().entrySet().iterator();
                    while(board.hasNext()) {
                        Map.Entry<String, JsonElement> boardAttribute = board.next();
                        switch ( boardAttribute.getKey() ) {
                            case SHORT_LINK:
                                action.setBoardShortLink(boardAttribute.getValue().getAsString());
                                break;
                            case NAME:
                                if(PERSONAL_BOARD_NAME.equals(boardAttribute.getValue().getAsString())) {
                                    action.setStatus(Status.PERSONAL);
                                }
                                else {
                                    action.setStatus(Status.WORK);
                                }
                                break;
                            case ID:
                                action.setBoardId(boardAttribute.getValue().getAsString());
                                break;
                            default:
                                // ignore
                        }
                    }
                    break;
                case LIST_AFTER:
                    String listAfter = entry.getValue().getAsJsonObject().get( NAME ).getAsString();
                    if ( DOING_LIST.equals(listAfter) ) {
                        action.setType( Type.DOING );
                    }
                    break;
                case LIST_BEFORE:
                    String listBefore = entry.getValue().getAsJsonObject().get( NAME ).getAsString();
                    if ( DOING_LIST.equals(listBefore) ) {
                        action.setType( Type.WAS_DOING );
                    }
                    break;
                default:
                    // ignore
            }
        }
        return action;
    }
}
