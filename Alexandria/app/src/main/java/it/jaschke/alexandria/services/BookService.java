package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
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

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.Utility;
import it.jaschke.alexandria.data.AlexandriaContract;


/*
*   ================================================
*        SuperDuo: Alexandria
*        BookService.java - Fetch book information from Google book server
*   ================================================
*
*     Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*
*    Original version :  Created by saj
*/


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK_BY_ISBN = "it.jaschke.alexandria.services.action.FETCH_BOOK_ISBN";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";
    public static final String STEXT = "it.jaschke.alexandria.services.extra.STEXT"; // search text


    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String Action = intent.getAction();

            if (Action == null) return;
            switch (Action) {
                case FETCH_BOOK_BY_ISBN :
                    final String ean = intent.getStringExtra(STEXT);
                    fetchBookByIsbn(ean);
                    break;

                case DELETE_BOOK :
                    final String dEan = intent.getStringExtra(STEXT);
                    deleteBook(dEan);
                    break;
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean!=null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }


    // fetch by book by isbn
    private void fetchBookByIsbn(String ean) {
        if(ean!=null && ean.length()!=13 &&  !TextUtils.isDigitsOnly(ean)){ // if not 13 digit number
            return;
        }
        fetchBook("isbn:", ean); // isbn mode
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String qParameter, String sText) {

        if (qParameter == null || sText ==null) return;

        // Check wheather book is already in my database.
        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(sText)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if(bookEntry.getCount()>0){ // if exsit

            String bookTitle;
            // get book name

            // fetch msg "already registered"
            String msg = getResources().getString(R.string.book_already_registerd);

            // then fetch title & make message (include book title)
            if(bookEntry.moveToFirst()) {
                bookTitle = bookEntry.getString(bookEntry.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                msg=String.format(msg, bookTitle);
            }

            Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY, msg); // send "not found" message
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);


            bookEntry.close();
            return;
        }

        bookEntry.close();


        // Now we are connecting to internet.
        // First check the intrnet connection

        if (!Utility.isNetworkAvailable(this)) {

            Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.no_internet_connection)); // send "not found" message
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);


            return;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/volumes?"; // https://developers.google.com/books/docs/v1/using


            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = qParameter + sText;

            Uri builtUri = Uri.parse(GOOGLE_BOOKS_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());


            // 성공하면 200 OK 를 제일 앞에 돌려준다
            //

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }


        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        // error processing (hangulo)
        if (bookJsonString == null) {

            Log.e(LOG_TAG, "bookJsonString is null ");

            // we'v got unknown error : Server side error
            Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.system_error));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
            return;

        }

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            }else{
                Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found)); // send "not found" message
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = "";
            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc="";
            if(bookInfo.has(DESC)){
                desc = bookInfo.getString(DESC);
            }

            String imgUrl = "";
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            writeBackBook(sText, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                writeBackAuthors(sText, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                writeBackCategories(sText,bookInfo.getJSONArray(CATEGORIES) );
            }

            // OK, all book info is fetched successfuly. then send message to MainActivity (to update info)
            if (MainActivity.mTwoPane) {
                Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                messageIntent.putExtra("NEW_EAN",sText); // send NEW_EAN
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    // write new all book info into Database
    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        try {
            getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
        } catch (android.database.SQLException e) {
            Log.e(LOG_TAG, "Error[writeBackBook] :", e);
        }


    }

    // write Author info into Database
    private void writeBackAuthors(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    // write Category info into Database
    private void writeBackCategories(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
 }