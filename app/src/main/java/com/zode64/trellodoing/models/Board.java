package com.zode64.trellodoing.models;

public class Board {

    private String name;
    private String id;

    private List todayList;
    private List clockedOffList;
    private List doingList;
    private List doneList;

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

    public List getClockedOffList() {
        return clockedOffList;
    }

    public String getClockedOffListId() {
        return clockedOffList.getId();
    }

    public void setClockedOffList( List clockedOffList ) {
        this.clockedOffList = clockedOffList;
    }

    public List getTodayList() {
        return todayList;
    }

    public String getTodayListId() {
        return todayList.getId();
    }

    public void setTodayList( List todoList ) {
        this.todayList = todoList;
    }

    public List getDoingList() {
        return doingList;
    }

    public String getDoingListId() {
        return doingList.getId();
    }

    public void setDoingList( List doingList ) {
        this.doingList = doingList;
    }

    public List getDoneList() {
        return doneList;
    }

    public String getDoneListId() {
        return doneList.getId();
    }

    public void setDoneList( List doneList ) {
        this.doneList = doneList;
    }
}
