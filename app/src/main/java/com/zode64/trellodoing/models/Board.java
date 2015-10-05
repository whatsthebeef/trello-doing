package com.zode64.trellodoing.models;

public class Board {

    private String name;
    private String id;

    private List todayList;
    private List clockedOffList;

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

}
