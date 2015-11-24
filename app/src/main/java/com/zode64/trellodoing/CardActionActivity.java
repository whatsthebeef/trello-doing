package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.zode64.trellodoing.db.AttachmentDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.db.DeadlineDAO;
import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Attachment;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Card.ListType;
import com.zode64.trellodoing.utils.FileUtils;
import com.zode64.trellodoing.utils.TimeUtils;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.utils.WidgetAlarm;
import com.zode64.trellodoing.widget.DoingWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CardActionActivity extends Activity implements ConfigureDelayFragment.DelayChangeListener,
        ConfirmationFragment.ConfirmationListener, InputFragment.TextChangeListener,
        AudioPlayerFragment.AudioPlayerListener, AudioRecorderFragment.AudioInputListener, PhotoShowerFragment.PhotoShowerListener {

    private TextView cardText;

    private TextView deadlineText;
    private TextView noTodayListText;
    private TextView noDoingListText;
    private TextView noDoneListText;
    private TextView noClockedOffListText;
    private TextView noTodoListText;

    private ImageButton clockOff;
    private ImageButton clockOn;
    private ImageButton todo;
    private ImageButton done;
    private ImageButton today;
    private ImageButton delete;
    private ImageButton open;
    private ImageButton cancel;

    private ImageButton setDeadline;
    private ImageButton recordAudio;
    private ImageButton photo;

    private ListView audios;
    private AudiosAdapter audiosAdapter;
    private ArrayList<File> audioFiles;
    private File selectedAudioFile;

    private File newImageFile;
    private ListView photos;
    private PhotosAdapter photosAdapter;
    private ArrayList<File> photoFiles;
    private File selectedPhotoFile;

    private TrelloManager trello;

    private CardDAO cardDAO;
    private DeadlineDAO deadlineDAO;
    private AttachmentDAO attachmentDAO;

    private Long existingDeadline;

    private Card card;

    private WidgetAlarm alarm;

    private Activity activity;

    private DoingPreferences preferences;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );

        String cardId = getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );

        deadlineDAO = new DeadlineDAO( this );
        cardDAO = new CardDAO( this );
        attachmentDAO = new AttachmentDAO( this );

        if ( cardId == null ) {
            setContentView( R.layout.dialog );
            getFragmentManager().beginTransaction().replace( R.id.dialog, new BoardActionFragment() ).commit();
            super.onCreate( savedInstanceState );
            return;
        }

        setContentView( R.layout.card_action );

        alarm = new WidgetAlarm( this, new DoingPreferences( this ) );

        card = cardDAO.findById( cardId );
        activity = this;

        cardText = ( TextView ) findViewById( R.id.card_name );

        clockOff = ( ImageButton ) findViewById( R.id.clock_out );
        clockOn = ( ImageButton ) findViewById( R.id.clock_on );
        done = ( ImageButton ) findViewById( R.id.done );
        today = ( ImageButton ) findViewById( R.id.today );
        todo = ( ImageButton ) findViewById( R.id.to_todo );
        delete = ( ImageButton ) findViewById( R.id.delete );
        open = ( ImageButton ) findViewById( R.id.open );
        cancel = ( ImageButton ) findViewById( R.id.cancel );

        recordAudio = ( ImageButton ) findViewById( R.id.record_audio );
        setDeadline = ( ImageButton ) findViewById( R.id.set_deadline );
        photo = ( ImageButton ) findViewById( R.id.photo );

        deadlineText = ( TextView ) findViewById( R.id.deadline_text );
        noDoneListText = ( TextView ) findViewById( R.id.no_done_list );
        noDoingListText = ( TextView ) findViewById( R.id.no_doing_list );
        noClockedOffListText = ( TextView ) findViewById( R.id.no_clocked_off_list );
        noTodayListText = ( TextView ) findViewById( R.id.no_today_list );
        noTodoListText = ( TextView ) findViewById( R.id.no_todo_list );

        switch ( card.getInListType() ) {
            case DOING:
                clockOff.setVisibility( View.VISIBLE );
                done.setVisibility( View.VISIBLE );
                break;
            case TODAY:
                clockOn.setVisibility( View.VISIBLE );
                todo.setVisibility( View.VISIBLE );
                break;
            case CLOCKED_OFF:
                clockOn.setVisibility( View.VISIBLE );
                today.setVisibility( View.VISIBLE );
                break;
            default:
                throw new RuntimeException( "Weird card list type in card action view" );
        }

        existingDeadline = deadlineDAO.find( card.getServerId() );
        if ( existingDeadline != null ) {
            deadlineText.setVisibility( View.VISIBLE );
            deadlineText.setText( getResources().getString( R.string.deadline_set_for )
                    + " " + TimeUtils.format( new Date( existingDeadline ) ) );
        }

        audioFiles = FileUtils.getAudioFiles( card.getServerId() );
        audios = ( ListView ) findViewById( R.id.audios );
        audiosAdapter = new AudiosAdapter( this, audioFiles );
        audios.setAdapter( audiosAdapter );
        audios.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                new AudioPlayerFragment().show( getFragmentManager(), null );
                selectedAudioFile = audioFiles.get( position );
            }
        } );

        photoFiles = new ArrayList<>();
        photos = ( ListView ) findViewById( R.id.photos );
        photosAdapter = new PhotosAdapter( this, photoFiles );
        photos.setAdapter( photosAdapter );
        photos.setOnItemClickListener( new OnItemClickListener() {

            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                /*
                new PhotoShowerFragment().show( getFragmentManager(), null );
                */
                selectedPhotoFile = photoFiles.get( position );
                Intent intent = new Intent( Intent.ACTION_VIEW );
                intent.setDataAndType( Uri.fromFile( selectedPhotoFile ), "image/*" );
                startActivity( intent );
            }
        } );

        if ( card.getListId( ListType.DOING ) == null ) {
            noDoingListText.setVisibility( View.VISIBLE );
            clockOn.setEnabled( false );
        }

        if ( card.getListId( ListType.DONE ) == null ) {
            noDoneListText.setVisibility( View.VISIBLE );
            done.setEnabled( false );
        }

        if ( card.getListId( ListType.TODAY ) == null ) {
            noTodayListText.setVisibility( View.VISIBLE );
            today.setEnabled( false );
        }

        if ( card.getListId( ListType.CLOCKED_OFF ) == null ) {
            noClockedOffListText.setVisibility( View.VISIBLE );
            clockOff.setEnabled( false );
        }

        if ( card.getListId( ListType.TODO ) == null ) {
            noTodoListText.setVisibility( View.VISIBLE );
            todo.setEnabled( false );
        }

        preferences = new DoingPreferences( this );
        trello = new TrelloManager( preferences );

        cardText.setText( card.getName() );

        cardText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new InputFragment().show( getFragmentManager(), null );
            }
        } );

        clockOff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOffTask( activity, trello ).execute( card );
            }
        } );

        clockOn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOnTask( activity, trello ).execute( card );
            }
        } );

        done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new DoneTask( activity, trello ).execute( card );
            }
        } );

        todo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ToTodoTask( activity, trello ).execute( card );
            }
        } );

        setDeadline.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ConfigureDelayFragment().show( getFragmentManager(), null );
            }
        } );

        today.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new TodayTask( activity, trello ).execute( card );
            }
        } );

        open.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent getBoard = new Intent( Intent.ACTION_VIEW, Uri.parse( card.getShortUrl() ) );
                startActivity( getBoard );
            }
        } );

        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ConfirmationFragment().show( getFragmentManager(), null );
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                activity.onBackPressed();
            }
        } );

        recordAudio.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new AudioRecorderFragment().show( getFragmentManager(), null );
            }
        } );

        photo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //create new Intent
                Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
                newImageFile = FileUtils.prepareImageFile( card.getServerId() );
                intent.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( newImageFile ) );
                // start the Video Capture Intent
                startActivityForResult( intent, 0 );
            }
        } );
    }

    @Override
    public void onDestroy() {
        deadlineDAO.closeDB();
        attachmentDAO.closeDB();
        cardDAO.closeDB();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( card != null ) {
            photoFiles = FileUtils.getPhotoFiles( card.getServerId() );
            photosAdapter.clear();
            photosAdapter.addAll( photoFiles );
        }
    }

    @Override
    public Card getCard() {
        return card;
    }

    @Override
    public void onConfirmation() {
        new DeleteTask( activity, trello ).execute( card );
    }

    @Override
    public String getConfirmationInstruction() {
        return getString( R.string.confirm ) + " '" + card.getName() + "' " + getString( R.string.delete );
    }

    @Override
    public void onTextChange( String text ) {
        new UpdateCardNameTask( activity, trello ).execute( card );
        card.setName( text );
    }

    @Override
    public Long getExistingDelay() {
        return existingDeadline;
    }

    @Override
    public void resetDelay() {
        deadlineDAO.delete( card.getServerId() );
        startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
    }

    @Override
    public void onDelayChange( Double delay ) {
        Calendar deadline = alarm.deadlineAlarm( delay );
        deadlineDAO.delete( card.getServerId() );
        deadlineDAO.create( card.getServerId(), deadline.getTimeInMillis() );
        startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
        finish();
    }

    @Override
    public File getAudioFileName() {
        return selectedAudioFile;
    }

    @Override
    public void onSaveAudio( File file ) {
        audiosAdapter.add( file );
        attachmentDAO = new AttachmentDAO( this );
        Attachment attachment = new Attachment();
        attachment.setFilename( file.getName() );
        attachment.setType( Attachment.Type.AUDIO );
        attachment.setCardServerId( card.getServerId() );
        attachment.setUploaded( false );
        attachmentDAO.create( attachment );
        startService( new Intent( DoingWidget.ACTION_UPLOAD_ATTACHMENTS ) );
    }

    @Override
    public void onDeleteAudio( File file ) {
        audiosAdapter.remove( file );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( resultCode == RESULT_OK ) {
            attachmentDAO = new AttachmentDAO( this );
            Attachment attachment = new Attachment();
            attachment.setFilename( newImageFile.getName() );
            attachment.setType( Attachment.Type.PHOTO );
            attachment.setCardServerId( card.getServerId() );
            attachment.setUploaded( false );
            attachmentDAO.create( attachment );
            startService( new Intent( DoingWidget.ACTION_UPLOAD_ATTACHMENTS ) );
        }
    }

    @Override
    public File getPhotoFile() {
        return selectedPhotoFile;
    }

    @Override
    public void onDeletePhoto( File file ) {
        photosAdapter.remove( file );
    }

    private class ClockOffTask extends TrelloTask {
        public ClockOffTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().clockOff( card[ 0 ] ) ) {
                getActionDAO().setClockedOff( card[ 0 ] );
            }
            return null;
        }
    }

    private class ClockOnTask extends TrelloTask {
        public ClockOnTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().clockOn( card[ 0 ] ) ) {
                getActionDAO().setClockedOn( card[ 0 ] );
            }
            return null;
        }
    }

    private class DoneTask extends TrelloTask {

        public DoneTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().done( card[ 0 ] ) ) {
                getActionDAO().setDone( card[ 0 ] );
            }
            return null;
        }
    }

    private class TodayTask extends TrelloTask {

        public TodayTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().today( card[ 0 ] ) ) {
                getActionDAO().setToday( card[ 0 ] );
            }
            return null;
        }
    }

    private class ToTodoTask extends TrelloTask {

        public ToTodoTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().todo( card[ 0 ] ) ) {
                getActionDAO().setTodo( card[ 0 ] );
            }
            return null;
        }
    }

    private class DeleteTask extends TrelloTask {

        public DeleteTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... cards ) {
            Card card = cards[ 0 ];
            ArrayList<Action> createActions = getActionDAO().find( card.getId(), Action.Type.CREATE );
            if ( !createActions.isEmpty() ) {
                getActionDAO().delete( card.getId() );
                getActionDAO().getCardDAO().delete( card.getId() );
                return null;
            } else {
                if ( !getTrello().deleteCard( card.getServerId() ) ) {
                    getActionDAO().createDelete( card );
                    getActionDAO().getCardDAO().markForDelete( card.getId() );
                }
            }
            return null;
        }
    }

    private class UpdateCardNameTask extends TrelloTask {

        public UpdateCardNameTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... cards ) {
            Card card = cards[ 0 ];
            List<Action> createActions = getActionDAO().find( card.getId(), Action.Type.CREATE );
            if ( createActions.isEmpty() ) {
                if ( !getTrello().updateCardName( card.getServerId(), card.getName() ) ) {
                    getActionDAO().getCardDAO().updateName( card.getId(), card.getName() );
                    getActionDAO().createUpdate( card );
                }
            } else {
                getActionDAO().getCardDAO().updateName( card.getId(), card.getName() );
            }
            return null;
        }
    }

    static class AudiosAdapter extends ArrayAdapter<File> {

        private LayoutInflater inflator;

        public AudiosAdapter( Context context, List<File> files ) {
            super( context, R.layout.audio_list_item, files );
            inflator = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            View row = null;
            if ( convertView == null ) {
                row = inflator.inflate( R.layout.audio_list_item, null );
            } else {
                row = convertView;
            }
            TextView fileNameView = ( TextView ) row.findViewById( R.id.filename );
            fileNameView.setText( getItem( position ).getName() );
            return row;
        }

    }

    static class PhotosAdapter extends ArrayAdapter<File> {

        private LayoutInflater inflator;

        public PhotosAdapter( Context context, List<File> files ) {
            super( context, R.layout.audio_list_item, files );
            inflator = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            View row = null;
            if ( convertView == null ) {
                row = inflator.inflate( R.layout.audio_list_item, null );
            } else {
                row = convertView;
            }
            TextView fileNameView = ( TextView ) row.findViewById( R.id.filename );
            fileNameView.setText( getItem( position ).getName() );
            return row;
        }

    }
}
