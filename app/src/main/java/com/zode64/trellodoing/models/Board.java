package com.zode64.trellodoing.models;

import java.util.HashMap;
import java.util.Map;

public class Board {


    public enum ListType {
        UNKNOWN,
        TODO,
        THIS_WEEK,
        TODAY,
        DOING,
        CLOCKED_OFF,
        DONE,
        NOT_CHARGING,
        NONE
    }

    private String name;
    private String id;
    private String shortLink;
    private String idOrganization;

    private HashMap<String, ListType> listMap = new HashMap<>();

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

    public void addList( String listId, ListType listType ) {
        listMap.put( listId, listType );
    }

    public ListType getListType( String listId ) {
        return listMap.get( listId );
    }

    public String getListId( ListType listType ) {
        for ( Map.Entry<String, ListType> entry : listMap.entrySet() ) {
            if(listType == entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getShortLink() {
        return shortLink;
    }

    public void setShortLink( String shortLink ) {
        this.shortLink = shortLink;
    }

    public String getIdOrganization() {
        return idOrganization;
    }

    public void setIdOrganization( String idOrganization ) {
        this.idOrganization = idOrganization;
    }

    public boolean isWorkBoard() {
        return idOrganization != null && !idOrganization.equals( "" );
    }

    public boolean isDoingList( String listId ) {
        return listMap.get( listId ) == ListType.DOING;
    }
}
