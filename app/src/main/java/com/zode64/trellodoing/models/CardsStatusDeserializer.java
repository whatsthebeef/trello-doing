package com.zode64.trellodoing.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardsStatusDeserializer implements JsonDeserializer<CardsStatus> {

    private static final String TAG = CardsStatusDeserializer.class.getSimpleName();

    private static final String TWO_HUNDRED = "200";
    private static final String CARD = "card";
    private static final String SHORT_LINK = "shortLink";
    private static final String NAME = "name";
    private static final String DATA = "data";
    private static final String ID_LIST = "idList";
    private static final String ID = "id";
    private static final String LIST_AFTER = "listAfter";
    private static final String LIST_BEFORE = "listBefore";
    private static final String DOING_LIST_NAME = "Doing";
    private static final String ID_MEMBER_CREATOR = "idMemberCreator";
    private static final String CLOCKED_OFF_LIST_NAME = "Clocked Off";

    private HashMap<String, Board> boardReg;

    private String userId;

    public CardsStatusDeserializer( HashMap<String, Board> boardReg, String userId ) {
        super();
        this.boardReg = boardReg;
        this.userId = userId;
    }

    @Override
    public CardsStatus deserialize( JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        CardsStatus cardsStatus = new CardsStatus();
        JsonArray jsonArray = json.getAsJsonArray();
        int i = 0;
        Map<String, Card> cardReg = new HashMap<>();
        List<String> notDoingReg = new ArrayList<>();
        List<String> notClockedOffReg = new ArrayList<>();
        for ( JsonElement element : jsonArray ) {
            JsonArray cardsOrActions = element.getAsJsonObject().getAsJsonArray( TWO_HUNDRED );
            for ( JsonElement cardOrAction : cardsOrActions ) {
                if ( i % 2 == 0 ) {
                    Card card = new Card();
                    JsonObject cardObj = cardOrAction.getAsJsonObject();
                    card.setName( cardObj.get( NAME ).getAsString() );
                    card.setShortLink( cardObj.get( SHORT_LINK ).getAsString() );
                    card.setServerId( cardObj.get( ID ).getAsString() );
                    for ( Board board : boardReg.values() ) {
                        Board.ListType type = board.getListType( cardObj.get( ID_LIST ).getAsString() );
                        if ( type != null ) {
                            card.setBoard( board );
                            card.setBoardId( board.getId() );
                            card.setListType( type );
                            break;
                        }
                    }
                    cardReg.put( card.getServerId(), card );
                    if ( card.getListType() == Board.ListType.THIS_WEEK
                            || card.getListType() == Board.ListType.TODAY
                            || card.getListType() == Board.ListType.DOING
                            || card.getListType() == Board.ListType.CLOCKED_OFF
                            ) {
                        cardsStatus.addCard( card );
                    }
                } else {
                    String userId = cardOrAction.getAsJsonObject().get( ID_MEMBER_CREATOR ).getAsString();
                    if ( !this.userId.equals( userId ) ) {
                        continue;
                    }
                    JsonObject dataObj = cardOrAction.getAsJsonObject().get( DATA ).getAsJsonObject();
                    Card card = cardReg.get( dataObj.get( CARD ).getAsJsonObject().get( ID ).getAsString() );
                    JsonElement listAfterElement = dataObj.get( LIST_AFTER );
                    if ( card != null && listAfterElement != null ) {
                        if ( listAfterElement.getAsJsonObject().get( NAME ).getAsString().equals( DOING_LIST_NAME ) ) {
                            if ( !notDoingReg.contains( card.getServerId() ) ) {
                                card.setCurrentUserDoing( true );
                            }
                        } else {
                            if ( !notDoingReg.contains( card.getServerId() ) ) {
                                card.setCurrentUserDoing( false );
                            }
                        }
                        notDoingReg.add( card.getServerId() );
                        if ( listAfterElement.getAsJsonObject().get( NAME ).getAsString().equals( CLOCKED_OFF_LIST_NAME ) ) {
                            if ( !notClockedOffReg.contains( card.getServerId() ) ) {
                                card.setCurrentUserClockedOff( true );
                                notClockedOffReg.add( card.getServerId() );
                            }
                        } else if ( dataObj.get( LIST_BEFORE ).getAsJsonObject().get( NAME ).getAsString().equals( CLOCKED_OFF_LIST_NAME ) ) {
                            if ( !notClockedOffReg.contains( card.getServerId() ) ) {
                                card.setCurrentUserClockedOff( false );
                                notClockedOffReg.add( card.getServerId() );
                            }
                        }
                    }
                }
            }
            i++;
        }
        return cardsStatus;
    }
}
