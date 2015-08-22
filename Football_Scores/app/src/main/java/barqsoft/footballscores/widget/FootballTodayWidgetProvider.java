package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import barqsoft.footballscores.service.FootballFetchService;

/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       FootballTodayWidgetProvider.java
 *
 *       Football today widget provider
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *     Original version Sunshine (https://github.com/udacity/Advanced_Android_Development/tree/7.05_Pretty_Wallpaper_Time)
 **/
public class FootballTodayWidgetProvider extends AppWidgetProvider {

    private static final String LOG_TAG = "WidgetProvider:hangulo";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, FootballTodayWidgetIntentService.class);
        ComponentName ret = context.startService(intent);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        Log.v(LOG_TAG, "onAppWidgetOptionsChanged");
        context.startService(new Intent(context, FootballTodayWidgetIntentService.class));

    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {

        super.onReceive(context, intent);
        int dataPos=0;
        int myWidgetId= AppWidgetManager.INVALID_APPWIDGET_ID;


        String myAction = intent.getAction();
        Log.v(LOG_TAG, "onReceive:" + myAction);

        switch (myAction) {
            case FootballWidget.ACTION_DATA_UPDATED: // when app is launched then all widget updated(reset position)
                context.startService(new Intent(context, FootballTodayWidgetIntentService.class));
                break;
            case FootballWidget.ACTION_NEXT_PRESSED: // 이거 하나로 합치자
            case FootballWidget.ACTION_PREV_PRESSED:
               Bundle extras = intent.getExtras();
                if (extras != null) {
                    dataPos = extras.getInt(FootballWidget.DATA_POS, 0);
                    myWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }

                Intent intent_next = new Intent(context, FootballTodayWidgetIntentService.class);
                if (myWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    intent_next.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myWidgetId);
                }
                intent_next.putExtra(FootballWidget.DATA_POS, dataPos);
                context.startService(intent_next); // show next match

                break;

        }
    }

    // refresh (FootballFetchService call)
    void refreshData(Context context) {
            Intent service_start = new Intent(context, FootballFetchService.class);
            context.startService(service_start);

    }



}
