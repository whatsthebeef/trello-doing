package com.zode64.trellodoing.models;

/**
 * Created by john on 2/3/15.
 */
public class Data {
    private List listAfter;
    private List listBefore;
    private Board board;
    private Card card;

    public List getListAfter() {
        return listAfter;
    }

    public void setListAfter(List listAfter) {
        this.listAfter = listAfter;
    }

    public List getListBefore() {
        return listBefore;
    }

    public void setListBefore(List listBefore) {
        this.listBefore = listBefore;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
