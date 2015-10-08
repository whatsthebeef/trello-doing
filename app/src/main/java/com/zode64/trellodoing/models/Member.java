package com.zode64.trellodoing.models;

import java.util.ArrayList;

public class Member {

    private ArrayList<Card> cards = new ArrayList<>();
    private ArrayList<Board> boards = new ArrayList<>();

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void addBoard( Board board ) {
        boards.add( board );
    }

    public ArrayList<Board> getBoards() {
        return boards;
    }

    public void addCard( Card card ) {
        cards.add( card );
    }

}
