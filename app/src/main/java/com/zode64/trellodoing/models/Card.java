package com.zode64.trellodoing.models;

import java.util.Calendar;

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
    private String clockedOffList;
    private String doingList;
    private String doneList;
    private String todayList;

    private ListType inListType;

    private long deadline;

    private int isClockedOff;
    private int isClockedOn;
    private int isToday;
    private int isDone;
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

    public String getInListTypeOrdinalStr() {
        return String.valueOf( inListType.ordinal() );
    }

    public void setInListType( int inListType ) {
        this.inListType = ListType.values()[ inListType ];
    }

    public String getDoingList() {
        return doingList;
    }

    public void setDoingList( String doingList ) {
        this.doingList = doingList;
    }

    public int getIsClockedOn() {
        return isClockedOn;
    }

    public void setIsClockedOn( int isClockedOn ) {
        this.isClockedOn = isClockedOn;
    }

    public String getDoneList() {
        return doneList;
    }

    public void setDoneList( String doneList ) {
        this.doneList = doneList;
    }

    public int getIsDone() {
        return isDone;
    }

    public void setIsDone( int isDone ) {
        this.isDone = isDone;
    }

    public String getTodayList() {
        return todayList;
    }

    public void setTodayList( String todayList ) {
        this.todayList = todayList;
    }

    public int getIsToday() {
        return isToday;
    }

    public void setIsToday( int isToday ) {
        this.isToday = isToday;
    }

}

