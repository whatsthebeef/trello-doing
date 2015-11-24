package com.zode64.trellodoing;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PhotoShowerFragment extends DoingFragment {

    private static final String LOG_TAG = PhotoShowerFragment.class.getName();

    private File photoFile;

    private ImageButton delete;
    private ImageButton cancel;

    private ImageView photo;

    private PhotoShowerListener listener;

    private DialogFragment mThis;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.photo_show, container, false );

        mThis = this;
        listener = ( PhotoShowerListener ) getActivity();
        photoFile = listener.getPhotoFile();

        photo = ( ImageView ) view.findViewById( R.id.photo );
        delete = ( ImageButton ) view.findViewById( R.id.delete );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize( size );
        int width = size.x;
        int height = size.y;

        photo.setImageBitmap( decodeFile( photoFile, width ) );

        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                photoFile.delete();
                listener.onDeletePhoto( photoFile );
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
        super.onDestroyView();
    }

    private Bitmap decodeFile( File f, int size ) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream( new FileInputStream( f ), null, o );

            int scale = 1;
            while ( o.outWidth / scale / 2 >= size &&
                    o.outHeight / scale / 2 >= size ) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream( new FileInputStream( f ), null, o2 );
        } catch ( FileNotFoundException e ) {
        }
        return null;
    }

    interface PhotoShowerListener {
        File getPhotoFile();

        void onDeletePhoto( File file );
    }
}