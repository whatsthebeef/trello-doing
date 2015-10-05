package com.zode64.trellodoing;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.List;
import com.zode64.trellodoing.models.Member;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.zode64.trellodoing.models.Card.ListType.*;

public class MemberDeserializer implements JsonDeserializer<Member> {

    private static final String CARD = "card";
    private static final String ACTIONS = "actions";
    private static final String BOARD = "board";
    private static final String BOARDS = "boards";
    private static final String SHORT_LINK = "shortLink";
    private static final String NAME = "name";
    private static final String LISTS = "lists";
    private static final String DATA = "data";
    private static final String LIST_AFTER = "listAfter";
    private static final String LIST_BEFORE = "listBefore";
    private static final String ID = "id";

    private static final String PERSONAL_BOARD_NAME = "Personal";

    private static final String DOING_LIST = "Doing";
    private static final String TODAY_LIST = "Today";
    private static final String CLOCKED_OFF_LIST = "Clocked Off";
    private static final String TODO_LIST = "Todo";

    @Override
    public Member deserialize( JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        Member member = new Member();
        JsonObject obj = json.getAsJsonObject();
        Iterator<JsonElement> boardsItr = obj.getAsJsonArray( BOARDS ).iterator();
        Map<String, Board> boardReg = new HashMap<>();
        while ( boardsItr.hasNext() ) {
            Iterator<Map.Entry<String, JsonElement>> boardAttrs = boardsItr.next().getAsJsonObject().entrySet().iterator();
            Board board = new Board();
            while ( boardAttrs.hasNext() ) {
                Map.Entry<String, JsonElement> boardAttr = boardAttrs.next();
                switch ( boardAttr.getKey() ) {
                    case NAME:
                        board.setName( boardAttr.getValue().getAsString() );
                        break;
                    case ID:
                        board.setId( boardAttr.getValue().getAsString() );
                        break;
                    case LISTS:
                        Iterator<JsonElement> listsItr = boardAttr.getValue().getAsJsonArray().iterator();
                        List list = new List();
                        while ( listsItr.hasNext() ) {
                            Iterator<Map.Entry<String, JsonElement>> listAttrs = listsItr.next().getAsJsonObject().entrySet().iterator();
                            while ( listAttrs.hasNext() ) {
                                Map.Entry<String, JsonElement> listAttr = listAttrs.next();
                                switch ( listAttr.getKey() ) {
                                    case ID:
                                        list.setId( listAttr.getValue().getAsString() );
                                        break;
                                    case NAME:
                                        if ( listAttr.getValue().getAsString().equals( CLOCKED_OFF_LIST ) ) {
                                            board.setClockedOffList( list );
                                        } else if ( listAttr.getValue().getAsString().equals( TODAY_LIST ) ) {
                                            board.setTodayList( list );
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                    default:
                        // ignore
                }
            }
            boardReg.put( board.getName(), board );
            if ( PERSONAL_BOARD_NAME.equals( board.getName() ) ) {
                member.setPersonalTodayListId( board.getId() );
            }
        }

        Map<String, Card> cardReg = new HashMap<>();
        Iterator<JsonElement> actions = obj.getAsJsonArray( ACTIONS ).iterator();
        while ( actions.hasNext() ) {
            JsonElement action = actions.next();
            Iterator<Map.Entry<String, JsonElement>> dataAttrs = action.getAsJsonObject().getAsJsonObject( DATA )
                    .entrySet().iterator();
            Map.Entry<String, JsonElement> boardEntry = null;
            Map.Entry<String, JsonElement> listAfterEntry = null;
            Card card = new Card();
            while ( dataAttrs.hasNext() ) {
                Map.Entry<String, JsonElement> dataAttr = dataAttrs.next();
                switch ( dataAttr.getKey() ) {
                    case CARD:
                        Iterator<Map.Entry<String, JsonElement>> cardAttrs = dataAttr.getValue().getAsJsonObject().entrySet().iterator();
                        while ( cardAttrs.hasNext() ) {
                            Map.Entry<String, JsonElement> cardAttr = cardAttrs.next();
                            switch ( cardAttr.getKey() ) {
                                case NAME:
                                    card.setName( cardAttr.getValue().getAsString() );
                                    break;
                                case ID:
                                    card.setId( cardAttr.getValue().getAsString() );
                                    break;
                            }
                        }
                        break;
                    case BOARD:
                        boardEntry = dataAttr;
                        break;
                    case LIST_AFTER:
                        listAfterEntry = dataAttr;
                        break;
                    default:
                        // ignore
                }
            }

            if ( !cardReg.containsKey( card.getId() ) ) {
                cardReg.put( card.getId(), card );
            } else {
                break;
            }

            String listAfter = listAfterEntry.getValue().getAsJsonObject().get( NAME ).getAsString();
            Iterator<Map.Entry<String, JsonElement>> boardAttrs = boardEntry.getValue().getAsJsonObject().entrySet().iterator();
            while ( boardAttrs.hasNext() ) {
                Map.Entry<String, JsonElement> boardAttribute = boardAttrs.next();
                switch ( boardAttribute.getKey() ) {
                    case SHORT_LINK:
                        card.setBoardShortLink( boardAttribute.getValue().getAsString() );
                        break;
                    case NAME:
                        String boardName = boardAttribute.getValue().getAsString();
                        card.setBoardName( boardName );
                        card.setClockedOffList( boardReg.get( boardName ).getClockedOffListId() );
                        if ( PERSONAL_BOARD_NAME.equals( boardName ) ) {
                            if ( isDoingList( listAfter ) ) {
                                member.addPersonalDoingCard( card );
                                card.setInListType( DOING );
                            } else if ( isTodayList( listAfter ) ) {
                                member.addPersonalTodayCard( card );
                                card.setInListType( TODAY );
                            }
                        } else {
                            if ( isDoingList( listAfter ) ) {
                                member.addWorkDoingCard( card );
                                card.setInListType( DOING );
                            } else if ( isTodayList( listAfter ) ) {
                                member.addPersonalTodayCard( card );
                                card.setInListType( TODAY );
                            }
                        }
                        break;
                    case ID:
                        card.setBoardId( boardAttribute.getValue().getAsString() );
                        break;
                    default:
                        // ignore
                }
            }
        }
        return member;
    }

    private boolean isDoingList( String list ) {
        if ( DOING_LIST.equals( list ) ) {
            return true;
        }
        return false;
    }

    private boolean isTodayList( String list ) {
        if ( TODAY_LIST.equals( list ) ) {
            return true;
        }
        return false;
    }

}
