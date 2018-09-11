package com.zode64.trellodoing.models;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.zode64.trellodoing.utils.TimeUtils.isThisWeek;
import static com.zode64.trellodoing.utils.TimeUtils.isToday;

public class Card {



    public enum DummyType {
        NOT,
        START_DOING
    }

    private Integer id;
    private String name;
    private String shortLink;
    private Board board;
    private String boardId;
    private String serverId;
    private boolean isCurrentUserDoing = false;
    private boolean isCurrentUserClockedOff = false;

    private Board.ListType listType;

    private DummyType dummyType = DummyType.NOT;

    public String getShortUrl() {
        return "https://trello.com/c/" + shortLink;
    }

    public String getBoardShortUrl() {
        return board.getShortUrl();
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

    public String getShortLink() {
        return shortLink;
    }
    public void setShortLink( String shortLink ) {
        this.shortLink = shortLink;
    }

    public String getBoardName() {
        return board.getName();
    }

    public String getBoardId() {
        return board == null ? boardId : board.getId();
    }

    public void setBoard( Board board ) {
        this.board = board;
    }

    void setBoardId( String boardId ) {
        this.boardId = boardId;
    }

    String getListId() {
        return board.getListId( listType );
    }

    public String getBoardListId( Board.ListType listType ) {
        return board.getListId( listType );
    }

    public DummyType getDummyType() {
        return dummyType;
    }

    public void setDummyType( DummyType dummyType ) {
        this.dummyType = dummyType;
    }

    public boolean isWorkCard() {
        return board.isWorkBoard();
    }

    public Board.ListType getListType() {
        return listType;
    }

    public void setListType( Board.ListType listType ) {
        this.listType = listType;
    }

    public boolean isCurrentUserDoing() {
        return isCurrentUserDoing;
    }

    public void setCurrentUserDoing( boolean isCurrentUserDoing ) {
        this.isCurrentUserDoing = isCurrentUserDoing;
    }

    public boolean isCurrentUserClockedOff() {
        return isCurrentUserClockedOff;
    }

    public void setCurrentUserClockedOff( boolean isCurrentUserClockedOff ) {
        this.isCurrentUserClockedOff = isCurrentUserClockedOff;
    }
}

