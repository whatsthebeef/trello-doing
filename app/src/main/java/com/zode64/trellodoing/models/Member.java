package com.zode64.trellodoing.models;

/**
 * Created by john on 2/3/15.
 */
public class Member {
    private java.util.List<Action> actions;
    private java.util.List<Board> boards;

    public java.util.List<Action> getActions() {
        return actions;
    }

    public void setActions(java.util.List<Action> actions) {
        this.actions = actions;
    }

    public java.util.List<Board> getBoards() {
        return boards;
    }

    public void setBoards(java.util.List<Board> boards) {
        this.boards = boards;
    }

    public List getClockedOffList(String boardId) {
        for(Board board : getBoards()) {
            if(boardId.equals(board.getId())) {
                return board.clockedOffList();
            }
        }
        return null;
    }
}
