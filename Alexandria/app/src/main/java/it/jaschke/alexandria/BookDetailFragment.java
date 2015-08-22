package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


/*
*   ================================================
*        SuperDuo: Alexandria
*        BookDetailFragment.java : Shows detail book information
*   ================================================
*
*    Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*
*    Original version :  Created by saj
*/

public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    final String LOG_TAG= BookDetailFragment.class.getSimpleName();

    public static final String EAN_KEY = "EAN";
    public static final String ADDMODE_KEY = "ADDMODE";
    private final int LOADER_ID = 10;
    private View rootView;
    private String mStrEan;
    private String mBookTitle;
    private ShareActionProvider mShareActionProvider;
    ImageView mFullBookCover;

    TextView mTxtViewTitle;
    TextView mTxtViewSubTitle;
    TextView mTxtViewAuthor;
    TextView mTxtViewDesc;
    TextView mTxtViewCat;
    Button mBtnDelete;
    Button mBtnSave;

    private boolean mAddMode; // is add mode?



    boolean mTwoPane;

    public BookDetailFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mTwoPane = getResources().getBoolean(R.bool.two_pane); // two_pane mode check

        if (savedInstanceState != null) {
            mStrEan = savedInstanceState.getString(BookDetailFragment.EAN_KEY);
            mAddMode = savedInstanceState.getBoolean(ADDMODE_KEY, false);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
        else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mStrEan = arguments.getString(BookDetailFragment.EAN_KEY);
                mAddMode = arguments.getBoolean(ADDMODE_KEY, false);
                getLoaderManager().restartLoader(LOADER_ID, null, this);
            }
        }

        rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);

        // set delete button
        mBtnDelete = (Button) rootView.findViewById(R.id.delete_button);
        mBtnSave = (Button) rootView.findViewById(R.id.save_button);

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // first.. confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.msg_dialog_confirm_delete)
                        .setTitle(R.string.msg_dialog_confirm_title)
                        .setPositiveButton(getString(R.string.msg_delete_now), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // delete item
                                Intent bookIntent = new Intent(getActivity(), BookService.class);
                                bookIntent.putExtra(BookService.STEXT, mStrEan);
                                bookIntent.setAction(BookService.DELETE_BOOK);
                                getActivity().startService(bookIntent);
                                if(mTwoPane) {
                                    // set right detail fragment clean
                                    // refresh left list
                                    ((Callback) getActivity()).onListItemUpdated();
                                }
                                else {
                                    getActivity().setResult(Activity.RESULT_OK);
                                    getActivity().finish(); // close current detail activity
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.msg_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        mTxtViewTitle = (TextView) rootView.findViewById(R.id.fullBookTitle);
        mTxtViewSubTitle = (TextView) rootView.findViewById(R.id.fullBookSubTitle);
        mTxtViewAuthor = (TextView) rootView.findViewById(R.id.fullBookAuthors);
        mTxtViewDesc = (TextView) rootView.findViewById(R.id.fullBookDesc);
        mTxtViewCat = (TextView) rootView.findViewById(R.id.fullBookCategories);
        mFullBookCover = (ImageView) rootView.findViewById(R.id.fullBookCover);

        if (mAddMode) { // if addmode then change button text & show delete button
            mBtnSave.setVisibility(View.VISIBLE);
            mBtnSave.setText(getString(R.string.ok_button));

            mBtnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Callback) getActivity()).onListItemUpdated(); // in fact, just refresh screen
                }
            });

        }

        // hide soft keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        return rootView;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }



    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(mStrEan)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));

        if (mTxtViewTitle != null && mBookTitle != null)
            mTxtViewTitle.setText(mBookTitle);

        // process share menu

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        else
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + mBookTitle);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));

        if (mTxtViewSubTitle != null && bookSubTitle != null)
            mTxtViewSubTitle.setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));

        if (mTxtViewDesc != null && desc != null)
            mTxtViewDesc.setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");


        if (mTxtViewAuthor != null) {
            mTxtViewAuthor.setLines(authorsArr.length);
            mTxtViewAuthor.setText(authors.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {

            Picasso.with(getActivity())
                    .load(imgUrl)
                    .fit()
                    .error(R.drawable.ic_launcher) // error (not found)
                    .placeholder(R.drawable.ic_launcher) // loading
                    .into(mFullBookCover, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    mFullBookCover.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError() {

                                }
                            }
                    );

        } else {

            Picasso.with(getActivity())
                    .load(R.drawable.ic_launcher)
                    .fit()
                    .placeholder(R.drawable.ic_launcher) // loading
                    .into(mFullBookCover);
        }



        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));

        if (mTxtViewCat != null && categories != null)
            mTxtViewCat.setText(categories);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

        mTxtViewTitle.setText("");
        mTxtViewSubTitle.setText("");
        mTxtViewAuthor.setText("");
        mTxtViewDesc.setText("");
        mTxtViewCat.setText("");
        mFullBookCover.setImageBitmap(null);
        mShareActionProvider.setShareIntent(null); // set share to null

    }

    // save current state
    @Override
    public void  onSaveInstanceState(Bundle savedInstanceState) {

        if (mStrEan != null)
            savedInstanceState.putString(BookDetailFragment.EAN_KEY, mStrEan);
        savedInstanceState.putBoolean("ADDMODE", mAddMode);
        super.onSaveInstanceState(savedInstanceState);
    }


}