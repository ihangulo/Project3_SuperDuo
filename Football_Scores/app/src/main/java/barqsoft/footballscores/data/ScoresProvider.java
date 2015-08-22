package barqsoft.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.data.ScoresDBHelper;

/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       ScoresProvider.java
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *     Original version Created by yehya khaled on 2/25/2015.
 **/

public class ScoresProvider extends ContentProvider
{
    private static ScoresDBHelper mOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int MATCHES_WITH_DATE_MS = 105; // milliscond unix time
    private static final int MATCHES_FROM_DATE_MS = 106; // milliscond unix time
    private static final int MATCHES_FROM_DATE_MS_ITEM = 107; // milliscond unix time

    private UriMatcher muriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder ScoreQuery = new SQLiteQueryBuilder();
    private static final String SCORES_BY_LEAGUE = DatabaseContract.scores_table.LEAGUE_COL + " = ?";


    private static final String SCORES_BY_DATE =
            DatabaseContract.scores_table.DATE_COL + " LIKE ?";
//    private static final String SCORES_BY_DATE3 =
//            "("+ DatabaseContract.scores_table.DATE_COL + " LIKE ? ) OR ("+DatabaseContract.scores_table.DATE_COL + " LIKE ?) OR ("+ DatabaseContract.scores_table.DATE_COL+ " LIKE ? ) ";

    private static final String SCORES_BY_DATE_MS =
            "("+ DatabaseContract.scores_table.MATCH_DATE_MS + " >= ? ) AND ("+DatabaseContract.scores_table.MATCH_DATE_MS + " <= ?) ";

    private static final String SCORES_FROM_DATE_MS =
            "("+ DatabaseContract.scores_table.MATCH_DATE_MS + " >= ? )";


    private static final String SCORES_BY_ID =
            DatabaseContract.scores_table.MATCH_ID + " = ?";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY; // important. except 'content://' String...
        matcher.addURI(authority, null , MATCHES);
        matcher.addURI(authority, "date/*" ,     MATCHES_WITH_DATE);
        matcher.addURI(authority, "date_ms",        MATCHES_WITH_DATE_MS);
        matcher.addURI(authority, "from_date_ms",   MATCHES_FROM_DATE_MS);
        matcher.addURI(authority, "league"+"/*" ,   MATCHES_WITH_LEAGUE);
        matcher.addURI(authority,  "id"+"/#" ,       MATCHES_WITH_ID);

        return matcher;
    }


    private int match_uri(Uri uri)
    {
        String link = uri.toString();
        {
           if(link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString()))
           {
               return MATCHES;
           }
           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreWithDate("").toString()))
           {
               return MATCHES_WITH_DATE;
           }
           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreWithDateMs("","").toString()))
           {
               return MATCHES_WITH_DATE_MS;
           }
           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreFromDateMs("").toString()))
           {
               return MATCHES_FROM_DATE_MS;
           }

           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreWithId().toString()))
           {
               return MATCHES_WITH_ID;
           }
           else if(link.contentEquals(DatabaseContract.scores_table.buildScoreWithLeague().toString()))
           {
               return MATCHES_WITH_LEAGUE;
           }
        }
        return -1;
    }



    @Override
    public boolean onCreate()
    {
        mOpenHelper = new ScoresDBHelper(getContext());
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = muriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.scores_table.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.scores_table.CONTENT_TYPE;

            case MATCHES_WITH_DATE_MS:
                return DatabaseContract.scores_table.CONTENT_TYPE;

            case MATCHES_FROM_DATE_MS:
                return DatabaseContract.scores_table.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri );
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;

        final int match = muriMatcher.match(uri);
        switch (match)
        {
            case MATCHES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,null,null,null,null,sortOrder);
                break;
            case MATCHES_WITH_DATE:
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[1]);
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[2]);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_DATE,selectionArgs,null,null,sortOrder);
                break;

            case MATCHES_WITH_DATE_MS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection,SCORES_BY_DATE_MS,selectionArgs,null,null,sortOrder);
                break;

            case MATCHES_FROM_DATE_MS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection,SCORES_FROM_DATE_MS,selectionArgs,null,null,sortOrder);
                break;


            case MATCHES_WITH_ID:
                    retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_ID,selectionArgs,null,null,sortOrder);
                break;

            case MATCHES_WITH_LEAGUE: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_LEAGUE,selectionArgs,null,null,sortOrder);
                break;

            default: throw new UnsupportedOperationException("Unknown Uri : " + uri); // 에러처리 필요
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);


        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch(muriMatcher.match(uri))
        {
            case MATCHES:
                db.beginTransaction();
                int returncount = 0;
                try
                {
                    for(ContentValues value : values)  {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE); // if there is data already then update
                        if (_id != -1) {
                            returncount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returncount;
            default:
                return super.bulkInsert(uri,values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
