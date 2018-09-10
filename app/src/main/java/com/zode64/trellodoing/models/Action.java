package com.zode64.trellodoing.models;

import com.zode64.trellodoing.utils.TrelloManager;

public abstract class Action {

    public enum Type {
        CREATE,
        UPDATE_NAME,
        MOVE,
        DELETE
    }

    protected int id;
    protected Card card;
    protected Type type;
    protected TrelloManager trello;

    Action( int id, Type type, Card card, TrelloManager trello ) {
        this.id = id;
        this.card = card;
        this.type = type;
        this.trello = trello;
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard( Card card ) {
        this.card = card;
    }

    public Type getType() {
        return type;
    }

    public void setType( Type type ) {
        this.type = type;
    }

    public void setType( int type ) {
        this.type = Type.values()[ type ];
    }

    public abstract boolean perform();
}
