package com.zode64.trellodoing.models;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Card {

    public final static String PENDING_CARD_ID = "pending";

    public enum ListType {
        UNKNOWN,
        TODO,
        THIS_WEEK,
        TODAY,
        DOING,
        CLOCKED_OFF,
        DONE,
        NOT_CHARGING,
        NONE
    }

    public enum DummyType {
        NOT,
        START_DOING,
        STOP_DOING
    }

    private Integer id;
    private String name;
    private String shortLink;
    private String boardShortLink;
    private String boardName;
    private String boardId;
    private String serverId;

    private ListType inListType;

    private long deadline;

    private DummyType dummyType = DummyType.NOT;

    private Map<ListType, String> listIds = new HashMap<>();

    public String getShortUrl() {
        return "https://trello.com/c/" + shortLink;
    }

    public String getBoardShortUrl() {
        return "https://trello.com/b/" + boardShortLink;
    }

    public boolean hasDeadline() {
        return deadline > 0;
    }

    public boolean pastDeadline() {
        return deadline < Calendar.getInstance().getTimeInMillis() && hasDeadline();
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getId() {
        return String.valueOf( id );
    }

    public void setId( Integer id ) {
        this.id = id;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId( String cardId ) {
        this.serverId = cardId;
    }

    public String getBoardShortLink() {
        return boardShortLink;
    }

    public void setBoardShortLink( String boardShortLink ) {
        this.boardShortLink = boardShortLink;
    }

    public String getShortLink() {
        return shortLink;
    }

    public void setShortLink( String shortLink ) {
        this.shortLink = shortLink;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName( String boardName ) {
        this.boardName = boardName;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId( String boardId ) {
        this.boardId = boardId;
    }

    public boolean isClockedOn() {
        return inListType == ListType.DOING;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline( long deadline ) {
        this.deadline = deadline;
    }

    public void setInListType( ListType inListType ) {
        this.inListType = inListType;
    }

    public int getInListTypeOrdinal() {
        return inListType != null ? inListType.ordinal() : 0;
    }

    public ListType getInListType() {
        return inListType;
    }

    public void setInListType( int inListType ) {
        this.inListType = ListType.values()[ inListType ];
    }

    public void setListId( ListType listType, String listId ) {
        listIds.put( listType, listId );
    }

    public String getListId( ListType listType ) {
        return listIds.get( listType );
    }

    public String getCurrentListId() {
        return listIds.get( inListType );
    }

    public void resetDeadline() {
        setDeadline( -1 );
    }

    public DummyType getDummyType() {
        return dummyType;
    }

    public void setDummyType( DummyType dummyType ) {
        this.dummyType = dummyType;
    }


}

