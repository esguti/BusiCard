package com.esguti.busicard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


import com.esguti.busicard.data.CardLoader;
import com.esguti.busicard.data.CardsContract;
import com.esguti.busicard.data.CardsDatabase;
import com.esguti.busicard.ui.CardAdapter;
import com.esguti.busicard.ui.CardListListener;

/**
 * An activity representing a list of cards. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CardDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CardListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, CardListListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean m_TwoPane;
    private RecyclerView m_RecyclerView;
    private CardAdapter m_Adapter;
    private Context m_Context;
    private CardDetailFragment m_CardDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);
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
}
