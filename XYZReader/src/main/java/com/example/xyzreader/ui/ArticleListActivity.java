package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListActivity.class.toString();
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");

    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();

    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    private boolean mIsRefreshing = false;

    /**
     * Creates the activity.
     *
     * @param savedInstanceState Sate of instances
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = findViewById(R.id.toolbar);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = findViewById(R.id.recycler_view);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {

            refresh();

        }

    }

    /**
     * Initiates update.
     */
    private void refresh() {

        startService(new Intent(this, UpdaterService.class));

    }

    /**
     * Activity's onStart event registers receiver.
     */
    @Override
    protected void onStart() {

        super.onStart();

        registerReceiver(mRefreshingReceiver,

                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));

    }

    /**
     * Activity's onStart event unregisters receiver.
     */
    @Override
    protected void onStop() {

        super.onStop();

        unregisterReceiver(mRefreshingReceiver);

    }

    /**
     * Receiver is declared here since it needs a method to override.
     */
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {

        /**
         * Handles received data from intent.
         *
         * @param context The best friend of the developer is just required but not used now
         * @param intent  Intent to get data
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {

                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();

            }

        }

    };

    /**
     * Cares about some update stuff.
     */
    private void updateRefreshingUI() {

        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);

    }

    /**
     * Handles the creation of the loader.
     *
     * @param i      Required but not used
     * @param bundle Required but not used
     * @return       A brand new loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return ArticleLoader.newAllArticlesInstance(this);

    }

    /**
     * Handles the finishing of the loading process.
     *
     * @param cursorLoader Required but not used
     * @param cursor       Cursor with useful data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        Adapter adapter = new Adapter(cursor);

        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);

    }

    /**
     * Handles the reset of the loader.
     *
     * @param loader Required but not used
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mRecyclerView.setAdapter(null);

    }

    /**
     * Adapter class for te RecyclerView.
     */
    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private Cursor mCursor;

        /**
         * Simple constructor.
         *
         * @param cursor Cursor to work with
         */
        Adapter(Cursor cursor) {

            mCursor = cursor;

        }

        /**
         * Gets item's id based on the cursor.
         *
         * @param position Position to search
         * @return         The id of the item
         */
        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        /**
         * Creates viewHolders.
         *
         * @param parent   The viewGroup to inflate the viewHolder in
         * @param viewType Required but not used
         * @return         The new viewHolder
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));

                }

            });

            return vh;

        }

        /**
         * Parses date.
         *
         * @return The parsed date
         */
        private Date parsePublishedDate() {

            try {

                String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);

                return dateFormat.parse(date);

            } catch (ParseException ex) {

                Log.e(TAG, ex.getMessage());
                Log.i(TAG, "passing today's date");

                return new Date();

            }

        }

        /**
         * Binds viewHolder.
         *
         * @param holder   Holder to bind
         * @param position Item's position to bind
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();

            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                holder.subtitleView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));

            } else {

                holder.subtitleView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                        + "<br/>" + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR)));

            }

            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());

            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

            holder.thumbnailView.setContentDescription(getResources().
                    getString(R.string.image_cover_article,mCursor.getString(ArticleLoader.Query.TITLE)));

        }

        /**
         * Gets the count of the items.
         *
         * @return The count of the items
         */
        @Override
        public int getItemCount() {

            return mCursor.getCount();

        }

    }

    /**
     * This class represents recyclerView's items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        /**
         * Simple constructor.
         *
         * @param view A new item
         */
        ViewHolder(View view) {

            super(view);

            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);

        }

    }

}
