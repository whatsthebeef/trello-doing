package com.zode64.trellodoing.models;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import static com.zode64.trellodoing.utils.TimeUtils.format;

public class CardsDeserializer implements JsonDeserializer<Member> {

    private static final String TAG = CardsDeserializer.class.getSimpleName();

    private static final String CARD = "card";
    private static final String ACTIONS = "actions";
    private static final String BOARD = "board";
    private static final String LIST = "list";
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
        for ( JsonElement action : obj.getAsJsonArray( ACTIONS ) ) {
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
                        for ( Map.Entry<String, JsonElement> stringJsonElementEntry1 : dataAttr.getValue().getAsJsonObject().entrySet() ) {
                            if ( dontAdd ) {
                                break;
                            }
                            switch ( stringJsonElementEntry1.getKey() ) {
                                case NAME:
                                    card.setName( stringJsonElementEntry1.getValue().getAsString() );
                                    break;
                                case SHORT_LINK:
                                    card.setShortLink( stringJsonElementEntry1.getValue().getAsString() );
                                    break;
                                case ID:
                                    card.setServerId( stringJsonElementEntry1.getValue().getAsString() );
                                    if ( !cardReg.containsKey( card.getServerId() ) ) {
                                        cardReg.put( card.getServerId(), card );
                                    } else {
                                        dontAdd = true;
                                    }
                                    break;
                                case ID_LIST:
                                    cardListId = stringJsonElementEntry1.getValue().getAsString();
                                    break;
                            }
                        }
                        break;
                    case LIST:
                        for ( Map.Entry<String, JsonElement> stringJsonElementEntry1 : dataAttr.getValue().getAsJsonObject().entrySet() ) {
                            if ( dontAdd ) {
                                break;
                            }
                            switch ( stringJsonElementEntry1.getKey() ) {
                                case ID:
                                    cardListId = stringJsonElementEntry1.getValue().getAsString();
                                    break;
                            }
                        }
                        break;
                    case BOARD:
                        for ( Map.Entry<String, JsonElement> stringJsonElementEntry : dataAttr.getValue().getAsJsonObject().entrySet() ) {
                            if ( dontAdd ) {
                                break;
                            }
                            switch ( stringJsonElementEntry.getKey() ) {
                                case ID:
                                    card.setBoardId( stringJsonElementEntry.getValue().getAsString() );
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
                        Calendar cal = GregorianCalendar.getInstance();
                        cal.setTimeZone( TimeZone.getDefault() );
                        cal.setTimeInMillis( card.getStartTimeForCurrentListType() );
                        Log.d( TAG, format( cal ) );
                        Log.d( TAG, cal.getTimeZone().getDisplayName() );
                    } catch ( ParseException e ) {
                        throw new RuntimeException( "Can't handle date format received from trello" + ": " + date );
                    }
                    member.addCard( card );
                }
            }
        }
        return member;
    }
}
