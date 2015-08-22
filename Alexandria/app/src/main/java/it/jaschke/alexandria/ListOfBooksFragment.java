package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;

/*
*   ================================================
*        SuperDuo: Alexandria
*        ListOfBooksFragment.java : Show booklist which is already registered
*   ================================================
*
*    Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*
*    Original version :  Created by saj
*/

public class ListOfBooksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final String LOG_TAG= ListOfBooksFragment.class.getSimpleName();
    public BookListAdapter mBookListAdapter;
    private ListView mBookList;
    private int mPosition = ListView.INVALID_POSITION;
    private EditText mSearchText;
    private ImageButton mSearchButton;

    private final int LOADER_ID = 10;

    public ListOfBooksFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("POSITION");
        }

        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        mBookListAdapter = new BookListAdapter(getActivity(), cursor, 0);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        mSearchText = (EditText) rootView.findViewById(R.id.searchText);

        mSearchButton= (ImageButton) rootView.findViewById(R.id.searchButton);

        mSearchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooksFragment.this.restartLoader();
                    }
                }
        );

        mBookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        mBookList.setAdapter(mBookListAdapter);

        View emptyView= rootView.findViewById(R.id.bookList_empty);
        mBookList.setEmptyView(emptyView);

        // if click list item --> call back to main activity
        mBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mBookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    mPosition=position; // save position
                    ((Callback) getActivity()) // callback, to change detail fragment
                            .onListItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        // hide soft keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return rootView;
    }

    public void removeItemFromAdapterAndRefresh() {
        restartLoader();
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    // scroll to saved position (for rotate screen etc..)
    public void scrollToSavedPosition()
    {
        if (mPosition != ListView.INVALID_POSITION) {
            mBookList.smoothScrollToPosition(mPosition); // restore position
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = mSearchText.getText().toString();

        if(searchString.length()>0){ // search mode

            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString,searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mBookListAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mBookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt("POSITION",mPosition);
        super.onSaveInstanceState(outState);
    }
}
