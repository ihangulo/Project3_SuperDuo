/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.data.DatabaseContract;

/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       FootballTodayWidgetIntentService.java
 *
 *       Football Today Widget provider
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *     Original version Sunshine (https://github.com/udacity/Advanced_Android_Development/tree/7.05_Pretty_Wallpaper_Time)
 **/
public class FootballTodayWidgetIntentService extends IntentService {

    private static final String LOG_TAG = "WidgetService:hangulo";

    private static final String[] FOOTBALL_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_DATE_MS
    };

    // these indices must match the projection

    private static final int INDEX_MATCH_ID = 0;
    private static final int INDEX_LEAGUE_COL = 1;
    private static final int INDEX_DATE_COL = 2;
    private static final int INDEX_TIME_COL = 3;
    private static final int INDEX_HOME_COL = 4;
    private static final int INDEX_AWAY_COL = 5;
    private static final int INDEX_HOME_GOALS_COL = 6;
    private static final int INDEX_AWAY_GOALS_COL = 7;
    private static final int INDEX_MATCH_DATE_MS=8;


    public FootballTodayWidgetIntentService() {
        super("FootballTodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long matchId = 0;
        String matchDate = "";
        String matchTime = "";
        String matchHomeName = "";
        String matchAwayName = "";
        int matchHomeGoals = 0;
        int matchAwayGoals = 0;
        Cursor data;
        int nextDataPos = 0;
        int prevDataPos = 0;
        int[] appWidgetIds;
        long matchDateMs = 0;
        String dayName;

        int myWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        int dataPos = 0;

//        Log.v(LOG_TAG, "onHandelIntent");


        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            myWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            dataPos = extras.getInt(FootballWidget.DATA_POS, 0); // get current datapos
        }

        if (myWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { // if there is no myWidgetId
            appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                    FootballTodayWidgetProvider.class));
        } else {
            appWidgetIds = new int[]{myWidgetId}; // set selected widget only
        }

        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mformat.setTimeZone(TimeZone.getDefault());

//        long dPlus2 = System.currentTimeMillis() + Utility.ONEDAY_MS * 2;
//
//        String tDate = mformat.format(dPlus2);
//        dPlus2 = Utility.convertStringToMs(tDate, "23:59:59"); // until D+2 23:59:59

//        Log.d(LOG_TAG, "dPlus2 ="+dPlus2+" CURRENT " +System.currentTimeMillis()+" tDate "+tDate );

        String from_date_ms = String.valueOf(System.currentTimeMillis()-Utility.HALFDAY_MS*3); // from D-36hours ago
        //String to_date_ms = String.valueOf(dPlus2); // until D+2
        String to_date_ms = String.valueOf(System.currentTimeMillis()+Utility.HALFDAY_MS*3); // until D+36hours
        Uri buildScoreWithDateMsUri = DatabaseContract.scores_table.buildScoreWithDateMs(from_date_ms, to_date_ms);


        String match_dates[] = {from_date_ms, to_date_ms};
        data = getContentResolver().query(buildScoreWithDateMsUri, FOOTBALL_COLUMNS, null,
                match_dates, DatabaseContract.scores_table.MATCH_DATE_MS + " ASC");

        if (data == null) {
            return;
        }

        if (data.getCount()==0) // if no result
        {
            data.close();
            for (int appWidgetId : appWidgetIds) {
                int layoutId;
                layoutId = R.layout.widget_today;
                RemoteViews views = new RemoteViews(getPackageName(), layoutId); // not hardcoding use  context.getPackageName()

                views.setViewVisibility(R.id.widget_button_prev, View.INVISIBLE);
                views.setViewVisibility(R.id.widget_button_next, View.INVISIBLE);
                views.setViewVisibility(R.id.widget_today_empty, View.VISIBLE);
                // Tell the AppWidgetManager to perform an update on the current appwidget
                appWidgetManager.updateAppWidget(appWidgetId, views);  // after onclick intent must call this... it effect

            }

            return;
        }

        int maxPos = data.getCount() - 1; // max index

        if (dataPos <= 0) {
            dataPos = 0;
            prevDataPos = 0;
        } else {
            prevDataPos = dataPos - 1;
        }

        if (dataPos >= maxPos)
            dataPos = maxPos;
        nextDataPos = dataPos + 1;

        Log.v(LOG_TAG, "getDataPos:" + dataPos + " MaxPos:" + maxPos + "NextPos" + nextDataPos + "PrevPos" + prevDataPos);

        if (data.moveToPosition(dataPos)) { // move to cursor

            Log.v(LOG_TAG, "widget data fetched");

            // Extract the football data from the Cursor

            matchId = data.getLong(INDEX_MATCH_ID);
            matchDate = data.getString(INDEX_DATE_COL);
            matchTime = data.getString(INDEX_TIME_COL);
            matchHomeName = data.getString(INDEX_HOME_COL);
            matchAwayName = data.getString(INDEX_AWAY_COL);
            matchHomeGoals = data.getInt(INDEX_HOME_GOALS_COL);
            matchAwayGoals = data.getInt(INDEX_AWAY_GOALS_COL);
            matchDateMs = data.getLong(INDEX_MATCH_DATE_MS);
        }
        data.close();

        // Perform this loop procedure for each Football widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId;


            layoutId = R.layout.widget_today;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId); // not hardcoding use  context.getPackageName()

            // Add the data to the RemoteViews

            String description = getString(R.string.msg_widget_today_description); // for content description
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                //setRemoteContentDescription(views, description);
            }

            views.setViewVisibility(R.id.widget_today_empty, View.INVISIBLE);// emtpty view make invisible

            views.setImageViewResource(R.id.widget_icon_home, Utility.getTeamCrestByTeamName(matchHomeName));
            views.setImageViewResource(R.id.widget_icon_away, Utility.getTeamCrestByTeamName(matchAwayName));

            dayName = Utility.getDayNameFromMillis(getApplicationContext(), matchDateMs);

            views.setTextViewText(R.id.widget_text_home, matchHomeName);
            views.setTextViewText(R.id.widget_text_away, matchAwayName);

            views.setTextViewText(R.id.widget_home_goals, (matchHomeGoals == -1) ? "" : NumberFormat.getInstance().format(matchHomeGoals));
            views.setTextViewText(R.id.widget_away_goals, (matchAwayGoals == -1) ? "" : NumberFormat.getInstance().format(matchAwayGoals));

            views.setTextViewText(R.id.widget_time, matchTime);
            views.setTextViewText(R.id.widget_date, dayName);

            Log.v(LOG_TAG, "matchaway:" + matchAwayName);

            if (dataPos == 0) { // if first data --> inactive prev button
                views.setViewVisibility(R.id.widget_button_prev, View.INVISIBLE);
            } else {
                views.setViewVisibility(R.id.widget_button_prev, View.VISIBLE);
                intent = new Intent(FootballWidget.FOOTBALL_TODAY_WIDGET_PROVIDER);
                intent.setAction(FootballWidget.ACTION_PREV_PRESSED);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId); //https://developer.android.com/guide/topics/appwidgets/index.html
                intent.putExtra(FootballWidget.DATA_POS, prevDataPos);

                PendingIntent pendingIntent_prev = PendingIntent.getBroadcast(this, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget_button_prev, pendingIntent_prev);

            }

            if (nextDataPos > maxPos) {
                views.setViewVisibility(R.id.widget_button_next, View.INVISIBLE);
            } else {
                views.setViewVisibility(R.id.widget_button_next, View.VISIBLE);

                intent = new Intent(FootballWidget.FOOTBALL_TODAY_WIDGET_PROVIDER);
                intent.setAction(FootballWidget.ACTION_NEXT_PRESSED);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId); //https://developer.android.com/guide/topics/appwidgets/index.html
                intent.putExtra(FootballWidget.DATA_POS, nextDataPos);

                PendingIntent pendingIntent_next = PendingIntent.getBroadcast(this, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget_button_next, pendingIntent_next);// important --> instead of 0, put appWidgetId
            }


            // Launch main App : widget contents touch --> open app
            boolean isRtl = Utility.isRtlMode(getApplicationContext());

            Intent launchIntent = new Intent(this, MainActivity.class);
            launchIntent.putExtra("FROM_WIDGET", true); // temp
            launchIntent.putExtra("Pager_Current", Utility.getPagerIndexFromMillis(matchDateMs, isRtl)); // get pager index
            launchIntent.putExtra("Selected_match", matchId);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT); // update mode(not 0) on last parameter

            Log.v(LOG_TAG, "Pager_Current:" + Utility.getPagerIndexFromMillis(matchDateMs, isRtl) + "Selected_match" + matchId);
            views.setOnClickPendingIntent(R.id.widget_contents, pendingIntent);

            Log.v(LOG_TAG, "widgetID:" + appWidgetId + "next" + nextDataPos);

            // Tell the AppWidgetManager to perform an update on the current appwidget
            appWidgetManager.updateAppWidget(appWidgetId, views);  // after onclick intent must call this... it effect
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_header, description);
    }
}
