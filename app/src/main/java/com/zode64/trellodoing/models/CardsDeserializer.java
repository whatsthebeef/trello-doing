package com.zode64.trellodoing.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CardsDeserializer implements JsonDeserializer<Member> {

    private static final String CARD = "card";
    private static final String ACTIONS = "actions";
    private static final String BOARD = "board";
    private static final String SHORT_LINK = "shortLink";
    private static final String NAME = "name";
    private static final String DATA = "data";
    private static final String ID_LIST = "idList";
    private static final String ID = "id";
    private static final String DATE = "date";

    private HashMap<String, Board> boardReg;

    public CardsDeserializer( HashMap<String, Board> boardReg ) {
        super();
        this.boardReg = boardReg;
    }

    @Override
    public Member deserialize( JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        Member member = new Member();
        JsonObject obj = json.getAsJsonObject();
        Map<String, Card> cardReg = new HashMap<>();
        Iterator<JsonElement> actions = obj.getAsJsonArray( ACTIONS ).iterator();
        while ( actions.hasNext() ) {
            JsonElement action = actions.next();
            JsonObject actionObj = action.getAsJsonObject();
            Iterator<Map.Entry<String, JsonElement>> dataAttrs = actionObj.getAsJsonObject( DATA )
                    .entrySet().iterator();
            Card card = new Card();
            String cardListId = null;
            boolean dontAdd = false;
            while ( dataAttrs.hasNext() ) {
                if ( dontAdd ) {
                    break;
                }
                Map.Entry<String, JsonElement> dataAttr = dataAttrs.next();
                switch ( dataAttr.getKey() ) {
                    case CARD:
                        Iterator<Map.Entry<String, JsonElement>> cardAttrs = dataAttr.getValue().getAsJsonObject().entrySet().iterator();
                        while ( cardAttrs.hasNext() ) {
                            if ( dontAdd ) {
                                break;
                            }
                            Map.Entry<String, JsonElement> cardAttr = cardAttrs.next();
                            switch ( cardAttr.getKey() ) {
                                case NAME:
                                    card.setName( cardAttr.getValue().getAsString() );
                                    break;
                                case SHORT_LINK:
                                    card.setShortLink( cardAttr.getValue().getAsString() );
                                    break;
                                case ID:
                                    card.setServerId( cardAttr.getValue().getAsString() );
                                    if ( !cardReg.containsKey( card.getServerId() ) ) {
                                        cardReg.put( card.getServerId(), card );
                                    } else {
                                        dontAdd = true;
                                    }
                                    break;
                                case ID_LIST:
                                    cardListId = cardAttr.getValue().getAsString();
                                    break;
                            }
                        }
                        break;
                    case BOARD:
                        Iterator<Map.Entry<String, JsonElement>> boardAttrs = dataAttr.getValue().getAsJsonObject().entrySet().iterator();
                        while ( boardAttrs.hasNext() ) {
                            if ( dontAdd ) {
                                break;
                            }
                            Map.Entry<String, JsonElement> boardAttr = boardAttrs.next();
                            switch ( boardAttr.getKey() ) {
                                case ID:
                                    card.setBoardId( boardAttr.getValue().getAsString() );
                                    if ( !boardReg.containsKey( card.getBoardId() ) ) {
                                        dontAdd = true;
                                    }
                                    break;
                            }
                        }
                    default:
                        // ignore
                }
            }
            if ( !dontAdd ) {
                card.setListType( boardReg.get( card.getBoardId() ).getListType( cardListId ) );
                if ( card.getListType() != null ) {
                    String date = actionObj.getAsJsonPrimitive( DATE ).getAsString();
                    DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
                    try {
                        card.setStartTimeOfCurrentListType( dateFormat.parse( date ).getTime() );
                    } catch ( ParseException e ) {
                        throw new RuntimeException( "Can't handle date format received from trello" + ": " + date);
                    }
                    member.addCard( card );
                }
            }
        }
        return member;
    }
}
