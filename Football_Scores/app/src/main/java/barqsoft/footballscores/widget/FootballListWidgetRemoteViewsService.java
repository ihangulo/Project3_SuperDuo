package barqsoft.footballscores.widget;

/**
 * Created by khjung on 2015-07-16.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.NumberFormat;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.data.DatabaseContract;

/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       FootballListWidgetRemoteViewsService.java
 *
 *       Collection Widget Remote View Service
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *     Original version Sunshine (https://github.com/udacity/Advanced_Android_Development/tree/7.05_Pretty_Wallpaper_Time)
 **/

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FootballListWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = FootballListWidgetRemoteViewsService.class.getSimpleName() ;

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
    private static final int INDEX_MATCH_DATE_MS = 8;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();


                String from_date_ms = String.valueOf(System.currentTimeMillis()-Utility.HALFDAY_MS*3); // from D-36hours ago
                String to_date_ms = String.valueOf(System.currentTimeMillis()+Utility.HALFDAY_MS*3); // until D+36hours
                Uri buildScoreWithDateMsUri = DatabaseContract.scores_table.buildScoreWithDateMs(from_date_ms,to_date_ms );

                String match_dates[] = {from_date_ms, to_date_ms};
                data = getContentResolver().query(buildScoreWithDateMsUri, FOOTBALL_COLUMNS, null,
                        match_dates, DatabaseContract.scores_table.MATCH_DATE_MS + " ASC");

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }


            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                // Extract the football data from the Cursor

                long matchId = data.getLong(INDEX_MATCH_ID);
                String matchDate=data.getString(INDEX_DATE_COL);
                String matchTime = data.getString(INDEX_TIME_COL);
                String matchHomeName = data.getString(INDEX_HOME_COL);
                String matchAwayName= data.getString(INDEX_AWAY_COL);
                int matchHomeGoals= data.getInt(INDEX_HOME_GOALS_COL);
                int matchAwayGoals= data.getInt(INDEX_AWAY_GOALS_COL);
                long matchDateMs = data.getLong(INDEX_MATCH_DATE_MS);

                // Add the data to the RemoteViews

                String description = getString(R.string.msg_widget_list_description); // for content description
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }


                views.setImageViewResource(R.id.widget_icon_home, Utility.getTeamCrestByTeamName(matchHomeName));
                views.setImageViewResource(R.id.widget_icon_away, Utility.getTeamCrestByTeamName(matchAwayName));

                String dayName= Utility.getDayNameFromMillis(getApplicationContext(), matchDateMs);

                views.setTextViewText(R.id.widget_text_home, matchHomeName);
                views.setTextViewText(R.id.widget_text_away, matchAwayName);

                views.setTextViewText(R.id.widget_home_goals, (matchHomeGoals == -1) ? "" :  NumberFormat.getInstance().format(matchHomeGoals));
                views.setTextViewText(R.id.widget_away_goals, (matchAwayGoals == -1) ? "" : NumberFormat.getInstance().format(matchAwayGoals));

                views.setTextViewText(R.id.widget_time, matchTime);
                views.setTextViewText(R.id.widget_date, dayName);


                boolean isRtl = Utility.isRtlMode(getApplicationContext());


                Bundle extras = new Bundle();
                extras.putBoolean("FROM_WIDGET", true); // is from widget?
                extras.putInt("Pager_Current", Utility.getPagerIndexFromMillis(matchDateMs, isRtl)); // get pager index
                extras.putLong("Selected_match", matchId);

                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }


            // except header info, all contentDescription is not need (all TextView)
            // images are all not need to read (it is duplicated description)
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_list_header, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_MATCH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}