package com.zode64.trellodoing.models;

import java.util.ArrayList;

public class CardsStatus {

    private ArrayList<Card> cards = new ArrayList<>();
    private ArrayList<Board> boards = new ArrayList<>();
    private String memberId;

    public ArrayList<Card> getCards() {
        return cards;
    }

    void addCard( Card card ) {
        cards.add( card );
    }

    public ArrayList<Board> getBoards() {
        return boards;
    }

    public void addBoard( Board board ) {
        this.boards.add( board );
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId( String memberId ) {
        this.memberId = memberId;
    }
}
