package com.esguti.busicard.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.esguti.busicard.data.CardsProvider.Tables;
import com.esguti.busicard.img.ImageTools;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by esguti on 18.04.16.
 */
public class CardsDatabase extends SQLiteOpenHelper {
    private static final String LOG_TAG = CardsDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "busicard.db";
    private static final int DATABASE_VERSION = 2;
    private static final int IMG_SIZE_WIDTH = 480;
    private static final int IMG_SIZE_HEIGHT = 640;
    private static final int THM_SIZE_WIDTH = 50;
    private static final int THM_SIZE_HEIGHT = 50;
    private Context m_Context;

    public CardsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        m_Context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CARDS + " ("
                + CardsContract.CardsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CardsContract.CardsColumns.COMPANY + " TEXT,"
                + CardsContract.CardsColumns.NAME + " TEXT,"
                + CardsContract.CardsColumns.EMAIL + " TEXT,"
                + CardsContract.CardsColumns.TELEPHONE + " TEXT,"
                + CardsContract.CardsColumns.ADDRESS + " TEXT,"
                + CardsContract.CardsColumns.PHOTO_URL + " TEXT,"
                + CardsContract.CardsColumns.THM_URL + " TEXT"
                + ")" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CARDS);
        onCreate(db);
    }

    /**
     * Gets the text information from the specified card in the database.
     *
     * @param cardId the identifier of the card for which to get the picture.
     *
     * @return the card information, or an empty string if info is not found.
     */
    public String[] getCardText(long cardId) {
        String[] output = new String[CardLoader.Query.CARD_COLUMNS_NUMBER];

        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();

        // After the query, the cursor points to the first database row
        // returned by the request
        Cursor cardCursor = db.query(Tables.CARDS,
                null,
                CardsContract.CardsColumns._ID + "=?",
                new String[]{String.valueOf(cardId)},
                null,
                null,
                null);
        cardCursor.moveToNext();

        output[CardLoader.Query._ID] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns._ID));
        output[CardLoader.Query.COMPANY] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns.COMPANY));
        output[CardLoader.Query.ADDRESS] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns.ADDRESS));
        output[CardLoader.Query.EMAIL] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns.EMAIL));
        output[CardLoader.Query.NAME] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns.NAME));
        output[CardLoader.Query.TELEPHONE] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns.TELEPHONE));
        output[CardLoader.Query.PHOTO_URL] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns.PHOTO_URL));
        output[CardLoader.Query.THM_URL] = cardCursor.getString(cardCursor.getColumnIndex(CardsContract.CardsColumns.THM_URL));

        return output;
    }

        /**
         * Updates the current picture for the card.
         *
         * @param cardId the identifier of the card for which to save the picture
         * @param photoPath the picture path to save to the internal storage and save path in the database.
         */
    public void updateCardImage(long cardId, String photoPath) {
        // Saves the new picture to the internal storage with the unique identifier of the card as
        // the name. That way, there will never be two card pictures with the same name.
        File internalStorage = m_Context.getDir("CardPictures", Context.MODE_PRIVATE);
        File cardFilePath = new File(internalStorage, cardId + ".png");
        File cardThumbFilePath = new File(internalStorage, cardId + "_thm.png");
        String picturePath = cardFilePath.toString();
        String thumbnailPath = cardThumbFilePath.toString();

        try {
            FileOutputStream fosp = new FileOutputStream(cardFilePath);
            FileOutputStream fost = new FileOutputStream(thumbnailPath);
            Bitmap picture = ImageTools.decodeSampledBitmapFromFile(photoPath, IMG_SIZE_WIDTH, IMG_SIZE_HEIGHT);
            Bitmap thumbnail = ImageTools.decodeSampledBitmapFromFile(photoPath, THM_SIZE_WIDTH, THM_SIZE_HEIGHT);
            picture.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fosp);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fost);
            fosp.close();
            fost.close();

            // Updates the database entry for the card to point to the picture
            SQLiteDatabase db = getWritableDatabase();

            ContentValues newPictureValue = new ContentValues();
            newPictureValue.put(CardsContract.CardsColumns.PHOTO_URL, picturePath);
            db.update(Tables.CARDS,
                    newPictureValue,
                    CardsContract.CardsColumns._ID + "=?",
                    new String[]{String.valueOf(cardId)});

            ContentValues newThumbnailValue = new ContentValues();
            newThumbnailValue.put(CardsContract.CardsColumns.THM_URL, thumbnailPath);
            db.update(Tables.CARDS,
                    newThumbnailValue,
                    CardsContract.CardsColumns._ID + "=?",
                    new String[]{String.valueOf(cardId)});
        }
        catch (Exception ex) {
            Log.w(LOG_TAG, "Problem updating picture", ex);
        }
    }


    public void updateTextField(long cardId, String column_id, String text) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues newTextValue = new ContentValues();
        newTextValue.put(column_id, text);

        db.update(Tables.CARDS,
                newTextValue,
                CardsContract.CardsColumns._ID + "=?",
                new String[]{String.valueOf(cardId)});
    }

    /**
     * Gets the picture for the specified card in the database.
     *
     * @param cardId the identifier of the card for which to get the picture.
     *
     * @return the picture for the card, or null if no picture was found.
     */
    public Bitmap getCardPicture(long cardId) {
        String picturePath = getCardPicturePath(cardId);
        if (picturePath == null || picturePath.length() == 0)
            return (null);

        Bitmap cardPicture = BitmapFactory.decodeFile(picturePath);
        return (cardPicture);
    }

    public static Bitmap getCardPicture(Cursor cursor) {
        String picturePath = cursor.getString(cursor.getColumnIndex(CardsContract.CardsColumns.PHOTO_URL));

        Bitmap cardPicture = BitmapFactory.decodeFile(picturePath);

        return (cardPicture);
    }

    /**
     * Gets the thumbnail for the specified card in the database.
     *
     * @param cardId the identifier of the card for which to get the thumbnail.
     *
     * @return the thumbnail for the card, or null if no thumbnail was found.
     */
    public Bitmap getCardThumbnail(long cardId) {
        String thumbnailPath = getCardThumbnailPath(cardId);
        if (thumbnailPath == null || thumbnailPath.length() == 0)
            return (null);

        Bitmap cardThumbnail = BitmapFactory.decodeFile(thumbnailPath);
        return (cardThumbnail);
    }

    public static Bitmap getCardThumbnail(Cursor cursor) {
        String thumbnailPath = cursor.getString(cursor.getColumnIndex(CardsContract.CardsColumns.THM_URL));

        Bitmap cardThumbnail = BitmapFactory.decodeFile(thumbnailPath);

        return (cardThumbnail);
    }
    
    /**
     * Gets the path of the picture for the specified card in the database.
     *
     * @param cardId the identifier of the card for which to get the picture.
     *
     * @return the picture for the card, or null if no picture was found.
     */
    private String getCardPicturePath(long cardId) {
        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();

        // After the query, the cursor points to the first database row
        // returned by the request
        Cursor cardCursor = db.query(Tables.CARDS,
                null,
                CardsContract.CardsColumns._ID + "=?",
                new String[]{String.valueOf(cardId)},
                null,
                null,
                null);
        cardCursor.moveToNext();

        // Get the path of the picture from the database row pointed by
        // the cursor using the getColumnIndex method of the cursor.
        String picturePath = cardCursor.getString(cardCursor.
                getColumnIndex(CardsContract.CardsColumns.PHOTO_URL));

        return (picturePath);
    }

    /**
     * Gets the path of the thumbnail for the specified card in the database.
     *
     * @param cardId the identifier of the card for which to get the thumbnail.
     *
     * @return the thumbnail for the card, or null if no thumbnail was found.
     */
    private String getCardThumbnailPath(long cardId) {
        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();

        // After the query, the cursor points to the first database row
        // returned by the request
        Cursor cardCursor = db.query(Tables.CARDS,
                null,
                CardsContract.CardsColumns._ID + "=?",
                new String[]{String.valueOf(cardId)},
                null,
                null,
                null);
        cardCursor.moveToNext();

        // Get the path of the thumbnail from the database row pointed by
        // the cursor using the getColumnIndex method of the cursor.
        String thumbnailPath = cardCursor.getString(cardCursor.
                getColumnIndex(CardsContract.CardsColumns.THM_URL));

        return (thumbnailPath);
    }
    
    /**
     * Deletes the specified card from the database, removing also the associated picture from the
     * internal storage if any.
     *
     * @param cardId the card to remove.
     */
    public void deleteCard(long cardId) {
        // Remove picture for card from internal storage
        String picturePath = getCardPicturePath(cardId); // See above
        if (picturePath != null && picturePath.length() != 0) {
            File cardFilePath = new File(picturePath);
            cardFilePath.delete();
        }

        // Remove thumbnail for card from internal storage
        String thumbnailPath = getCardThumbnailPath(cardId); // See above
        if (thumbnailPath != null && thumbnailPath.length() != 0) {
            File cardFilePath = new File(thumbnailPath);
            cardFilePath.delete();
        }
        
        // Remove the card from the database
        SQLiteDatabase db = getWritableDatabase();

        db.delete(Tables.CARDS,
                CardsContract.CardsColumns._ID + "=?",
                new String[]{String.valueOf(cardId)});
    }
}
