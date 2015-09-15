package com.zode64.trellodoing.models;

/**
 * Created by john on 2/3/15.
 */
public class Action {

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Card getCard() {
        return getData().getCard();
    }

    public Board getBoard() {
        return getData().getBoard();
    }

    public boolean isDoingAction() {
        if(List.DOING_LIST.equals(data.getListAfter().getName())) {
            return true;
        }
        return false;
    }

    public boolean isStoppedDoingAction() {
        if(List.DOING_LIST.equals(data.getListBefore().getName())) {
            return true;
        }
        return false;
    }

    public boolean isPersonalAction() {
        return getBoard().isPersonalBoard();
    }

    public boolean isWorkAction() {
        return !isPersonalAction();
    }
}
