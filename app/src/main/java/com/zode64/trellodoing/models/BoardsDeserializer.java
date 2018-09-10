package com.zode64.trellodoing.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Iterator;
import java.util.Map;

public class BoardsDeserializer implements JsonDeserializer<Member> {

    private static final String BOARDS = "boards";
    private static final String SHORT_LINK = "shortLink";
    private static final String NAME = "name";
    private static final String LISTS = "lists";
    private static final String ID = "id";
    private static final String ID_ORGANIZATION = "idOrganization";

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
        for ( JsonElement jsonElement1 : obj.getAsJsonArray( BOARDS ) ) {
            Iterator<Map.Entry<String, JsonElement>> boardAttrs = jsonElement1.getAsJsonObject().entrySet().iterator();
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
                    case ID_ORGANIZATION:
                        if ( !boardAttr.getValue().isJsonNull() ) {
                            board.setIdOrganization( boardAttr.getValue().getAsString() );
                        }
                        break;
                    case ID:
                        board.setId( boardAttr.getValue().getAsString() );
                        break;
                    case LISTS:
                        for ( JsonElement jsonElement : boardAttr.getValue().getAsJsonArray() ) {
                            Iterator<Map.Entry<String, JsonElement>> listAttrs = jsonElement.getAsJsonObject().entrySet().iterator();
                            String listId = null;
                            Board.ListType listType = null;
                            while ( listAttrs.hasNext() ) {
                                Map.Entry<String, JsonElement> listAttr = listAttrs.next();
                                switch ( listAttr.getKey() ) {
                                    case ID:
                                        listId = listAttr.getValue().getAsString();
                                        break;
                                    case NAME:
                                        String listName = listAttr.getValue().getAsString();
                                        switch ( listName ) {
                                            case CLOCKED_OFF_LIST:
                                                listType = Board.ListType.CLOCKED_OFF;
                                                break;
                                            case TODAY_LIST:
                                                listType = Board.ListType.TODAY;
                                                break;
                                            case DOING_LIST:
                                                listType = Board.ListType.DOING;
                                                break;
                                            case DONE_LIST:
                                                listType = Board.ListType.DONE;
                                                break;
                                            case TODO_LIST:
                                                listType = Board.ListType.TODO;
                                                break;
                                            case THIS_WEEK_LIST:
                                                listType = Board.ListType.THIS_WEEK;
                                                break;
                                            default:
                                                // ignore
                                        }
                                        break;
                                    default:
                                        // ignore
                                }
                            }
                            board.addList( listId, listType );
                        }
                        break;
                    default:
                        // ignore
                }
            }
            member.addBoard( board );
        }
        return member;
    }
}
