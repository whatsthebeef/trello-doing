package com.zode64.trellodoing.models;

public class List {

    private String id;

    private Board.ListType listType;

    public void setListType( Board.ListType listType ) {
        this.listType = listType;
    }

    public Board.ListType getListType() {
        return listType;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

}
