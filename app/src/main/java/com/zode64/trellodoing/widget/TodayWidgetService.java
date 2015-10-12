package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.R;
import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;

import static com.zode64.trellodoing.TimeUtils.after;

public class TodayWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return new TodayListProvider( getApplicationContext(), intent );
    }

    static class TodayListProvider extends ListProvider {

        private DoingPreferences preferences;

        private ArrayList<Board> boards = new ArrayList<>();

        public TodayListProvider( Context context, Intent intent ) {
            super( context, intent );
            preferences = new DoingPreferences( context );
        }

        @Override
        public int getCount() {
            if ( preferences.isBoards() ) {
                return boards.size();
            } else {
                return cards.size();
            }
        }

        @Override
        public RemoteViews getViewAt( int position ) {
            if ( preferences.isBoards() ) {
                Board board = boards.get( position );
                RemoteViews view = new RemoteViews( context.getPackageName(), R.layout.board_card );
                view.setTextViewText( R.id.board_name, context.getString( R.string.get_cards_from )
                        + " " + board.getName() );
                Intent fillInIntent = new Intent();
                fillInIntent.putExtra( DoingWidget.EXTRA_BOARD_ID, board.getId() );
                view.setOnClickFillInIntent( R.id.doing_card, fillInIntent );
                return view;
            } else {
                Card card = cards.get( position );
                if ( after( preferences.getEndHour() ) ) {
                    RemoteViews view = new RemoteViews( context.getPackageName(), R.layout.doing_card_past_today );
                    view.setTextViewText( R.id.card_name, card.getBoardName() + ": " + card.getName() );
                    setClickListener( view, card );
                    if ( card.hasDeadline() ) {
                        view.setViewVisibility( R.id.deadline_set, View.VISIBLE );
                        view.setViewVisibility( R.id.deadline_not_set, View.GONE );
                    } else {
                        view.setViewVisibility( R.id.deadline_set, View.GONE );
                        view.setViewVisibility( R.id.deadline_not_set, View.VISIBLE );
                    }
                    return view;
                } else {
                    return super.getViewAt( position );
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        protected void loadCards() {
            cards = cardDAO.where( Card.ListType.TODAY );
            if ( preferences.isBoards() ) {
                BoardDAO boardDAO = new BoardDAO( context );
                boards = boardDAO.all();
            }
        }
    }
}