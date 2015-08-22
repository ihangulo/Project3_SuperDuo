package barqsoft.footballscores;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import barqsoft.footballscores.data.DatabaseContract;

/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       MainScreenFragment.java
 *
 *       Main fragment
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *      Original version Created by yehya khaled
 **/
/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String LOG_TAG = "MainScreenFrag:hangulo";
    public ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentDate = new String[1];

    private ListView mScrollList;

    private View mRootView;
    TextView mEmptyView;
    ProgressBar mProgressCircle;

    public MainScreenFragment()
    {
    }

    public void setFragmentDate(String date)
    {
        fragmentDate[0] = date;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        fragmentDate[0] = arguments.getString("MATCH_DATE", "");

        Log.v(LOG_TAG, "onCreateView()" + fragmentDate[0]);

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        mScrollList = (ListView) mRootView.findViewById(R.id.scores_list);

        // set empty view of listview
        mEmptyView= (TextView) mRootView.findViewById(R.id.scores_list_empty);
        mScrollList.setEmptyView(mEmptyView);

        mAdapter = new ScoresAdapter(getActivity(),null,0);
        mScrollList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);

        mAdapter.detail_match_id = MainActivity.selected_match_id;

        mScrollList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScoresAdapter.ViewHolder selected = (ScoresAdapter.ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });

        return mRootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(fragmentDate[0]), null,null, fragmentDate, DatabaseContract.scores_table.TIME_COL);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        mAdapter.swapCursor(cursor);
        if (cursor.getCount() == 0)
            mEmptyView.setText(R.string.empty_football_list); // set there is no game.
        mAdapter.notifyDataSetChanged();

        // ok. let's search "opened item" (launched from widget)
        int open_position = -1;

        if (mAdapter.detail_match_id > 0) {
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    if (mAdapter.detail_match_id == cursor.getInt(mAdapter.COL_MATCH_ID)) {
                        open_position = i;
                        break;
                    }
                    cursor.moveToNext();
                }
            }

            if (open_position > -1) // if there is open item
                mScrollList.setSelection(open_position);
        }
        Log.v(LOG_TAG, "Match ID"+mAdapter.detail_match_id+" Scroll to :"+open_position);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

}
