package com.zode64.trellodoing.models;

import com.zode64.trellodoing.utils.FileUtils;

import java.io.File;

public class Attachment {

    public enum Type {
        AUDIO,
        PHOTO
    }

    protected int id;
    protected String filename;
    protected String cardServerId;
    protected Type type;
    protected boolean isUploaded;

    public Attachment() {
    }

    public Attachment( int id, String filename, String cardId, Type type, boolean isUploaded ) {
        this.id = id;
        this.filename = filename;
        this.cardServerId = cardId;
        this.type = type;
        this.isUploaded = isUploaded;
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        if ( type == Type.AUDIO ) {
            return FileUtils.ROOT_PATH + FileUtils.AUDIO_DIR + cardServerId + "/" + filename;
        } else {
            return FileUtils.ROOT_PATH + FileUtils.IMAGE_DIR + cardServerId + "/" + filename;
        }
    }

    public File getFile() {
        return new File( getPath() );
    }

    public String getCardServerId() {
        return cardServerId;
    }

    public void setCardServerId( String cardId ) {
        this.cardServerId = cardId;
    }

    public void setFilename( String filename ) {
        this.filename = filename;
    }

    public Type getType() {
        return type;
    }

    public int getTypeOrdinal() {
        return type.ordinal();
    }

    public void setType( Type type ) {
        this.type = type;
    }

    public void setType( int type ) {
        this.type = Type.values()[ type ];
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded( boolean isUploaded ) {
        this.isUploaded = isUploaded;
    }
}
