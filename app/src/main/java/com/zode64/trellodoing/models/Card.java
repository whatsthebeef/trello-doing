package com.zode64.trellodoing.models;

import java.util.Calendar;

public class Card {

    public enum ListType {
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
    private String clockedOffList;
    private ListType inListType;

    private long deadline;
    private int isClockedOff;
    private int isPendingPush;

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

    public String getClockedOffList() {
        return clockedOffList;
    }

    public void setClockedOffList( String clockedOffList ) {
        this.clockedOffList = clockedOffList;
    }

    public int getIsClockedOff() {
        return isClockedOff;
    }

    public void setIsClockedOff( int isClockedOff ) {
        this.isClockedOff = isClockedOff;
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
        return inListType.ordinal();
    }

    public void setInListType( int inListType ) {
        this.inListType = ListType.values()[inListType];
    }
}

