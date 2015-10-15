package com.zode64.trellodoing.utils;

import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FileUtils {

    private final static String AUDIO_DIR = "/doing/audio/";
    private final static String IMAGE_DIR = "/doing/images/";

    private final static String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private final static String AUDIO_FILE_EXTENSION = ".mpeg4";
    private final static String IMAGE_FILE_EXTENSION = ".jpg";

    public static String prepareAudioFile( String cardId ) {
        return prepareFile( AUDIO_DIR, cardId, AUDIO_FILE_EXTENSION );
    }

    public static String prepareImageFile( String cardId ) {
        String uri = prepareFile( IMAGE_DIR, cardId, IMAGE_FILE_EXTENSION );
        File file = new File( uri );
        /*
        try {
            file.createNewFile();
        } catch ( IOException e ) {
            throw new RuntimeException( "Can't create image file" );
        }
        */
        return Uri.fromFile( file ).toString();
    }

    public static List<File> getAudioFiles( String cardId ) {
        File dir = new File( ROOT_PATH + AUDIO_DIR + cardId );
        if ( dir != null && dir.isDirectory() ) {
            return Arrays.asList( dir.listFiles() );
        }
        return null;
    }

    public static void deleteAudioFile( String fileName ) {
        new File( fileName ).delete();
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

    private static String prepareFile( String path, String cardId, String extension ) {
        String fileDir = ROOT_PATH + path + cardId;
        File dir = new File( fileDir );
        dir.mkdirs();
        if ( !dir.isDirectory() ) {
            throw new RuntimeException( "Can't create dir at : " + fileDir );
        }
        return fileDir + "/" + Calendar.getInstance().getTimeInMillis() + extension;
    }

}
