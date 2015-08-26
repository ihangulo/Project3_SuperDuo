package it.jaschke.alexandria;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import it.jaschke.alexandria.barcode.SimpleScannerActivity;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
/*
*   ================================================
*        SuperDuo: Alexandria
*       AddBookFragment.java : used when new book register, it support barcode scan & key board input
*   ================================================
*
*    Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*    Original version :  Created by saj
*/


public class AddBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final String LOG_TAG= AddBookFragment.class.getSimpleName();

    final static int REQUEST_CODE_SCANNER_INTERNAL = 100;
    final static int REQUEST_CODE_SCANNER_EXTERNAL = 200;

    private EditText mEditTxtEan;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";

    private ImageView mBookCover;
    private TextView mTxtViewTitle1;
    private TextView mTxtViewSubTitle;
    private TextView mTxtViewAuthors;
    private TextView mTxtViewCat;
    private Button mBtnScan;

    private Button mBtnSave;
    private Button mBtnDelete;

    private int mScanType = 0 ; // 0 :internal scan 1:external scan

    boolean newStart = false;
    String lastSuccessEan=null;

    public AddBookFragment(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mEditTxtEan !=null) {
            outState.putString(EAN_CONTENT, mEditTxtEan.getText().toString());
            if (lastSuccessEan!=null && lastSuccessEan.length()==13) { // save recent successful fecthed book's ean
                outState.putString("LASTEAN", lastSuccessEan);
                outState.putBoolean("NEWSTART", true);
            }

        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEditTxtEan = (EditText) rootView.findViewById(R.id.ean);

        mBookCover = (ImageView) rootView.findViewById(R.id.bookCover);
        mTxtViewTitle1 = (TextView) rootView.findViewById(R.id.bookTitle1);
        mTxtViewSubTitle = (TextView) rootView.findViewById(R.id.bookSubTitle);
        mTxtViewAuthors= (TextView) rootView.findViewById(R.id.authors);
        mTxtViewCat = (TextView) rootView.findViewById(R.id.categories);
        mBtnSave =(Button) rootView.findViewById(R.id.save_button);
        mBtnDelete=(Button) rootView.findViewById(R.id.delete_button);
        mBtnScan = (Button) rootView.findViewById(R.id.scan_button);

        ImageView imgView = (ImageView) rootView.findViewById(R.id.imageView);
        if (MainActivity.mTwoPane)
            if (imgView != null)
                imgView.setVisibility(View.GONE); // image icon

        // default --> by Isbn is default
        mEditTxtEan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();


                // check is that all digit
                if (!TextUtils.isDigitsOnly(s)) {
                    Toast.makeText(getActivity(), getString(R.string.digits_only), Toast.LENGTH_SHORT).show();
                    return;
                }

                // catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                 /*
                    Code Reviewer's comment
                    2.The bug for the UX feedback - "The app could use some work. Sometimes when I add a book and
                    don’t double-check the ISBN, it just disappears!” isn't fixed and can be reproduced as follows:
                    Add a valid ISBN code, say 1234567897, let the book detail load. Then, add another digit,
                    say 2 to the already typed ISBN code, now at this point since the ISBN is invalid,
                    the book details are also cleared. The user might have entered that extra digit accidentally,
                    so ideally, whenever a book's detail is loaded let it stay on the screen until a new valid ISBN is entered.
                     */

                    // clearFields(); //  <=== so i'v  commenting out this line.. it works! and something more changed!
                    return;
                }

                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.STEXT, ean);
                bookIntent.setAction(BookService.FETCH_BOOK_BY_ISBN); // set action
                getActivity().startService(bookIntent); // go and fetch book

                AddBookFragment.this.restartLoader();
            }
        });

        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // barcode scan -> from scan type
                switch (mScanType) {
                    case 0:
                        launchInternalBarcodeScanner(); // using internal library
                        break;
                    case 1:
                        lauchExternalBarcodeScannerApp(); // using app (https://play.google.com/store/apps/details?id=com.google.zxing.client.android)
                        break;
                    default:
                        launchInternalBarcodeScanner(); // using internal library
                }

            }
        });

        // button next
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditTxtEan.setText("");
                clearFields();

            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete book by ISBN

                // first.. confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.msg_dialog_confirm_delete)
                        .setTitle(R.string.msg_dialog_confirm_title)
                        .setPositiveButton(getString(R.string.msg_delete_now), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Intent bookIntent = new Intent(getActivity(), BookService.class);
                                bookIntent.putExtra(BookService.STEXT, mEditTxtEan.getText().toString());
                                bookIntent.setAction(BookService.DELETE_BOOK);
                                getActivity().startService(bookIntent);
                                mEditTxtEan.setText("");
                                clearFields();
                            }
                        })
                        .setNegativeButton(getString(R.string.msg_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked no button
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        clearFields();
        if(savedInstanceState!=null){
            mEditTxtEan.setText(savedInstanceState.getString(EAN_CONTENT));
            mEditTxtEan.setHint("");
            lastSuccessEan = savedInstanceState.getString("LASTEAN",null);
            if (lastSuccessEan!=null && lastSuccessEan.length()!=13) { // if not valid
                lastSuccessEan = null;
                newStart=false;
            }
            else
                newStart= savedInstanceState.getBoolean("NEWSTART",false); // is that new start?

            AddBookFragment.this.restartLoader();
        }

        return rootView;
    }


    void AddBoookIsbnRefresh() {
        mEditTxtEan.setText("");
        //lastValidEan=null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // get barcode scan type
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mScanType = Integer.parseInt(prefs.getString( getString(R.string.pref_scanType_key), getString(R.string.pref_scan_default)));

        // set scan button text
        switch (mScanType) {

            case 0:
                mBtnScan.setText(R.string.scan_button_internal);
                break;
            case 1:
                mBtnScan.setText(R.string.scan_button_external);
                break;
        }
    }

    // using google play api barcode detection
    // https://search-codelabs.appspot.com/codelabs/bar-codes
    // (for future project)
    public void launchGooglePlayApiBarcode() {
    }

    public void launchInternalBarcodeScanner() {

        Intent intent = new Intent(getActivity(), SimpleScannerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCANNER_INTERNAL);

    }

    public void lauchExternalBarcodeScannerApp() {

        // source :http://stackoverflow.com/questions/3872063/launch-an-application-from-another-application-on-android
        // https://play.google.com/store/apps/details?id=com.google.zxing.client.android
        // https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/Intents.java
        final String zxingPackage = "com.google.zxing.client.android";
        final String zxingScanAction = "com.google.zxing.client.android.SCAN";
        final String zxingMarketURL = "market://details?id=com.google.zxing.client.android";

        Intent intent = new Intent(zxingScanAction);
            intent.setPackage(zxingPackage);

        try {

            Log.v(LOG_TAG, "startActivityForResult : REQUEST_CODE_SCANNER_EXTERNAL" );
            startActivityForResult(intent, REQUEST_CODE_SCANNER_EXTERNAL);

        } catch (ActivityNotFoundException e) {
            //implement prompt dialog asking user to download the package
            AlertDialog.Builder downloadDialog = new AlertDialog.Builder(getActivity());
            downloadDialog.setTitle(getString(R.string.title_app_download));
            downloadDialog.setMessage(getString(R.string.error_no_app));
            downloadDialog.setPositiveButton(getString(R.string.msg_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri uri = Uri.parse(zxingMarketURL);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Utility.showAlertDialog(getActivity(), getString(R.string.title_error), getString(R.string.not_found_playstore));

                            }
                        }
                    });
            downloadDialog.setNegativeButton(getString(R.string.msg_no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = downloadDialog.create();
            dialog.show();
        }

    }

    // if receive value from scanner.. process it
    // http://stackoverflow.com/questions/5604550/calling-barcode-scanner-on-a-button-click-in-android-application
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_SCANNER_INTERNAL: {

                if (resultCode == Activity.RESULT_OK) {
                    String barCode = data.getStringExtra("SCAN_RESULT");
                    mEditTxtEan.setText(barCode);
                }

                break;
            }

            case REQUEST_CODE_SCANNER_EXTERNAL: {
                if (resultCode == Activity.RESULT_OK) { // if OK
                    String barCode = data.getStringExtra("SCAN_RESULT");
                    mEditTxtEan.setText(barCode);
                }
                break;
            }


        }

    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // remove loading circle
        ProgressBar progressCircle = (ProgressBar) rootView.findViewById(R.id.progressCircle); // view progressbarif (progressCircle != null)
        progressCircle.setVisibility(View.VISIBLE);


        // if rotate screen, EditText is garbage(there is no book ean) but below screen has recent successful fetched data...
        String eanStr="";
        if (newStart && lastSuccessEan!=null)
            eanStr=lastSuccessEan;
        else
            eanStr= mEditTxtEan.getText().toString();

        if(eanStr.length()<10){
            return null;
        }

        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }

        if (eanStr.length() != 13)
            return null;

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {

        // remove loading circle
        ProgressBar progressCircle = (ProgressBar) rootView.findViewById(R.id.progressCircle); // view progressbarif (progressCircle != null)/
        progressCircle.setVisibility(View.INVISIBLE);

        if (!cursor.moveToFirst()) { // if there is no data
            return;
        }
        // if tabletmode

        String _ID="";
        if (newStart && lastSuccessEan!=null) {
            _ID = lastSuccessEan;
            newStart=false;
        }
        else {
            // get data ID
             _ID = mEditTxtEan.getText().toString();
            if (_ID.length() == 10 && !_ID.startsWith("978")) { // to make ISBN-13
                _ID = "978" + _ID;
            }
            lastSuccessEan=_ID; // save current screen EAN
        }



        if (MainActivity.mTwoPane) {

            // to avoid error : Can not perform this action inside of onLoadFinished
            // http://stackoverflow.com/questions/22788684/can-not-perform-this-action-inside-of-onloadfinished
            // http://stackoverflow.com/questions/13218310/put-string-in-handler
            myHandler handler = new myHandler();

            Message msg= new Message();
            Bundle bundle = new Bundle();
            bundle.putString("_ID",  _ID) ; // put string to bundle
            msg.setData(bundle);
            handler.sendMessage(msg); // just call handler , callback function (right pannel information)

            hideKeyboard();

            return;
        } else {

            // phone mode

            try {
                String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                if (mTxtViewTitle1 != null && bookTitle != null)
                    mTxtViewTitle1.setText(bookTitle);

                String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
                if (mTxtViewSubTitle != null && bookSubTitle != null)
                    mTxtViewSubTitle.setText(bookSubTitle);

                String authors = cursor.getString(cursor.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));


                if (authors == null)
                    authors = ""; // error passing

                String[] authorsArr = authors.split(","); // for error checking

                if (mTxtViewAuthors != null) {
                    mTxtViewAuthors.setLines(authorsArr.length);
                    mTxtViewAuthors.setText(authors.replace(",", "\n"));
                }

                String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));


                if (Patterns.WEB_URL.matcher(imgUrl).matches()) {

                    Picasso.with(getActivity())
                            .load(imgUrl)
                            .fit()
                            .error(R.drawable.ic_library_books_black_48dp) // error (not found)
                            .placeholder(R.drawable.ic_library_books_black_48dp) // loading
                            .into(mBookCover);
                } else {

                    Picasso.with(getActivity())
                            .load(R.drawable.ic_library_books_black_48dp)
                            .fit()
                            .placeholder(R.drawable.ic_library_books_black_48dp) // loading
                            .into(mBookCover);
                }


                String categories = cursor.getString(cursor.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));


                mTxtViewCat.setText(categories);


                mBtnSave.setVisibility(View.VISIBLE);
                mBtnDelete.setVisibility(View.VISIBLE);

                hideKeyboard();

            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // nothing to do
    }

    private void clearFields(){

        mTxtViewTitle1.setText("");
        mTxtViewSubTitle.setText("");
        mTxtViewAuthors.setText("");
        mTxtViewCat.setText("");
        mBookCover.setImageBitmap(null);
        mBtnSave.setVisibility(View.INVISIBLE);
        mBtnDelete.setVisibility(View.INVISIBLE);
        lastSuccessEan=null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // to call callback function onLoadFinished
    class myHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData(); // get bundle
            String _ID = bundle.getString("_ID");
                ((it.jaschke.alexandria.api.Callback) getActivity()) // callback, to change detail fragment
                        .onListItemSelected(_ID);
        }
    };
}
