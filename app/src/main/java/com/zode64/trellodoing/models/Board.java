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

    public String getClockedOffListId() {
        return clockedOffList != null ? clockedOffList.getId() : null;
    }

    public void setClockedOffList( List clockedOffList ) {
        this.clockedOffList = clockedOffList;
    }

    public String getTodayListId() {
        return todayList != null ? todayList.getId() : null;
    }

    public void setTodayList( List todoList ) {
        this.todayList = todoList;
    }

    public String getDoingListId() {
        return doingList != null ? doingList.getId() : null;
    }

    public void setDoingList( List doingList ) {
        this.doingList = doingList;
    }

    public String getDoneListId() {
        return doneList != null ? doneList.getId() : null;
    }

    public void setDoneList( List doneList ) {
        this.doneList = doneList;
    }
}
