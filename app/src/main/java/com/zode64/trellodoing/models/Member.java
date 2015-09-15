package com.zode64.trellodoing.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by john on 2/3/15.
 */
public class Member {
    private java.util.List<Action> actions;
    private java.util.List<Board> boards;

    public java.util.List<Action> getActions() {
        return actions;
    }

    public void setActions( java.util.List<Action> actions ) {
        this.actions = actions;
    }

    public java.util.List<Board> getBoards() {
        return boards;
    }

    public List getNextPersonalTodoCard() {
        for ( Board board : boards ) {
            if ( board.isPersonalBoard() ) {
                return board.getTodolist();
            }
        }
        throw new RuntimeException( "No 'Personal' board exists for this user" );
    }

    public void setBoards( java.util.List<Board> boards ) {
        this.boards = boards;
    }

    public List getClockedOffList( String boardId ) {
        for ( Board board : getBoards() ) {
            if ( boardId.equals( board.getId() ) ) {
                return board.clockedOffList();
            }
        }
        return null;
    }

    private java.util.List<Action> findDoingActions(boolean onlyPersonal, boolean include) {
        Map<String, Boolean> shifts = new HashMap<String, Boolean>();
        // Cycles from most recent to least recent
        java.util.List<Action> doingActions = new ArrayList<Action>();
        for ( Action action : actions ) {
            if(action.isWorkAction()) {
                if ( action.isStoppedDoingAction() ) {
                    shifts.put( action.getCard().getId(), true );
                } else if ( action.isDoingAction() ) {
                    if ( shifts.containsKey( action.getCard().getId() ) ) {
                        shifts.remove( action.getCard().getId() );
                    } else {
                        doingActions.add( action );
                    }
                }
            }
        }
        return doingActions;
    }

    private Card findPersonalDoingActions() {

        return null;
    }
}
