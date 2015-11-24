package com.zode64.trellodoing;

import android.app.DialogFragment;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.zode64.trellodoing.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class AudioRecorderFragment extends DoingFragment {

    private static final String LOG_TAG = AudioRecorderFragment.class.getName();

    private File file;

    private ImageButton save;
    private ImageButton cancel;

    private MediaRecorder recorder;
    private AudioInputListener listener;

    private DialogFragment mThis;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        listener = ( ( AudioInputListener ) getActivity() );

        View view = inflater.inflate( R.layout.audio_input, container, false );

        save = ( ImageButton ) view.findViewById( R.id.save );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );

        mThis = this;

        file = FileUtils.prepareAudioFile( listener.getCard().getServerId() );

        startRecording();

        save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                stopRecording();
                listener.onSaveAudio( file );
                mThis.dismiss();
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                mThis.dismiss();
            }
        } );

        return view;
    }

    @Override
    public void onDestroyView() {
        stopRecording();
        super.onDestroyView();
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource( MediaRecorder.AudioSource.MIC );
        recorder.setOutputFormat( MediaRecorder.OutputFormat.MPEG_4 );
        recorder.setOutputFile( file.getPath() );
        recorder.setAudioEncoder( MediaRecorder.AudioEncoder.DEFAULT );
        try {
            recorder.prepare();
            recorder.start();
        } catch ( IOException e ) {
            e.printStackTrace();
            Log.e( LOG_TAG, "prepare() failed" );
        }
    }

    private void stopRecording() {
        if ( recorder != null ) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    interface AudioInputListener extends CardGetter {
        void onSaveAudio( File audioFile );
    }
}