package com.zode64.trellodoing;

import android.app.DialogFragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.zode64.trellodoing.utils.FileUtils;

import java.io.IOException;

public class AudioPlayerFragment extends DoingFragment {

    private static final String LOG_TAG = AudioPlayerFragment.class.getName();

    private String fileName;

    private ImageButton play;
    private ImageButton stop;
    private ImageButton delete;

    private MediaPlayer player;

    private AudioPlayerListener listener;

    private DialogFragment mThis;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.audio_play, container, false );

        mThis = this;
        fileName = ( ( AudioPlayerListener ) getActivity() ).getAudioFileName();
        play = ( ImageButton ) view.findViewById( R.id.play );
        stop = ( ImageButton ) view.findViewById( R.id.stop );
        delete = ( ImageButton ) view.findViewById( R.id.delete );

        listener = ( AudioPlayerListener ) getActivity();

        startPlaying();

        play.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                startPlaying();
            }
        } );

        stop.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                stopPlaying();
            }
        } );

        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                FileUtils.deleteAudioFile( fileName );
                listener.onDeleteAudio();
                mThis.dismiss();
            }
        } );

        return view;
    }

    @Override
    public void onDestroyView() {
        stopPlaying();
        super.onDestroyView();
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource( fileName );
            player.prepare();
            player.start();
        } catch ( IOException e ) {
            e.printStackTrace();
            Log.e( LOG_TAG, "play failed failed" );
        }
    }

    private void stopPlaying() {
        if ( player != null ) {
            player.stop();
            player.release();
            player = null;
        }
    }

    interface AudioPlayerListener {
        String getAudioFileName();
        void onDeleteAudio();
    }
}