package it.jaschke.alexandria;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

/*
*   ================================================
*       SuperDuo: Alexandria
*       BookDetailActivity.java : show book detail (only phone mode)
*   ================================================
*
*    Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*/
public class BookDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        if(savedInstanceState == null) { // if new...
            Intent intent=getIntent();
            if (intent != null)
            {
                BookDetailFragment fragment = new BookDetailFragment();

                Bundle args = new Bundle();
                fragment.setArguments(intent.getExtras()); // pass the all extra argument

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.book_detail_container, fragment)
                        .commit();
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
