package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/*
*   ================================================
*       SuperDuo: Football Scores
*       DatabaseContract.java
*   ================================================
**    Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
*
*/
public class DatabaseContract {
    public static final String SCORES_TABLE = "scores_table";
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class scores_table implements BaseColumns {

        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";
        public static final String MATCH_DATE_MS = "match_date_ms"; // millisecond datetime of match (UTC)

        //public static Uri SCORES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH)
        //.build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY;

        public static Uri buildScoreWithLeague() {
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }

        public static Uri buildScoreWithId() {
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }

        public static Uri buildScoreWithDate(String sDate) //    /scores/date/2015-01-01
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("date")
                    .appendPath(sDate)
                    .build();

        }

        public static Uri buildScoreWithDateMs(String from_ms, String to_ms) //  /scores/date_ms?from_ms=2015-01-01&to_ms=2015-05-01
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("date_ms")
                    .appendQueryParameter("from_ms", from_ms)
                    .appendQueryParameter("to_ms", to_ms)
                    .build();
        }

        public static Uri buildScoreFromDateMs(String from_ms) //    /scores/date_from_ms?from_ms=2015-01-01
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("from_date_ms")
                    .appendQueryParameter("from_ms", from_ms)
                    .build(); // options
        }

    }

}
