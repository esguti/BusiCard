package com.esguti.busicard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.esguti.busicard.data.CardsContract;
import com.esguti.busicard.data.CardsDatabase;
import com.esguti.busicard.ui.CardDetailListener;
import com.esguti.busicard.ui.CardListListener;

/**
 * An activity representing a single card detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CardListActivity}.
 */
public class CardDetailActivity extends AppCompatActivity implements CardListListener{

    private CardDetail m_CardDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail);

        m_CardDetail = new CardDetail(this, this);
        m_CardDetail.setView(findViewById(R.id.card_detail));

        Toolbar toolbar = (Toolbar) findViewById(R.id.card_detail_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                long cardId = CardsContract.Cards.getCardId(getIntent().getData());
                m_CardDetail.loadCard(cardId);
            }
        }

        m_CardDetail.addListeners();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        m_CardDetail.onActionResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if ( m_CardDetail.getCardId() == CardDetail.CARD_ID_NULL ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String message = getString(R.string.card_detail_back_msg);
            String message_yes = getString(R.string.card_list_delete_msg_yes);
            String message_no = getString(R.string.card_list_delete_msg_no);
            builder
                    .setMessage(message)
                    .setPositiveButton(message_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            goBack();
                        }
                    })
                    .setNegativeButton(message_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        } else {
            m_CardDetail.updateAllTextFields();
            goBack();
        }
    }

    private void goBack() {
        super.onBackPressed();
        Intent intent = new Intent(this, CardListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, CardListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateList() {}

    @Override
    public void changeCard(long cardId) {

    }

    @Override
    public CardsDatabase getDatabase() {
        return new CardsDatabase(this);
    }
}