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
                CardsContract.Cards.PHOTO_URL
        };

        int _ID = 0;
        int COMAPANY = 1;
        int NAME = 2;
        int EMAIL = 3;
        int TELPHONE = 4;
        int ADDRESS = 5;
        int PHOTO_URL = 6;
    }
}