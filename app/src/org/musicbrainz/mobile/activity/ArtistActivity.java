package org.musicbrainz.mobile.activity;

import java.util.List;

import org.musicbrainz.android.api.data.Artist;
import org.musicbrainz.android.api.data.ReleaseGroupStub;
import org.musicbrainz.android.api.data.Tag;
import org.musicbrainz.android.api.data.UserData;
import org.musicbrainz.android.api.data.WebLink;
import org.musicbrainz.mobile.R;
import org.musicbrainz.mobile.adapter.pager.ArtistPagerAdapter;
import org.musicbrainz.mobile.async.ArtistLoader;
import org.musicbrainz.mobile.async.result.AsyncEntityResult;
import org.musicbrainz.mobile.config.Configuration;
import org.musicbrainz.mobile.fragment.ArtistReleaseGroupsFragment;
import org.musicbrainz.mobile.fragment.ArtistReleaseGroupsFragment.ReleaseGroupsFragmentCallback;
import org.musicbrainz.mobile.fragment.EditFragment;
import org.musicbrainz.mobile.fragment.EditFragment.EditFragmentCallback;
import org.musicbrainz.mobile.fragment.LinksFragment;
import org.musicbrainz.mobile.fragment.LinksFragment.LinksFragmentCallback;
import org.musicbrainz.mobile.intent.IntentFactory.Extra;
import org.musicbrainz.mobile.string.StringFormat;
import org.musicbrainz.mobile.util.Utils;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Activity that retrieves and displays information about an artist given an
 * artist MBID.
 */
public class ArtistActivity extends MusicBrainzActivity implements LoaderCallbacks<AsyncEntityResult<Artist>>,
        LinksFragmentCallback, ReleaseGroupsFragmentCallback, EditFragmentCallback {

    private static final int ARTIST_LOADER = 0;

    private String mbid;
    private Artist artist;
    private UserData userData;

    private View loading;
    private View error;

    private RatingBar ratingBar;
    private TextView tagView;

    private ArtistPagerAdapter pagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mbid = getIntent().getStringExtra(Extra.ARTIST_MBID);
        setContentView(R.layout.activity_artist);
        configurePager();
        findViews();
        setSupportProgressBarIndeterminateVisibility(false);
        getSupportLoaderManager().initLoader(ARTIST_LOADER, savedInstanceState, this);
    }

    protected void populateLayout() {
        TextView artistText = (TextView) findViewById(R.id.artist_artist);

        artistText.setText(artist.getName());
        ratingBar.setRating(artist.getRating());
        tagView.setText(StringFormat.commaSeparateTags(artist.getTags(), this));

        artistText.setSelected(true);
        tagView.setSelected(true);

        displayMessagesForEmptyData(); // TODO move to fragments

        updateFragments();
        loading.setVisibility(View.GONE);
    }

    private void updateFragments() {
        ((LinksFragment) pagerAdapter.getFragment(0)).update();
        ((ArtistReleaseGroupsFragment) pagerAdapter.getFragment(1)).update();
        ((EditFragment) pagerAdapter.getFragment(2)).update();
    }

    private void configurePager() {
        pagerAdapter = new ArtistPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        pager.setCurrentItem(1);
    }

    private void findViews() {
        loading = findViewById(R.id.loading);
        error = findViewById(R.id.error);
        ratingBar = (RatingBar) findViewById(R.id.rating);
        tagView = (TextView) findViewById(R.id.tags);
    }

    private void displayMessagesForEmptyData() {
        if (artist.getReleases().isEmpty()) {
            TextView noRes = (TextView) findViewById(R.id.noreleases);
            noRes.setVisibility(View.VISIBLE);
        }
        if (artist.getLinks().isEmpty()) {
            TextView noRes = (TextView) findViewById(R.id.nolinks);
            noRes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.artist, menu);
        ShareActionProvider actionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareIntent(Utils.shareIntent(Configuration.ARTIST_SHARE + mbid));
        return true;
    }

    @Override
    public Loader<AsyncEntityResult<Artist>> onCreateLoader(int id, Bundle args) {
        return new ArtistLoader(mbid);
    }

    @Override
    public void onLoadFinished(Loader<AsyncEntityResult<Artist>> loader, AsyncEntityResult<Artist> container) {
        handleLoadResult(container);
    }

    private void handleLoadResult(AsyncEntityResult<Artist> result) {
        switch (result.getStatus()) {
        case SUCCESS:
            artist = result.getData();
            userData = result.getUserData();
            populateLayout();
            break;
        case EXCEPTION:
            showConnectionErrorWarning();
        }
    }

    private void showConnectionErrorWarning() {
        error.setVisibility(View.VISIBLE);
        Button retry = (Button) error.findViewById(R.id.retry_button);
        retry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                error.setVisibility(View.GONE);
                getSupportLoaderManager().restartLoader(ARTIST_LOADER, null, ArtistActivity.this);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<AsyncEntityResult<Artist>> loader) {
        loader.reset();
    }

    @Override
    public void showLoading() {
        setSupportProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void hideLoading() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public String getMbid() {
        return mbid;
    }

    @Override
    public UserData getUserData() {
        return userData;
    }

    @Override
    public void updateTags(List<Tag> tags) {
        artist.setTags(tags);
        tagView.setText(StringFormat.commaSeparateTags(tags, this));
    }

    @Override
    public void updateRating(Float rating) {
        artist.setRating(rating);
        ratingBar.setRating(rating);
    }

    @Override
    public List<ReleaseGroupStub> getReleaseGroups() {
        return artist.getReleaseGroups();
    }

    @Override
    public List<WebLink> getLinks() {
        return artist.getLinks();
    }

}
