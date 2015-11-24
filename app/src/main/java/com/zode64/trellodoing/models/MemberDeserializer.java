package com.zode64.trellodoing.models;

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

import static com.zode64.trellodoing.models.Card.ListType.CLOCKED_OFF;
import static com.zode64.trellodoing.models.Card.ListType.DOING;
import static com.zode64.trellodoing.models.Card.ListType.THIS_WEEK;
import static com.zode64.trellodoing.models.Card.ListType.TODAY;
import static com.zode64.trellodoing.models.Card.ListType.TODO;

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
    private static final String ID = "id";

    private static final String DOING_LIST = "Doing";
    private static final String DONE_LIST = "Done";
    private static final String TODAY_LIST = "Today";
    private static final String CLOCKED_OFF_LIST = "Clocked Off";
    private static final String THIS_WEEK_LIST = "This Week";
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
                    case SHORT_LINK:
                        board.setShortLink( boardAttr.getValue().getAsString() );
                        break;
                    case ID:
                        board.setId( boardAttr.getValue().getAsString() );
                        break;
                    case LISTS:
                        Iterator<JsonElement> listsItr = boardAttr.getValue().getAsJsonArray().iterator();
                        while ( listsItr.hasNext() ) {
                            Iterator<Map.Entry<String, JsonElement>> listAttrs = listsItr.next().getAsJsonObject().entrySet().iterator();
                            List list = new List();
                            while ( listAttrs.hasNext() ) {
                                Map.Entry<String, JsonElement> listAttr = listAttrs.next();
                                switch ( listAttr.getKey() ) {
                                    case ID:
                                        list.setId( listAttr.getValue().getAsString() );
                                        break;
                                    case NAME:
                                        String listName = listAttr.getValue().getAsString();
                                        switch ( listName ) {
                                            case CLOCKED_OFF_LIST:
                                                board.setClockedOffList( list );
                                                break;
                                            case TODAY_LIST:
                                                board.setTodayList( list );
                                                break;
                                            case DOING_LIST:
                                                board.setDoingList( list );
                                                break;
                                            case DONE_LIST:
                                                board.setDoneList( list );
                                                break;
                                            case TODO_LIST:
                                                board.setTodoList( list );
                                                break;
                                            case THIS_WEEK_LIST:
                                                board.setThisWeekList( list );
                                                break;
                                            default:
                                                // ignore
                                        }
                                        break;
                                    default:
                                        // ignore
                                }
                            }
                        }
                        break;
                    default:
                        // ignore
                }
            }
            member.addBoard( board );
            boardReg.put( board.getName(), board );
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
                                case SHORT_LINK:
                                    card.setShortLink( cardAttr.getValue().getAsString() );
                                    break;
                                case ID:
                                    card.setServerId( cardAttr.getValue().getAsString() );
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

            if ( !cardReg.containsKey( card.getServerId() ) ) {
                cardReg.put( card.getServerId(), card );
            } else {
                continue;
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
                        Board board = boardReg.get( boardName );
                        if(board != null) {
                            card.setListId( Card.ListType.DOING, board.getDoingListId() );
                            card.setListId( Card.ListType.TODAY, board.getTodayListId() );
                            card.setListId( Card.ListType.THIS_WEEK, board.getThisWeekListId() );
                            card.setListId( Card.ListType.CLOCKED_OFF, board.getClockedOffListId() );
                            card.setListId( Card.ListType.DONE, board.getDoneListId() );
                            card.setListId( Card.ListType.TODO, board.getTodoListId() );
                        }
                        switch ( listAfter ) {
                            case DOING_LIST:
                                card.setInListType( DOING );
                                member.addCard( card );
                                break;
                            case CLOCKED_OFF_LIST:
                                card.setInListType( CLOCKED_OFF );
                                member.addCard( card );
                                break;
                            case TODAY_LIST:
                                card.setInListType( TODAY );
                                member.addCard( card );
                                break;
                            case THIS_WEEK_LIST:
                                card.setInListType( THIS_WEEK );
                                member.addCard( card );
                                break;
                            default:
                                // ignore
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
}
