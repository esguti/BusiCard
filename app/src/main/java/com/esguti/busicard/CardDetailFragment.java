package com.esguti.busicard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esguti.busicard.ui.CardDetailListener;
import com.esguti.busicard.ui.CardListListener;

/**
 * A fragment representing a single card detail screen.
 * This fragment is either contained in a {@link CardListActivity}
 * in two-pane mode (on tablets) or a {@link CardDetailActivity}
 * on handsets.
 */
public class CardDetailFragment extends Fragment{
    private static final String LOG_TAG = CardDetailFragment.class.getSimpleName();

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_CARD_ID = "card_id";

    private CardDetail m_CardDetail;
    private long m_cardId = CardDetail.CARD_ID_NULL;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CardDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_CARD_ID))
            m_cardId = getArguments().getLong(ARG_CARD_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_detail, container, false);
        m_CardDetail.setView(rootView);
        m_CardDetail.loadCard(m_cardId);
        m_CardDetail.addListeners();

        return rootView;
    }

    public void setListener(Activity activity, CardListListener listener){
        m_CardDetail = new CardDetail(activity, listener);
    }

    public void onActionResult(int requestCode, int resultCode, Intent data) {
        m_CardDetail.onActionResult(requestCode, resultCode, data);
    }
}
