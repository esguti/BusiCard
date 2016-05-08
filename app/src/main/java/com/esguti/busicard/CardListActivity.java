package com.esguti.busicard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


import com.esguti.busicard.analytics.AnalyticsApplication;
import com.esguti.busicard.data.CardLoader;
import com.esguti.busicard.data.CardsContract;
import com.esguti.busicard.data.CardsDatabase;
import com.esguti.busicard.geo.Constants;
import com.esguti.busicard.geo.FetchAddressIntentService;
import com.esguti.busicard.ui.CardAdapter;
import com.esguti.busicard.ui.CardListListener;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * An activity representing a list of cards. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CardDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CardListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, CardListListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = CardListActivity.class.getSimpleName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean m_TwoPane;
    private RecyclerView m_RecyclerView;
    private CardAdapter m_Adapter;
    private Context m_Context;
    private CardDetailFragment m_CardDetailFragment = null;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient m_GoogleApiClient;
    private AddressResultReceiver m_ResultReceiver;
    private String m_Country = "Unknown";
    private Tracker m_Tracker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        buildGoogleApiClient();
        m_ResultReceiver = new AddressResultReceiver(new Handler());

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        m_Tracker = application.getDefaultTracker();
        // [END shared_tracker]

        m_Context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { addCard(); }
        });

        m_RecyclerView = (RecyclerView) findViewById(R.id.card_list);

        m_RecyclerView.setLayoutManager(new LinearLayoutManager(this));

        m_Adapter = new CardAdapter(m_Context);
        m_RecyclerView.setAdapter(m_Adapter);

        if (findViewById(R.id.card_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            m_TwoPane = true;
            CoordinatorLayout.LayoutParams params =
                    new CoordinatorLayout.LayoutParams(
                            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                            CoordinatorLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT;
            fab.setLayoutParams(params);
        }

        //getSupportLoaderManager().initLoader(0, null, this);
        getSupportLoaderManager().restartLoader(1, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        m_GoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (m_GoogleApiClient.isConnected()) {
            m_GoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(LOG_TAG, "Sending track Info");
        m_Tracker.setScreenName("App Start");
        m_Tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        m_GoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void addCard() {
        Toast.makeText(this, "To save a new card take a photo", Toast.LENGTH_SHORT)
                .show();
        
        if (m_TwoPane) {
            setCardFragment(-1);
        } else {
            Intent intent = new Intent(m_Context, CardDetailActivity.class);
            //intent.putExtra(CardDetailFragment.ARG_ITEM_ID, holder.mItem.id);
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return CardLoader.newAllCardsInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if( cursor != null ) { m_Adapter.swapCursor(cursor); }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { m_RecyclerView.setAdapter(null); }


    @Override
    public void updateList() {
        getSupportLoaderManager().restartLoader(1, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( m_TwoPane && m_CardDetailFragment != null)
            m_CardDetailFragment.onActionResult(requestCode, resultCode, data);
    }

    public void changeCard(long cardId){
        if( m_TwoPane ){
            setCardFragment(cardId);
        }else {
            m_Context.startActivity(new Intent(Intent.ACTION_VIEW,
                    CardsContract.Cards.buildCardUri(cardId)));
        }
    }

    @Override
    public CardsDatabase getDatabase() {
        return new CardsDatabase(this);
    }

    private void setCardFragment(long cardId){
        m_CardDetailFragment = new CardDetailFragment();
        m_CardDetailFragment.setListener(this,this);

        Bundle arguments = new Bundle();
        arguments.putLong(CardDetailFragment.ARG_CARD_ID, cardId);
        m_CardDetailFragment.setArguments(arguments);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_detail_container, m_CardDetailFragment)
                .commit();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(m_GoogleApiClient);
        if( location != null && Geocoder.isPresent() ) {
            Intent intent = new Intent(this, FetchAddressIntentService.class);
            intent.putExtra(Constants.RECEIVER, m_ResultReceiver);
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
            startService(intent);
        }

    }

    private void makeToast(String text){
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            String country = resultData.getString(Constants.RESULT_DATA_KEY);
            // Show country toast
            if (resultCode == Constants.SUCCESS_RESULT) {
                makeToast(getString(R.string.card_list_activity_hello) + " "  + country);
                Log.i(LOG_TAG, "Sending track Info: " + country);
                m_Tracker.setScreenName("Country~" + country);
                m_Tracker.send(new HitBuilders.ScreenViewBuilder().build());

            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        m_GoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Connection failed: " + connectionResult);
    }
}
