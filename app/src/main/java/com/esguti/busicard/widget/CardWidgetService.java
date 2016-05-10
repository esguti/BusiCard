package com.esguti.busicard.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.esguti.busicard.data.CardLoader;
import com.esguti.busicard.data.CardsDatabase;

import com.esguti.busicard.R;

/**
 * Created by esguti on 09.05.16.
 */
public class CardWidgetService extends RemoteViewsService {
    /**
     * Lock to avoid race condition between widgets.
     */
    private static Object s_WidgetLock = new Object();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CardRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    /**
     * Remote Views Factory for Card Widget.
     */
    private static class CardRemoteViewsFactory
            implements RemoteViewsService.RemoteViewsFactory  {
        private static final String LOG_TAG = CardRemoteViewsFactory.class.getName();
        private static final int MAX_CARDS_COUNT = 25;

        private CardsDatabase m_CardsDatabase;
        private Cursor m_Cursor;

        private final Context m_Context;
        private int m_AppWidgetId;


        public CardRemoteViewsFactory(Context context, Intent intent) {
            m_Context = context;
            m_AppWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        public void onCreate() {
            m_CardsDatabase = new CardsDatabase(m_Context);
            m_Cursor = m_CardsDatabase.getFirst();
        }

        @Override
        public void onDataSetChanged() {
            synchronized (s_WidgetLock) {
                // TODO: use loader manager.
                m_Cursor = m_CardsDatabase.getFirst();
            }
        }

        public void onDestroy() {
            synchronized (s_WidgetLock) {
                if (m_Cursor != null && !m_Cursor.isClosed()) {
                    m_Cursor.close();
                    m_Cursor = null;
                }
            }
        }

        public int getCount() {
            synchronized (s_WidgetLock) {
                return Math.min(m_CardsDatabase.getItemsCount(), MAX_CARDS_COUNT);
            }
        }

        /**
         * @return the {@link RemoteViews} for a specific position in the list.
         */
        public RemoteViews getViewAt(int position) {
            synchronized (s_WidgetLock) {
                RemoteViews rv = new RemoteViews(m_Context.getPackageName(), R.layout.card_widget_item);
                m_Cursor = m_CardsDatabase.getCardCursor(position+1);
                if( m_Cursor != null && !m_Cursor.isAfterLast() ) {
                    String [] cardTextInfo = m_CardsDatabase.getCardText(m_Cursor);
                    String id = cardTextInfo[CardLoader.Query._ID];
                    String company = cardTextInfo[CardLoader.Query.COMPANY];
                    String name = cardTextInfo[CardLoader.Query.NAME];
                    Bitmap thumbnail = m_CardsDatabase.getCardThumbnail(m_Cursor);

                    rv.setImageViewBitmap(R.id.cardWidgetItemPicture, thumbnail);
                    rv.setTextViewText(R.id.cardWidgetItemUsername, name);
                    rv.setTextViewText(R.id.cardWidgetItemContent, company);
                    Log.i(LOG_TAG, "Add element: " + id);
                }else{
                    if( m_Cursor.isAfterLast() ){ m_Cursor = m_CardsDatabase.getFirst(); }
                }
                return rv;
            }
        }

        public RemoteViews getLoadingView() {
            return null;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public long getItemId(int position) {
            return position;
        }

        public boolean hasStableIds() {
            return true;
        }

//        @Override
//        public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
//            if (!data.moveToFirst()) { return; }
//
//            RemoteViews remoteViews = new RemoteViews(m_Context.getPackageName(), R.layout.card_widget);
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(m_Context);
//
//            appWidgetManager.partiallyUpdateAppWidget(m_AppWidgetId, remoteViews);
//        }
    }
}
