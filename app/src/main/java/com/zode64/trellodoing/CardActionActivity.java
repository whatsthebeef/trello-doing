package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.db.DeadlineDAO;
import com.zode64.trellodoing.models.Action;
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
        AudioPlayerFragment.AudioPlayerListener, AudioRecorderFragment.AudioInputListener {

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
    private ImageButton playAudio;
    private ImageButton photo;

    private List<File> audioFiles;

    private TrelloManager trello;

    private CardDAO cardDAO;
    private DeadlineDAO deadlineDAO;

    private Long existingDeadline;

    private Card card;

    private WidgetAlarm alarm;

    private Activity activity;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );

        DoingPreferences preferences = new DoingPreferences( this );

        if ( preferences.isBoards() ) {
            setContentView( R.layout.dialog );
            getFragmentManager().beginTransaction().replace( R.id.dialog, new BoardActionFragment() ).commit();
            super.onCreate( savedInstanceState );
            return;
        }
        setContentView( R.layout.card_action );

        alarm = new WidgetAlarm( this, new DoingPreferences( this ) );

        String cardId = getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );
        cardDAO = new CardDAO( this );
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
        playAudio = ( ImageButton ) findViewById( R.id.play_audio );
        setDeadline = ( ImageButton ) findViewById( R.id.set_deadline );
        photo = ( ImageButton ) findViewById( R.id.photo );

        deadlineText = ( TextView ) findViewById( R.id.deadline_text );
        noDoneListText = ( TextView ) findViewById( R.id.no_done_list );
        noDoingListText = ( TextView ) findViewById( R.id.no_doing_list );
        noClockedOffListText = ( TextView ) findViewById( R.id.no_clocked_off_list );
        noTodayListText = ( TextView ) findViewById( R.id.no_today_list );
        noTodoListText = ( TextView ) findViewById( R.id.no_todo_list );

        switch (card.getInListType()  ) {
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

        deadlineDAO = new DeadlineDAO( this );
        existingDeadline = deadlineDAO.find( card.getServerId() );
        if ( existingDeadline != null ) {
            deadlineText.setVisibility( View.VISIBLE );
            deadlineText.setText( getResources().getString( R.string.deadline_set_for )
                    + " " + TimeUtils.format( new Date( existingDeadline ) ) );
        }

        updateAudioOptions();

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

        trello = new TrelloManager( preferences.getSharedPreferences() );

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

        playAudio.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new AudioPlayerFragment().show( getFragmentManager(), null );
            }
        } );

        photo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //create new Intent
                Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
                String fileUri = FileUtils.prepareImageFile( card.getServerId() );
                intent.putExtra( MediaStore.EXTRA_OUTPUT, fileUri );
                intent.putExtra( MediaStore.EXTRA_VIDEO_QUALITY, 1 );
                // start the Video Capture Intent
                startActivityForResult( intent, 1 );
            }
        } );
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
        deadlineDAO.create( card.getServerId(), deadline.getTimeInMillis() );
        startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
        finish();
    }

    @Override
    public String getAudioFileName() {
        return audioFiles.get( 0 ).getPath();
    }

    @Override
    public void onSaveAudio( String path ) {
        updateAudioOptions();
    }

    @Override
    public void onDeleteAudio() {
        updateAudioOptions();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if ( resultCode == RESULT_OK ) {
            // Image captured and saved to fileUri specified in the Intent
            Toast.makeText( this, "Image saved to:\n" +
                    data.getData(), Toast.LENGTH_LONG ).show();
        } else if ( resultCode == RESULT_CANCELED ) {
            // User cancelled the image capture
        } else {
            // Image capture failed, advise user
        }
    }

    private void updateAudioOptions() {
        audioFiles = FileUtils.getAudioFiles( card.getServerId() );
        if ( audioFiles != null && !audioFiles.isEmpty() ) {
            recordAudio.setVisibility( View.GONE );
            playAudio.setVisibility( View.VISIBLE );
        } else {
            recordAudio.setVisibility( View.VISIBLE );
            playAudio.setVisibility( View.GONE );
        }
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
                    getActionDAO().createUpdate( card );
                }
            } else {
                getActionDAO().getCardDAO().updateName( card.getId(), card.getName() );
            }
            return null;
        }
    }

}
