package com.zode64.trellodoing.models;

import android.util.Log;

/**
 * Created by john on 2/3/15.
 */
public class Action {

    private static final String TAG = Action.class.getName();

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public enum Status {
        WORK,
        PERSONAL
    }

    public enum Type {
        DOING,
        WAS_DOING,
        OTHER
    }

    private String boardShortLink;
    private String boardName;
    private String boardId;
    private Status status;
    private Type type;
    private String cardName;
    private String cardId;
    private String id;

    public boolean isWorkAction() {
        return status == Status.WORK;
    }

    public boolean isPersonalAction() {
        return status == Status.PERSONAL;
    }

    public boolean isStoppedDoingAction() {
        Log.i( TAG, cardName + " has stopped doing : " + (type == Type.WAS_DOING));
        return type == Type.WAS_DOING;
    }

    public boolean isDoingAction() {
        return type == Type.DOING;
    }

    public String getBoardShortUrl() {
        return "https://trello.com/b/" + boardShortLink;
    }

    public void setBoardShortLink(String boardShortLink) {
        this.boardShortLink = boardShortLink;
    }

    public String getBoardShortLink() {
        return boardShortLink;
    }

    public void setBoardName( String boardName ) {
        this.boardName = boardName;
    }

    public String getBoardName() {
        return this.boardName;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId( String boardId ) {
        this.boardId = boardId;
    }

    public Type getType() {
        return type;
    }

    public void setType( Type type ) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus( Status status ) {
        this.status = status;
    }

    public void setCardName( String cardName ) {
        this.cardName = cardName;
    }

    public String getCardName() {
        return this.cardName;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId( String cardId ) {
        this.cardId = cardId;
    }

}
