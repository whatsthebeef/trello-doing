package com.zode64.trellodoing.models;

public class Board {

    private String name;
    private String shortLink;
    private String id;

    private java.util.List<List> lists;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public java.util.List<List> getLists() {
        return lists;
    }

    public void setLists(java.util.List<List> lists) {
        this.lists = lists;
    }

    public String getShortUrl() {
        return "https://trello.com/b/" + shortLink;
    }

    public List getTodolist() {
        for (List list : getLists()) {
            if(list.isTodoList()) {
                return list;
            }
        }
        return null;
    }

    public List doingList() {
        for (List list : getLists()) {
            if(list.isDoingList()) {
                return list;
            }
        }
        return null;
    }

    public List clockedOffList() {
        for (List list : getLists()) {
            if(list.isClockedOffList()) {
                return list;
            }
        }
        return null;
    }

    public boolean isPersonalBoard() {
        if(name == "Personal") {
            return true;
        }
        return false;
    }

    public String getShortLink() {
        return shortLink;
    }

    public void setShortLink(String shortLink) {
        this.shortLink = shortLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
