package com.zode64.trellodoing.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class FileUtils {

    public final static String AUDIO_DIR = "/doing/audio/";
    public final static String IMAGE_DIR = "/doing/images/";

    public final static String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    public final static String AUDIO_FILE_EXTENSION = ".mpeg4";
    public final static String IMAGE_FILE_EXTENSION = ".jpg";

    public static File prepareAudioFile( String cardId ) {
        return prepareFile( AUDIO_DIR, cardId, AUDIO_FILE_EXTENSION );
    }

    public static File prepareImageFile( String cardId ) {
        File file = prepareFile( IMAGE_DIR, cardId, IMAGE_FILE_EXTENSION );
        try {
            file.createNewFile();
        } catch ( IOException e ) {
            throw new RuntimeException( "Can't create image file" );
        }
        return file;
    }

    public static ArrayList<File> getAudioFiles( String cardId ) {
        return getFiles( cardId, AUDIO_DIR );
    }

    public static ArrayList<File> getPhotoFiles( String cardId ) {
        return getFiles( cardId, IMAGE_DIR );
    }

    public static ArrayList<File> getFiles( String cardId, String path ) {
        File dir = new File( ROOT_PATH + path + cardId );
        ArrayList<File> audioFiles = new ArrayList<>();
        if ( dir != null && dir.isDirectory() ) {
            Collections.addAll( audioFiles, dir.listFiles() );
        }
        return audioFiles;
    }

    public static boolean renameAudioDir( String oldServerId, String newServerId ) {
        return renameDir( oldServerId, newServerId, AUDIO_DIR );
    }

    public static boolean renameImageDir( String oldServerId, String newServerId ) {
        return renameDir( oldServerId, newServerId, IMAGE_DIR );
    }

    public static boolean renameDir( String oldServerId, String newServerId, String path ) {
        File oldDir = new File( ROOT_PATH + path + oldServerId );
        if ( oldDir.isDirectory() ) {
            File newDir = new File( ROOT_PATH + path + newServerId );
            return oldDir.renameTo( newDir );
        }
        return false;
    }

    private static File prepareFile( String path, String cardId, String extension ) {
        String fileDir = ROOT_PATH + path + cardId;
        File dir = new File( fileDir );
        dir.mkdirs();
        if ( !dir.isDirectory() ) {
            throw new RuntimeException( "Can't create dir at : " + fileDir );
        }
        return new File( fileDir + "/" + Calendar.getInstance().getTimeInMillis() + extension );
    }

}
