package com.zode64.trellodoing.models;

public class Board {

    private String name;
    private String id;
    private String shortLink;

    private List todayList;
    private List clockedOffList;
    private List doingList;
    private List doneList;
    private List todoList;

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

    public String getShortUrl() {
        return "https://trello.com/b/" + shortLink;
    }

    public String getClockedOffListId() {
        return clockedOffList != null ? clockedOffList.getId() : null;
    }

    public void setClockedOffList( List clockedOffList ) {
        this.clockedOffList = clockedOffList;
    }

    public void setClockedOffListId(String id) {
        clockedOffList = new List(id);
    }

    public String getTodayListId() {
        return todayList != null ? todayList.getId() : null;
    }

    public void setTodayList( List todoList ) {
        this.todayList = todoList;
    }

    public void setTodayListId(String id) {
        todayList = new List(id);
    }

    public String getDoingListId() {
        return doingList != null ? doingList.getId() : null;
    }

    public void setDoingList( List doingList ) {
        this.doingList = doingList;
    }

    public void setDoingListId(String id) {
        doingList = new List(id);
    }

    public String getDoneListId() {
        return doneList != null ? doneList.getId() : null;
    }

    public void setDoneListId(String id) {
        doneList = new List(id);
    }

    public void setDoneList( List doneList ) {
        this.doneList = doneList;
    }

    public String getTodoListId() {
        return todoList != null ? todoList.getId() : null;
    }

    public void setTodoList( List todoList ) {
        this.todoList = todoList;
    }

    public void setTodoListId( String id ) {
        this.todoList = new List( id );
    }

    public String getShortLink() {
        return shortLink;
    }

    public void setShortLink( String shortLink ) {
        this.shortLink = shortLink;
    }
}
