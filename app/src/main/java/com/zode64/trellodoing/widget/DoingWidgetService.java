package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.R;
import com.zode64.trellodoing.models.Card;

import java.util.Calendar;

import static com.zode64.trellodoing.utils.TimeUtils.between;

public class DoingWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return ( new DoingCardsProvider( this.getApplicationContext(), intent ) );
    }

    static class DoingCardsProvider extends ListProvider {

        private DoingPreferences preferences;

        private int mEndHour;
        private int mStartHour;

        public DoingCardsProvider( Context context, Intent intent ) {
            super( context, intent );
            this.preferences = new DoingPreferences( context );
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public RemoteViews getViewAt( int position ) {
            Card card = cards.get( position );
            Calendar now = Calendar.getInstance();
            if ( card.getDummyType() == Card.DummyType.START_DOING ) {
                return new RemoteViews( context.getPackageName(), R.layout.doing_card_start_doing );
            } else if ( !between( mStartHour, mEndHour, now ) ||
                    ( between( mStartHour, mEndHour, now ) && !card.isWorkCard() ) ) {
                RemoteViews view = new RemoteViews( context.getPackageName(), R.layout.doing_card_past_deadline );
                view.setTextViewText( R.id.card_name, card.getBoardName() + ": " + card.getName() );
                setClickListener( view, card );
                return view;
            } else {
                return super.getViewAt( position );
            }
        }

        protected void loadCards() {
            mEndHour = preferences.getEndHour();
            mStartHour = preferences.getStartHour();
            cards = cardDAO.where( Card.ListType.DOING );
            if ( cards.isEmpty() && between( mStartHour, mEndHour, Calendar.getInstance() ) ) {
                Card card = new Card();
                card.setDummyType( Card.DummyType.START_DOING );
                cards.add( card );
            }
        }
    }
}