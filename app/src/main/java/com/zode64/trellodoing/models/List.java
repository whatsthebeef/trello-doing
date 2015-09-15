package com.zode64.trellodoing.models;

public class List {

    public final static String DOING_LIST = "Doing";

    public final static String TODO_LIST = "Todo";

    public final static String CLOCKED_OFF_LIST = "Clocked Off";

    private String name;

    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDoingList(){
        if(DOING_LIST.equals(name)) {
           return true;
        }
        return false;
    }

    public boolean isClockedOffList(){
        if(CLOCKED_OFF_LIST.equals(name)) {
            return true;
        }
        return false;
    }

    public boolean isTodoList() {
        if(TODO_LIST.equals(name)) {
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
