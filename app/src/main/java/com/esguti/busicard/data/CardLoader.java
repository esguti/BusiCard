package com.esguti.busicard.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by esguti on 18.04.16.
 */
public class CardLoader extends CursorLoader {
    public static CardLoader newAllCardsInstance(Context context) {
        return new CardLoader(context, CardsContract.Cards.buildDirUri());
    }

    public static CardLoader newInstanceForCardId(Context context, long itemId) {
        return new CardLoader(context, CardsContract.Cards.buildCardUri(itemId));
    }

    private CardLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, CardsContract.Cards.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = {
                CardsContract.Cards._ID,
                CardsContract.Cards.COMPANY,
                CardsContract.Cards.NAME,
                CardsContract.Cards.EMAIL,
                CardsContract.Cards.TELEPHONE,
                CardsContract.Cards.ADDRESS,
                CardsContract.Cards.PHOTO_URL,
                CardsContract.Cards.THM_URL
        };

        int _ID = 0;
        int COMPANY = _ID+1;
        int NAME = COMPANY+1;
        int EMAIL = NAME+1;
        int TELEPHONE = EMAIL+1;
        int ADDRESS = TELEPHONE+1;
        int PHOTO_URL = ADDRESS+1;
        int THM_URL = PHOTO_URL +1;
        int CARD_COLUMNS_NUMBER = THM_URL + 1;
    }
}
