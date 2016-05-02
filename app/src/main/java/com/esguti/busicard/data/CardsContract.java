package com.esguti.busicard.data;

import android.net.Uri;

/**
 * Created by esguti on 18.04.16.
 */
public class CardsContract {
    public static final String CONTENT_AUTHORITY = "com.esguti.busicard";
    public static final Uri BASE_URI = Uri.parse("content://com.esguti.busicard");

    interface CardsColumns {
        /** Type: INTEGER PRIMARY KEY AUTOINCREMENT */
        String _ID = "_id";
        /** Type: TEXT */
        String COMPANY = "company";
        /** Type: TEXT */
        String NAME = "name";
        /** Type: TEXT */
        String TELEPHONE = "telephone";
        /** Type: TEXT */
        String EMAIL = "email";
        /** Type: TEXT */
        String ADDRESS = "address";
        /** Type: TEXT NOT NULL */
        String PHOTO_URL = "photo";}

    public static class Cards implements CardsColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.esguti.busicard.cards";
        public static final String CONTENT_CARD_TYPE = "vnd.android.cursor.card/vnd.com.esguti.busicard.cards";

        public static final String DEFAULT_SORT = NAME + " DESC";

        /** Matches: /cards/ */
        public static Uri buildDirUri() {
            return BASE_URI.buildUpon().appendPath("cards").build();
        }

        /** Matches: /cards/[_id]/ */
        public static Uri buildCardUri(long _id) {
            return BASE_URI.buildUpon().appendPath("cards").appendPath(Long.toString(_id)).build();
        }

        /** Read card ID card detail URI. */
        public static long getCardId(Uri cardUri) {
            return Long.parseLong(cardUri.getPathSegments().get(1));
        }
    }

    private CardsContract() {
    }
}
