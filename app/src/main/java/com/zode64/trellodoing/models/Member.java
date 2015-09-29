package com.zode64.trellodoing.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by john on 2/3/15.
 */
public class Member {

    private static final String TAG = Member.class.getName();

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

    public List getPersonalDoingList() {
        for ( Board board : getBoards() ) {
            if ( board.isPersonalBoard() ) {
                return board.doingList();
            }
        }
        return null;
    }

    public List getPersonalTodoList() {
        for ( Board board : getBoards() ) {
            if ( board.isPersonalBoard() ) {
                return board.todolist();
            }
        }
        return null;
    }

    public List getDoneList( String boardId ) {
        for ( Board board : getBoards() ) {
            if ( boardId.equals( board.getId() ) ) {
                return board.doneList();
            }
        }
        return null;
    }

    public Map<Action.Status, java.util.List<Action>> findDoingActions() {
        Map<String, Boolean> shifts = new HashMap<String, Boolean>();
        // Cycles from most recent to least recent
        Map<Action.Status, java.util.List<Action>> doingActions = new HashMap<>();
        doingActions.put( Action.Status.WORK, new ArrayList<Action>() );
        doingActions.put( Action.Status.PERSONAL, new ArrayList<Action>() );
        for ( Action action : actions ) {
            if ( action.isStoppedDoingAction() ) {
                shifts.put( action.getCardId(), true );
            } else if ( action.isDoingAction() ) {
                if ( shifts.containsKey( action.getCardId() ) ) {
                    shifts.remove( action.getCardId() );
                } else {
                    if ( action.isWorkAction() ) {
                        doingActions.get( Action.Status.WORK ).add( action );
                    } else {
                        doingActions.get( Action.Status.PERSONAL ).add( action );
                    }
                }
            }
        }
        return doingActions;
    }

}
