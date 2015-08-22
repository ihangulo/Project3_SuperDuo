package barqsoft.footballscores;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import barqsoft.footballscores.service.FootballFetchService;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 3 SuperDuo: Football Scores
*   ================================================
*
*        from : 1th JUL 2015
*        to : 22 AUG 2015
*
*     Kwanghyun JUNG
*     ihangulo@gmail.com
*
*    Android Devlelopment Nanodegree
*    Udacity
*
*    MainActivity.java
*
*
*/


public class MainActivity extends AppCompatActivity {
    final static String LOG_TAG="MainActivity";
    public static long selected_match_id = 0;
    long selected_match=-1;
    public static int current_fragment = 2;

    private PagerFragment mPagerFragment;

    // prefrence
    static final String FOOTBALL_PREF = "FootballPref";
    static final String FOOTBALL_LAST_SYNC_TIME = "FOOTBALL_LAST_SYNC_TIME";
    static final String FOOTBALL_TIMEZONE = "FOOTBALL_TIMEZONE";
    public static final int SCORES_LOADER = 0;
    // broadcast
    private BroadcastReceiver messageReciever;
    public static final String MESSAGE_EVENT = "barqsoft.footballscores.ACTION_FETCH_UPDATED";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static final String RESULT_KEY = "FETCH_RESULT";
    final static int FETCH_SERVICE_OK = 1;
    final static int FETCH_SERVICE_FAIL = 0;

    ProgressBar mProgressCircle; // loading circle

    boolean FROM_WIDGET = false;
    int pager_current=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            Intent intent = getIntent();

            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Log.v(LOG_TAG, "FROMWIDGET " + extras.getLong("Selected_match", 0L) + " PAGER " + extras.getInt("Pager_Current", 2));
                    FROM_WIDGET = extras.getBoolean("FROM_WIDGET", false);

                    if (FROM_WIDGET) {
                        // if from widget
                        pager_current = extras.getInt("Pager_Current", 2);
                        selected_match = extras.getLong("Selected_match", 0L);
                    }
                    extras.clear();
                } else {
                    FROM_WIDGET = false;
                }
            }

//            current_fragment = pager_current;
//            selected_match_id = selected_match;
            //getIntent().set


        if (savedInstanceState == null) {
            mPagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPagerFragment, "foot_ball")
                    .commit();
        } else {

            current_fragment = savedInstanceState.getInt("Pager_Current");
            selected_match_id = savedInstanceState.getLong("Selected_match");
        }


        // Register Broadcast Receiver --> get message from FootballFetchService
        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);

        mProgressCircle = (ProgressBar) findViewById(R.id.progressCircle);

    }


    // determine it need data sync
    private boolean needDataSync() {

        SharedPreferences settings = getSharedPreferences(FOOTBALL_PREF, MODE_PRIVATE);
        if (settings != null) {

            // 1. if last sync time is more than 1 hour
            long lastSyncTime = settings.getLong(FOOTBALL_LAST_SYNC_TIME, 0); // get last sync time (ms)
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastSyncTime) > 3600000) // if less than 1 hour
                return true;

            // 2. if it is changed Timezone --> clean all database and refresh
            Time now_t = new Time();
            now_t.setToNow();
            long myTimezone = settings.getLong(FOOTBALL_TIMEZONE, Utility.ONEDAY_MS * 2); // default is invalid value


            Log.v(LOG_TAG,"Timezone now :"+now_t.gmtoff+" saved:"+myTimezone );
            if (now_t.gmtoff != myTimezone)
               return true;
        }


        return false;
    }

    // fetch & update  from D-3 to D+3
    private void FetchAllScrores() {
        // if it passed more than 1 hour then fecth new data from server
        Intent service_start = new Intent(this, FootballFetchService.class);
        startService(service_start);
        mProgressCircle.bringToFront();
        mProgressCircle.setVisibility(View.VISIBLE); // show loading circle

    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "onDestroy()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this, AboutActivity.class);
            startActivity(start_about);
            return true;
        } else if (id == R.id.action_refresh) { // refresh data
            FetchAllScrores();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

//      Log.v(save_tag, "will save");
//        Log.v(LOG_TAG, "fragment: " + String.valueOf(my_main.mPagerHandler.getCurrentItem()));
//        Log.v(LOG_TAG, "selected id: " + selected_match_id);
        outState.putInt("Pager_Current", mPagerFragment.mPagerHandler.getCurrentItem());
        outState.putLong("Selected_match", selected_match_id);
        getSupportFragmentManager().putFragment(outState, "mPagerFragment", mPagerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        Log.v(LOG_TAG, "onRestoreInstanceState");

            current_fragment = savedInstanceState.getInt("Pager_Current");
            selected_match_id = savedInstanceState.getLong("Selected_match");
            mPagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mPagerFragment");

        super.onRestoreInstanceState(savedInstanceState);
    }

    // after onRestoreInstanceState()
    @Override
    protected void onResume() {
        super.onResume();

        Log.v(LOG_TAG, "onResume()");
        // update data
        if (needDataSync())
            FetchAllScrores(); // fetch & update score data

        if (FROM_WIDGET) {
            current_fragment = pager_current;
            selected_match_id = selected_match;
            if(mPagerFragment!=null) {
                if (mPagerFragment.mPagerHandler != null)
                    mPagerFragment.mPagerHandler.setCurrentItem(current_fragment); // open selected page
            }
            FROM_WIDGET = false;
        }
    }

    // Broadcast receiver.......
    // get message from FootballFetchService
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int msgResult = intent.getIntExtra(RESULT_KEY, FETCH_SERVICE_FAIL);
            String msgKey = intent.getStringExtra(MESSAGE_KEY);

            if (msgResult == FETCH_SERVICE_OK) { // set last sync time
                SharedPreferences settings = getSharedPreferences(FOOTBALL_PREF, MODE_PRIVATE);
                if (settings != null) {

                    // save sycn time
                    SharedPreferences.Editor editor = settings.edit();
                    if (editor != null) {
                        editor.putLong(FOOTBALL_LAST_SYNC_TIME, System.currentTimeMillis()); // set to current time

                        // save timezone
                        Time now_t = new Time();
                        now_t.setToNow();
                        editor.putLong(FOOTBALL_TIMEZONE, now_t.gmtoff);
                        editor.apply();
                    }
                }
            }

            if (msgKey != null) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE); // show off loading circle
            }
        }
    }

// // http://nnish.com/2014/12/16/scheduled-notifications-in-android-using-alarm-manager/
    // http://codetheory.in/android-broadcast-receivers/
}
