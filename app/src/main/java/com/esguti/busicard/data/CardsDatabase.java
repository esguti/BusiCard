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

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by esguti on 18.04.16.
 */
public class CardsDatabase extends SQLiteOpenHelper {
    private static final String LOG_TAG = CardsDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "busicard.db";
    private static final int DATABASE_VERSION = 1;
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
                + CardsContract.CardsColumns.PHOTO_URL + " TEXT NOT NULL,"
                + ")" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CARDS);
        onCreate(db);
    }

    /**
     * Updates the current picture for the card.
     *
     * @param cardId the identifier of the card for which to save the picture
     * @param picture the picture to save to the internal storage and save path in the database.
     */
    public void updateReportPicture(long cardId, Bitmap picture) {
        // Saves the new picture to the internal storage with the unique identifier of the card as
        // the name. That way, there will never be two report pictures with the same name.
        File internalStorage = m_Context.getDir("ReportPictures", Context.MODE_PRIVATE);
        File reportFilePath = new File(internalStorage, cardId + ".png");
        String picturePath = reportFilePath.toString();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(reportFilePath);
            picture.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fos);
            fos.close();
        }
        catch (Exception ex) {
            Log.w(LOG_TAG, "Problem updating picture", ex);
            picturePath = "";
        }

        // Updates the database entry for the report to point to the picture
        SQLiteDatabase db = getWritableDatabase();

        ContentValues newPictureValue = new ContentValues();
        newPictureValue.put(CardsContract.CardsColumns.PHOTO_URL, picturePath);

        db.update(Tables.CARDS,
                newPictureValue,
                CardsContract.CardsColumns._ID + "=?",
                new String[]{String.valueOf(cardId)});
    }


    /**
     * Gets the picture for the specified card in the database.
     *
     * @param cardId the identifier of the report for which to get the picture.
     *
     * @return the picture for the card, or null if no picture was found.
     */
    public Bitmap getCardPicture(long cardId) {
        String picturePath = getCardPicturePath(cardId);
        if (picturePath == null || picturePath.length() == 0)
            return (null);

        Bitmap reportPicture = BitmapFactory.decodeFile(picturePath);

        return (reportPicture);
    }

    public static Bitmap getCardPicture(Cursor cursor) {
        String picturePath = cursor.getString(cursor.getColumnIndex(CardsContract.CardsColumns.PHOTO_URL));

        Bitmap reportPicture = BitmapFactory.decodeFile(picturePath);

        return (reportPicture);
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
     * Deletes the specified card from the database, removing also the associated picture from the
     * internal storage if any.
     *
     * @param cardId the card to remove.
     */
    public void deleteCard(long cardId) {
        // Remove picture for report from internal storage
        String picturePath = getCardPicturePath(cardId); // See above
        if (picturePath != null && picturePath.length() != 0) {
            File cardFilePath = new File(picturePath);
            cardFilePath.delete();
        }

        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();

        db.delete(Tables.CARDS,
                CardsContract.CardsColumns._ID + "=?",
                new String[]{String.valueOf(cardId)});
    }
}
