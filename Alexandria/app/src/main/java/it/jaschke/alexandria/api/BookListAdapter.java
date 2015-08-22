package it.jaschke.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;


/*
*   ================================================
*        SuperDuo: Alexandria
*       BookListAdapter.java
*   ================================================
*
*     Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*
*    Original version :  Created by saj on 11/01/15.
*/
public class BookListAdapter extends CursorAdapter {


    public static class ViewHolder {
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View view) {
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
        }
    }
    private final static String LOG_TAG ="BookListAdapter";

    public BookListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

//        Log.v(LOG_TAG, "bindView");
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
//         new DownloadImage(viewHolder.bookCover).execute(imgUrl);

        // i use picasso for image processing

        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {

            Picasso.with(context)
                    .load(imgUrl)
                    .fit()
                    .error(R.drawable.ic_launcher) // error (not found)
                    .placeholder(R.drawable.ic_launcher) // loading
                    .into(viewHolder.bookCover, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {

                                }
                            }
                    );
//                new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);

        } else {

            Picasso.with(context)
                    .load(R.drawable.ic_launcher)
                    .fit()
                    .placeholder(R.drawable.ic_launcher) // loading
                    .into(viewHolder.bookCover);
        }

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        viewHolder.bookTitle.setText(bookTitle);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        viewHolder.bookSubTitle.setText(bookSubTitle);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
}
