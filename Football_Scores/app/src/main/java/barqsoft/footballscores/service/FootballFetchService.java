package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.widget.FootballWidget;


/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       FootballFetchService.java : get Data from football server
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *     Original version Created by yehya khaled on 3/2/2015.
 **/
public class FootballFetchService extends IntentService {
    public static final String LOG_TAG = "FootballFetchService";

    private static final String MY_KEY = " "; // your key here

    private static final int MODE_TIME_FRAME = 0;
    private static final int MODE_MATCH_DAY = 1;
    private static final int MODE_TIME_FRAME_OLD = 2;


    int mode = 0;
    int result;
    String matchDate;


    public FootballFetchService() {
        super("FootballFetchService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {


        Log.v(LOG_TAG, "onHandleIntent()");

        // check internet connection
        if (!Utility.isNetworkAvailable(this)) { // if no internet service
            sendBroadcast(getString(R.string.no_internet_connection), FETCH_SERVICE_FAIL);
            return;
        }

        matchDate = intent.getStringExtra("MATCH_DATE");

        // depend on MATCH_DATE argument it fetches data


        if (matchDate == null || matchDate.equals("")) {

            mode = MODE_TIME_FRAME;

            Date startDate = new Date(System.currentTimeMillis()-86400000*3); // D-3
            Date endDate = new Date(System.currentTimeMillis()+86400000*3); // D+3

            // because there is different style number, so put this result only standard English number (0-9)
            // if else this format converted to ٢٠١٥-٠٨-١٢:٢٠١٥-٠٨-١ something else (arabic number.. etc)
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            // get formatted string
            String sStartDate = mformat.format(startDate);
            String sEndDate = mformat.format(endDate);

            Log.v(LOG_TAG,"timeframe:"+sStartDate+":"+sEndDate);

            result = getData(sStartDate, sEndDate, MODE_TIME_FRAME); // get D-3 to D+3

        } else {
            result = getData(matchDate, matchDate, MODE_MATCH_DAY);
        }
        if (result<0)
            sendBroadcast(getString(R.string.msg_data_sync_fail), FETCH_SERVICE_FAIL);
        else
            sendBroadcast(getString(R.string.msg_data_sync_ok), FETCH_SERVICE_OK);
    }

    private int getData(String timeFrame, int mode) {

        return getData(timeFrame, timeFrame, mode);
    }
    private int getData(String timeFrameStart, String timeFrameEnd, int mode ) {

        Log.v(LOG_TAG, "getData():" + timeFrameStart +"/"+timeFrameEnd);

        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL (original)

//        final String BASE_URL = "http://hanguloi.cafe24.com/football/"; // M YWON TEST SERVER

        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days

        final String QUERY_TIME_FRAME_START = "timeFrameStart";
        final String QUERY_TIME_FRAME_END = "timeFrameEnd";

        Uri fetch_build = null;

        int ret=0;

        if(mode == MODE_TIME_FRAME)  {
            fetch_build = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_TIME_FRAME_START, timeFrameStart)
                    .appendQueryParameter(QUERY_TIME_FRAME_END, timeFrameEnd)
                    .build();
        }

        else if (mode == MODE_MATCH_DAY) { // only use when i test
            //http://api.football-data.org/alpha/fixtures/?timeFrameStart=2015-03-01&timeFrameEnd=2015-03-05

            fetch_build = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_TIME_FRAME_START, timeFrameStart)
                    .appendQueryParameter(QUERY_TIME_FRAME_END, timeFrameEnd)
                    .build();
            //Log.v(LOG_TAG, fetch_build.toString());
        }

        else if (mode == MODE_TIME_FRAME_OLD) {
            fetch_build = Uri.parse(BASE_URL).buildUpon().
                    appendQueryParameter(QUERY_TIME_FRAME, timeFrameStart).build();
        }


        //Log.v(LOG_TAG, fetch_build.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection

        /* error code

FBD_MESSAGE_CODE

            400 Bad Request
            Your request was malformed, most likely the value of a Filter was not set according to the Data Type that is expected.

            403 Restricted Resource
            You tried to access a resource that exists but is not available for you. This can be out of the following reasons:
                the resource is only available to authenticated clients.
                the resource is not available in the API version you are using.

            404 Not Found
            You tried to access a resource that doesn’t exist.

            429 Too Many Requests
            You exceeded your allowed requests per minute/day, depending on API version and your user status. Look at "Other Conventions" for more information.
             */
        try {

            if (fetch_build == null) return -1;
            String str= fetch_build.toString();
            if (str==null) return -1;

            URL fetch = new URL(str);
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", MY_KEY); // my won key
            m_connection.connect();


            Log.v(LOG_TAG, "fetchString " + fetch_build.toString());

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return 0;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return 0;
            }
            JSON_data = buffer.toString();
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "NullPointerException here" + e.getMessage());
            ret=-1;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here" + e.getMessage());
            ret = -1;

        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream");
                    ret=-1;

                }
            }

            if (ret==-1) return -1;
        }
        try {

                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");

            // if it is no season, then use below
                /*
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    return processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);

                }
              */
                return processJSONdata(JSON_data, getApplicationContext(), true);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return -1;
        }


    }

    private int processJSONdata(String JSONdata, Context mContext, boolean isReal) throws JSONException {
        //Log.v(LOG_TAG, JSONdata);

        //JSON data
        //final String SERIE_A = "398";


        final String CHAMPIONS_LEAGUE = "362";
        final String PRIMERA_DIVISION = "358";


        final String BUNDESLIGA1 = "394"; //http://api.football-data.org/alpha/soccerseasons/394"
        final String BUNDESLIGA2 = "395";  // "http://api.football-data.org/alpha/soccerseasons/395"
        final String LIGUE_1 = "396"; //http://api.football-data.org/alpha/soccerseasons/396"
        final String LIGUE_2 = "397"; //http://api.football-data.org/alpha/soccerseasons/396"
        final String PREMIER_LEGAUE = "398"; // "http://api.football-data.org/alpha/soccerseasons/398"


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";
        final String STATUS = "status"; // match status "FINISHED" / "TIMED"

        //Match data
        String League;
        String mDate;
        String mTime;
        String Home;
        String Away;
        String Home_goals;
        String Away_goals;
        String match_id;
        String match_day;
        String match_date_ms;
        String status;

        long match_date_milli; // UTC match date milliscond time

        final String ERROR_MSG = "error";

        try {

            JSONObject footballJson = new JSONObject(JSONdata);

            //  have an error?
            if (footballJson.has(ERROR_MSG)) {
                String errorMsg = footballJson.getString(ERROR_MSG);
                Log.e(LOG_TAG, errorMsg); // error processing
                return -1;
            }

            JSONArray matches = footballJson.getJSONArray(FIXTURES);

            if (matches == null) {
                Log.e(LOG_TAG, "ERROR at JSONArray matches = footballJson.getJSONArray(FIXTURES); "); // error processing
                return -1;
            }

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {
                JSONObject match_data = matches.getJSONObject(i);

                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK, "");
                if (League.equals(PREMIER_LEGAUE) ||
                        League.equals(CHAMPIONS_LEAGUE) ||
                        League.equals(BUNDESLIGA1) ||
                        League.equals(BUNDESLIGA2) ||
                        League.equals(LIGUE_1) ||
                        League.equals(LIGUE_2) ||
                        League.equals(PRIMERA_DIVISION)) {
                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");


                    // subtract time from data .. date, time

                    mDate = match_data.getString(MATCH_DATE); // date shows ex)"2015-07-14T11:30:00Z"

                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z")); // last half(next to T until Z)
                    mDate = mDate.substring(0, mDate.indexOf("T")); // first half(until T)


                    try {

                        SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.ENGLISH);
                        match_date.setTimeZone(TimeZone.getTimeZone("UTC")); // data is UTC (GMT+0)
                        Date utcDate = match_date.parse(mDate + mTime);

                        match_date_ms= String.valueOf(utcDate.getTime()); // get millisecond time

                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm", Locale.ENGLISH);
                        new_date.setTimeZone(TimeZone.getDefault()); // get this device's timezone.
                        String tmpDateTime = new_date.format(utcDate);   // convert to this time zone
                        mTime = tmpDateTime.substring(tmpDateTime.indexOf(":") + 1);
                        mDate = tmpDateTime.substring(0, tmpDateTime.indexOf(":"));



                     } catch (Exception e) {
                        //Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                        return -1;
                    }

                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);

                        if (mDate.equals("")) {
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000)); // dummy data
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            mDate = mformat.format(fragmentdate);

                        } else
                            mDate = matchDate; // hangulo

                    }

                    Home = match_data.getString(HOME_TEAM);
                    Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);

                    // not used now
                    //status=match_data.getJSONObject(RESULT).getString(STATUS); // match status --> 왜 이걸 DB에 저장하지 않을까? ㅠㅠ 나중에 지우자 TODO


                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);


                    match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);


                    match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);


                    match_values.put(DatabaseContract.scores_table.MATCH_DATE_MS, match_date_ms); // hangulo match_date_millisecond

                    values.add(match_values);
                }
            }

            // insert bulk data
            int inserted_data;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);

            Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));

            // update widget
            updateWidgets();

          return inserted_data;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return -1;
    }


    // send broadcast

    final static int FETCH_SERVICE_OK = 1;
    final static int FETCH_SERVICE_FAIL = 0;

    void sendBroadcast(String msg, int mode){

        int result=FETCH_SERVICE_FAIL;

        if (mode == FETCH_SERVICE_OK || mode==FETCH_SERVICE_FAIL)
            result = mode;

        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.RESULT_KEY, result);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY, msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);

    }


    // update widget
    private void updateWidgets() {
        Context context = getApplicationContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(FootballWidget.ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}


