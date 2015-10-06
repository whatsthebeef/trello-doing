package com.zode64.trellodoing.models;

import java.util.ArrayList;

public class Member {

    private static final String TAG = Member.class.getName();

    private ArrayList<Card> cards = new ArrayList<>();
    private String personalTodayListId = null;

    public String getPersonalTodayListId() {
        return personalTodayListId;
    }

    public void setPersonalTodayListId( String personalTodayListId ) {
        this.personalTodayListId = personalTodayListId;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void addCard( Card card ) {
        cards.add( card );
    }

}
