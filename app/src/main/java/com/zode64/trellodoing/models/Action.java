package com.zode64.trellodoing.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by john on 2/3/15.
 */
public class Action implements Parcelable {

    private static final String TAG = Action.class.getName();

    public enum Status {
        BOTH( 0 ),
        WORK( 1 ),
        PERSONAL( 2 ),
        NONE( 3 );

        private int mInt;

        private Status( int aInt ) {
            mInt = aInt;
        }

        public int getInt() {
            return mInt;
        }

        public String getIntString() {
            return String.valueOf( mInt );
        }

        public static Status getStatus( int status ) {
            for ( Status s : values() ) {
                if ( s.getInt() == status ) {
                    return s;
                }
            }
            return NONE;
        }

        public static Status getStatus( String status ) {
            return getStatus( Integer.parseInt( status ) );
        }

    }

    public enum Type {
        DOING( 0 ),
        WAS_DOING( 1 ),
        OTHER( 2 );

        private int mInt;

        private Type( int aInt ) {
            mInt = aInt;
        }

        public int getInt() {
            return mInt;
        }

        public String getIntString() {
            return String.valueOf( mInt );
        }

        public static Type getType( int status ) {
            for ( Type t : values() ) {
                if ( t.getInt() == status ) {
                    return t;
                }
            }
            return OTHER;
        }

        public static Type getType( String type ) {
            return getType( Integer.parseInt( type ) );
        }
    }

    private String boardShortLink;
    private String boardName;
    private String boardId;
    private Status status;
    private Type type;
    private String cardName;
    private String cardId;
    private String id;

    public Action() {
    }

    public Action( Parcel in ) {
        String[] data = new String[ 5 ];

        in.readStringArray( data );
        this.cardId = data[ 0 ];
        this.cardName = data[ 1 ];
        this.boardShortLink = data[ 2 ];
        this.type = Type.getType( data[ 3 ] );
        this.status = Status.getStatus( data[ 4 ] );
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel( Parcel dest, int flags ) {
        dest.writeStringArray(new String[] {this.cardId,
                this.cardId,
                this.cardName,
                this.boardShortLink,
                this.type.getIntString(),
                this.status.getIntString()
        });
    }

    public static final Parcelable.Creator<Action> CREATOR
            = new Parcelable.Creator<Action>() {
        public Action createFromParcel( Parcel in ) {
            return new Action( in );
        }

        public Action[] newArray( int size ) {
            return new Action[ size ];
        }
    };

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public boolean isWorkAction() {
        return status == Status.WORK;
    }

    public boolean isPersonalAction() {
        return status == Status.PERSONAL;
    }

    public boolean isStoppedDoingAction() {
        return type == Type.WAS_DOING;
    }

    public boolean isDoingAction() {
        return type == Type.DOING;
    }

    public String getBoardShortUrl() {
        return "https://trello.com/b/" + boardShortLink;
    }

    public void setBoardShortLink( String boardShortLink ) {
        this.boardShortLink = boardShortLink;
    }

    public String getBoardShortLink() {
        return boardShortLink;
    }

    public void setBoardName( String boardName ) {
        this.boardName = boardName;
    }

    public String getBoardName() {
        return this.boardName;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId( String boardId ) {
        this.boardId = boardId;
    }

    public Type getType() {
        return type;
    }

    public void setType( Type type ) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus( Status status ) {
        this.status = status;
    }

    public void setCardName( String cardName ) {
        this.cardName = cardName;
    }

    public String getCardName() {
        return this.cardName;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId( String cardId ) {
        this.cardId = cardId;
    }

}
