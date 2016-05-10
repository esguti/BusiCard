package com.esguti.busicard.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.esguti.busicard.R;


/**
 * Created by esguti on 09.05.16.
 */
public class CardWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = CardWidgetProvider.class.getName();

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int i = 0; i < appWidgetIds.length; ++i) {
            Log.i(LOG_TAG, "Widget OnUpdate called: " + appWidgetIds[i]);

            // set intent for widget service that will create the views
            Intent serviceIntent = new Intent(context, CardWidgetService.class);

            // Add the app widget ID to the intent extras.
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))); // embed extras so they don't get ignored

            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.card_widget);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.cardWidgetView, serviceIntent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            remoteViews.setEmptyView(R.id.cardWidgetView, R.id.cardWidgetEmptyView);

            // update widget
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
