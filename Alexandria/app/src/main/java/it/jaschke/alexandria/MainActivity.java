package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 3 SuperDuo: Alexandria*
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
*    *Library of Alexandria
*    one of the largest and most significant libraries of the ancient world.
*    https://en.wikipedia.org/wiki/Library_of_Alexandria
*
*/
public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback {

    // ActionBarActivity is deprecated -> change to AppCompatActivity
    // http://android-developers.blogspot.in/2015/04/android-support-library-221.html

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    final String LOG_TAG= MainActivity.class.getSimpleName();

    private NavigationDrawerFragment navigationDrawerFragment;
    Fragment nextFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean mTwoPane;
    private BroadcastReceiver messageReciever;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    public static String mEan; // save ean
    private boolean mAddmode; // addmode flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwoPane = getResources().getBoolean(R.bool.two_pane); // two_pane mode check

       setContentView(R.layout.activity_main);

        // Register Broadcast Receiver --> get message from BookService
        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever,filter);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        setBlankFragment();
    }

    // if left drawer menu selected
    @Override
    public void onNavigationDrawerItemSelected(int position) {

        //getSupportFragmentManager().popBackStack(); // clear stack

        switch (position){
            default:
            case 0:
                nextFragment = new ListOfBooksFragment();
                mAddmode=false;
                break;
            case 1:
                nextFragment = new AddBookFragment();
                mAddmode=true;
                break;
            case 2:
                nextFragment = new AboutFragment();
                mAddmode=false;
                break;

        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, nextFragment)
                //.addToBackStack((String) title)  // not used this version
                .commit();

        mEan = ""; // reset mEan
        setBlankFragment();


    }

    void setBlankFragment() {
        if (!mTwoPane) return; // if not two pane mode then return

        Fragment blankFragment = new BlankFragment();
        if(findViewById(R.id.detail_container) != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, blankFragment)
                        .commit();
        }


    }


    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
          int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // save fragment
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mEan != null && mEan.length()==13) // if exist mEan, save it
            outState.putString("EAN", mEan);
        outState.putBoolean(BookDetailFragment.ADDMODE_KEY, mAddmode);
        getSupportFragmentManager().putFragment(outState, "mCurrentFragment", nextFragment);
        super.onSaveInstanceState(outState);
    }

    //restore fragment
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onRestoreInstanceState");

        String myEan=savedInstanceState.getString("EAN"); //get EAN
        int myPosition = savedInstanceState.getInt("POSITION"); //get EAN

        nextFragment = getSupportFragmentManager().getFragment(savedInstanceState, "mCurrentFragment");
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, nextFragment)
                    .commit();
            if (myEan != null && myEan.length()==13 && !(nextFragment instanceof AboutFragment) ) { // if there is detail view
                onListItemSelected(myEan);
            }
        }

        if (nextFragment instanceof ListOfBooksFragment) {
            ((ListOfBooksFragment)nextFragment).scrollToSavedPosition(); // scroll to saved position
        }

        mAddmode = savedInstanceState.getBoolean(BookDetailFragment.ADDMODE_KEY, false);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }


    final int REQUEST_BOOK_DETAIL = 1;

    // from interface callback
    @Override
    public void onListItemSelected(String ean) {

        mEan= ean;

        Bundle args = new Bundle();
        args.putString(BookDetailFragment.EAN_KEY, ean);


        if (mTwoPane) {
            args.putBoolean(BookDetailFragment.ADDMODE_KEY, mAddmode); // set addmode

            BookDetailFragment detailFragment = new BookDetailFragment();
            detailFragment.setArguments(args);

            if (findViewById(R.id.detail_container) != null) { //if two pane mode
                getSupportFragmentManager().beginTransaction()
                        .replace( R.id.detail_container, detailFragment)
                        .commit();
            }
        }
        else {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailFragment.EAN_KEY, ean);
            startActivityForResult(intent, REQUEST_BOOK_DETAIL);
        }
    }

    // from interface callback
    // refresh listview
    @Override
    public void onListItemUpdated() {

        if (nextFragment instanceof ListOfBooksFragment)
            ((ListOfBooksFragment) nextFragment).removeItemFromAdapterAndRefresh();
         else if (nextFragment instanceof AddBookFragment) {
            ((AddBookFragment) nextFragment).AddBoookIsbnRefresh(); // set refresh text field
        }

        if (mTwoPane)
          setBlankFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BOOK_DETAIL) {
            if (resultCode==RESULT_OK) { // the referesh
                // refresh list
                onListItemUpdated();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Broadcast receiver
    // get message from BookService
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String newEan = intent.getStringExtra("NEW_EAN");

            // if there is new Book is fetched & TABLET // and mode is now ADDMODE
            if (newEan != null && mTwoPane) {
                onListItemSelected(newEan);
            }

            String msgKey = intent.getStringExtra(MESSAGE_KEY);
            if(msgKey != null)
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();

        }
    }


    // determine is this device tablet --> not use
    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    // confirm dialog before quit app
    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<1){
            // show confirm dialog

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.msg_really_quit)
                    .setTitle(R.string.app_name)

                    .setPositiveButton(getString(R.string.msg_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }

                    })
                    .setNegativeButton(R.string.msg_no, null);

            AlertDialog dialog = builder.create();
            dialog.show();

        }
        else super.onBackPressed();
    }

}