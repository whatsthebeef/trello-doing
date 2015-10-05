package com.zode64.trellodoing.models;

import java.util.ArrayList;

/**
 * Created by john on 2/3/15.
 */
public class Member {

    private static final String TAG = Member.class.getName();

    private ArrayList<Card> personalDoingCards = new ArrayList<>();
    private ArrayList<Card> personalTodayCards = new ArrayList<>();
    private ArrayList<Card> workDoingCards = new ArrayList<>();
    private ArrayList<Card> workTodayCards = new ArrayList<>();
    private String personalTodayListId = null;

    public String getPersonalTodayListId() {
        return personalTodayListId;
    }

    public void setPersonalTodayListId( String personalTodayListId ) {
        this.personalTodayListId = personalTodayListId;
    }

    public ArrayList<Card> getPersonalDoingCards() {
        return personalDoingCards;
    }

    public void addPersonalDoingCard( Card personalDoingCard ) {
        personalDoingCards.add( personalDoingCard );
    }

    public ArrayList<Card> getPersonalTodayCards() {
        return personalTodayCards;
    }

    public void addPersonalTodayCard( Card personalTodayCard ) {
        personalTodayCards.add( personalTodayCard );
    }

    public ArrayList<Card> getWorkDoingCards() {
        return workDoingCards;
    }

    public void addWorkDoingCard( Card workDoingCard ) {
        workDoingCards.add( workDoingCard );
    }

    public ArrayList<Card> getWorkTodayCards() {
        return workTodayCards;
    }

    public void addWorkTodayCards( Card workTodayCard ) {
        workTodayCards.add( workTodayCard );
    }

}
