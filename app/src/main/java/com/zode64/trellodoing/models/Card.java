package com.zode64.trellodoing.models;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Card {

    public final static int TRUE = 1;
    public final static int FALSE = 0;

    public enum ListType {
        UNKNOWN,
        TODO,
        TODAY,
        DOING,
        CLOCKED_OFF,
        DONE
    }

    private String name;
    private String boardShortLink;
    private String boardName;
    private String boardId;
    private String id;

    private ListType inListType;

    private long deadline;

    private int isPendingPush;

    private Map<ListType, String> listIds = new HashMap<>();

    public String getBoardShortUrl() {
        return "https://trello.com/b/" + boardShortLink;
    }

    public boolean hasDeadline() {
        return deadline > 0;
    }

    public boolean pastDeadline() {
        return deadline > Calendar.getInstance().getTimeInMillis();
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getBoardShortLink() {
        return boardShortLink;
    }

    public void setBoardShortLink( String boardShortLink ) {
        this.boardShortLink = boardShortLink;
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

    public boolean isClockedOff() {
        return inListType == ListType.CLOCKED_OFF;
    }

    public void setClockedOff() {
        inListType = ListType.CLOCKED_OFF;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline( long deadline ) {
        this.deadline = deadline;
    }

    public int getIsPendingPush() {
        return isPendingPush;
    }

    public boolean isPendingPush() {
        return isPendingPush == 1;
    }

    public void setIsPendingPush( int isPendingPush ) {
        this.isPendingPush = isPendingPush;
    }

    public ListType getInListType() {
        return inListType;
    }

    public void setInListType( ListType inListType ) {
        this.inListType = inListType;
    }

    public int getInListTypeOrdinal() {
        if ( inListType != null ) {
            return inListType.ordinal();
        } else {
            return 0;
        }
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

    public boolean isToday() {
        return inListType == ListType.TODAY;
    }

    public void setToday() {
        inListType = ListType.TODAY;
    }


}

