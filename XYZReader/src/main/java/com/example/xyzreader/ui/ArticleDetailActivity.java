package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
// @SuppressWarnings("ALL")
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    /**
     * Initializes the activity.
     *
     * @param savedInstanceState State of instances
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {

                super.onPageScrollStateChanged(state);

            }

            @Override
            public void onPageSelected(int position) {

                if (mCursor != null) {

                    mCursor.moveToPosition(position);

                    mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);

                }

            }

        });

        if (savedInstanceState == null) {

            if (getIntent() != null && getIntent().getData() != null) {

                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;

            }

        }

    }

    /**
     * Creates loader.
     *
     * @param i      Not used.
     * @param bundle Not used.
     * @return       The created loader.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return ArticleLoader.newAllArticlesInstance(this);

    }

    /**
     * Handles data after load is finished.
     *
     * @param cursorLoader Mot used
     * @param cursor       Cursor to work with
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {

            mCursor.moveToFirst();

            // COMPLETED: optimize - At least I think so.
            while (!mCursor.isAfterLast()) {

                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {

                    mPager.setCurrentItem(mCursor.getPosition(), false);
                    break;
                }

                mCursor.moveToNext();
            }

            mStartId = 0;

        }

    }

    /**
     * Handle if loader resets.
     *
     * @param cursorLoader Not used
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();

    }

    /**
     * Simple adapter class. Although it's deprecated, it isn't task of this exercise to solve it
     * differently so I supressed inspection to leave it as is.
     */
    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Simple constructor.
         *
         * @param fm Instance of FragmentManager
         */
        MyPagerAdapter(FragmentManager fm) {

            super(fm);

        }

        /**
         * Sets primary item.
         *
         * @param container Requeired but not used parameter
         * @param position  Requeired but not used parameter
         * @param object    Contains the instance of the needed fragment
         */
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {

            super.setPrimaryItem(container, position, object);

            ArticleDetailFragment fragment = (ArticleDetailFragment) object;

        }

        /**
         * Simple getItem method.
         *
         * @param position Poistion of the item
         * @return         The created item itself
         */
        @Override
        public Fragment getItem(int position) {

            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));

        }

        /**
         * Simple getCount method.
         *
         * @return The count of the cursor
         */
        @Override
        public int getCount() {

            return (mCursor != null) ? mCursor.getCount() : 0;

        }

    }

}
