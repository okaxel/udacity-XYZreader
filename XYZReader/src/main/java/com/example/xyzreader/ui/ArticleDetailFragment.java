package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;

    private ImageView mPhotoView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    /**
     * Creates a new instance of the fragment with the given id.
     *
     * @param itemId Id of the new fragment
     * @return       The created fragment
     */
    public static ArticleDetailFragment newInstance(long itemId) {

        Bundle arguments = new Bundle();

        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);

        return fragment;

    }

    /**
     * Simple constructor.
     *
     * @param savedInstanceState State of instances
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            mItemId = getArguments().getLong(ARG_ITEM_ID);

        }

        setHasOptionsMenu(true);

    }

    /**
     * Very simple cast method.
     *
     * @return The instance of the activity
     */
    public ArticleDetailActivity getActivityCast() {

        return (ArticleDetailActivity) getActivity();

    }

    /**
     * Stock metod, this case it manages the loader after activity is created.
     *
     * @param savedInstanceState State of instances to pus super-vise
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);

    }

    /**
     * It creates the view.
     *
     * @param inflater           Inflater to use
     * @param container          Container for the view
     * @param savedInstanceState State of instances to send super-vise
     * @return                   A fancy new view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        Toolbar toolbar = mRootView.findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ((ArticleDetailActivity) getActivity()).onSupportNavigateUp();

            }

        });

        mPhotoView = mRootView.findViewById(R.id.photo);
        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));

            }

        });

        bindViews();

        return mRootView;

    }

    /**
     * Calculates progress.
     *
     * @param v   State
     * @param min Minimum
     * @param max Maximum
     * @return    Progress level from 0 to 1
     */
    static float progress(float v, float min, float max) {

        return constrain((v - min) / (max - min), 0, 1);

    }

    /**
     * Progress helper.
     *
     * @param val Value
     * @param min Minimum
     * @param max Maximum
     * @return    Simple result
     */
    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    /**
     * Parses date.
     *
     * @return The paresd date
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
     * Simple viewBinder. It uses class level variables therefore it hasn't parameters.
     */
    private void bindViews() {

        if (mRootView == null) {

            return;

        }

        TextView titleView = mRootView.findViewById(R.id.article_title);
        TextView bylineView = mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = mRootView.findViewById(R.id.article_body);


        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {

            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();

            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            } else {

                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                        + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            }

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {

                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {

                            Bitmap bitmap = imageContainer.getBitmap();

                            if (bitmap != null) {

                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mPhotoView.setContentDescription(getResources().getString(
                                        R.string.image_cover_article,mCursor.getString(ArticleLoader.Query.TITLE)));
                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);

                            }

                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }

                    });

        } else {

            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");

        }

    }

    /**
     * Very simple method to create new loader.
     *
     * @param i      Required but not used
     * @param bundle Required but not used
     * @return       A new instance
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);

    }

    /**
     * Finishes loader.
     *
     * @param cursorLoader Required but not used
     * @param cursor       Cursor to work with
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (!isAdded()) {

            if (cursor != null) {

                cursor.close();
            }

            return;
        }

        mCursor = cursor;

        if (mCursor != null && !mCursor.moveToFirst()) {

            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;

        }

        bindViews();

    }

    /**
     * Resets loader.
     *
     * @param cursorLoader Required but not used
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

        mCursor = null;
        bindViews();

    }
}
