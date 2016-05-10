package com.esguti.busicard;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.esguti.busicard.data.CardLoader;
import com.esguti.busicard.data.CardsContract;
import com.esguti.busicard.data.CardsDatabase;
import com.esguti.busicard.data.CardsProvider;
import com.esguti.busicard.ui.CardDetailListener;
import com.esguti.busicard.ui.CardListListener;
import com.esguti.busicard.ui.SaveOnTextChanged;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

/**
 * Created by esguti on 08.05.16.
 */
public class CardDetail {
    private static final String LOG_TAG = CardDetail.class.getSimpleName();
    public static final int CARD_ID_NULL = -1;

    private CardsDatabase m_CardsDatabase;
    private CardListListener m_listener;
    private Activity m_activity;
    private View m_view = null;
    private CollapsingToolbarLayout m_appBarLayout;

    private long m_CardId = CARD_ID_NULL;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String m_photoPath = null;


    public CardDetail(Activity activity, CardListListener listener) {
        m_activity = activity;
        m_listener = listener;
        m_CardsDatabase = listener.getDatabase();
    }

    public void setView(View view){
        m_view = view;
        m_appBarLayout = (CollapsingToolbarLayout) m_view.findViewById(R.id.card_detail_collapsing_toolbar);
    }

    public void updateAllTextFields(){
        if( m_CardId != CARD_ID_NULL ) {
            EditText txt_card_detail_company = (EditText) m_view.findViewById(R.id.card_detail_company_name);
            EditText txt_card_detail_email = (EditText) m_view.findViewById(R.id.card_detail_email);
            EditText txt_card_detail_location = (EditText) m_view.findViewById(R.id.card_detail_location);
            EditText txt_card_detail_telephone = (EditText) m_view.findViewById(R.id.card_detail_telephone);
            EditText txt_card_detail_name = (EditText) m_view.findViewById(R.id.card_detail_name);

            m_CardsDatabase.updateTextField(m_CardId, CardsContract.Cards.COMPANY, txt_card_detail_company.getText().toString());
            m_CardsDatabase.updateTextField(m_CardId, CardsContract.Cards.EMAIL, txt_card_detail_email.getText().toString());
            m_CardsDatabase.updateTextField(m_CardId, CardsContract.Cards.ADDRESS, txt_card_detail_location.getText().toString());
            m_CardsDatabase.updateTextField(m_CardId, CardsContract.Cards.TELEPHONE, txt_card_detail_telephone.getText().toString());
            m_CardsDatabase.updateTextField(m_CardId, CardsContract.Cards.NAME, txt_card_detail_name.getText().toString());
        }
    }

    public void addListeners(){
        FloatingActionButton fab_share = (FloatingActionButton) m_view.findViewById(R.id.card_detail_fab_share);
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareCard();
            }
        });
        FloatingActionButton fab_scan = (FloatingActionButton) m_view.findViewById(R.id.card_detail_fab_scan);
        fab_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(m_activity);
                scanIntegrator.initiateScan();
            }
        });
        FloatingActionButton fab_photo = (FloatingActionButton) m_view.findViewById(R.id.card_detail_fab_photo);
        fab_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { takePicture(); }
        });
        EditText txt_card_detail_company = (EditText) m_view.findViewById(R.id.card_detail_company_name);
        txt_card_detail_company.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_company, m_CardId, CardsContract.Cards.COMPANY, m_CardsDatabase));
        EditText txt_card_detail_email = (EditText) m_view.findViewById(R.id.card_detail_email);
        txt_card_detail_email.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_email, m_CardId, CardsContract.Cards.EMAIL, m_CardsDatabase));
        EditText txt_card_detail_location = (EditText) m_view.findViewById(R.id.card_detail_location);
        txt_card_detail_location.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_location, m_CardId, CardsContract.Cards.ADDRESS, m_CardsDatabase));
        EditText txt_card_detail_telephone = (EditText) m_view.findViewById(R.id.card_detail_telephone);
        txt_card_detail_telephone.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_telephone, m_CardId, CardsContract.Cards.TELEPHONE, m_CardsDatabase));
        EditText txt_card_detail_name = (EditText) m_view.findViewById(R.id.card_detail_name);
        txt_card_detail_name.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_name, m_CardId, CardsContract.Cards.NAME, m_CardsDatabase));
        ImageView img_photo = (ImageView) m_view.findViewById(R.id.card_detail_photo);
    }

    private void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(m_activity.getPackageManager()) != null) {
            File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            m_photoPath = photo.getPath();
            m_activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void shareCard() {
        EditText txt_card_detail_company = (EditText) m_view.findViewById(R.id.card_detail_company_name);
        EditText txt_card_detail_email = (EditText) m_view.findViewById(R.id.card_detail_email);
        EditText txt_card_detail_location = (EditText) m_view.findViewById(R.id.card_detail_location);
        EditText txt_card_detail_telephone = (EditText) m_view.findViewById(R.id.card_detail_telephone);
        EditText txt_card_detail_name = (EditText) m_view.findViewById(R.id.card_detail_name);
        String vcard =
                "BEGIN:VCARD\r\n" +
                        "VERSION:3.0\r\n" +
                        "N:" + txt_card_detail_name.getText().toString() + "\r\n" +
                        "TEL:" + txt_card_detail_telephone.getText().toString() + "\r\n" +
                        "ADR:" + txt_card_detail_location.getText().toString() + "\r\n" +
                        "ORG:" + txt_card_detail_company.getText().toString() + "\r\n" +
                        "EMAIL:" + txt_card_detail_email.getText().toString() + "\r\n" +
                        "END:VCARD\r\n";

        Intent share = new Intent();
        share.setAction(android.content.Intent.ACTION_VIEW);
        File vcfFile = new File(m_activity.getExternalFilesDir(null), "tmp.vcf");
        try {
            FileWriter fw = new FileWriter(vcfFile);
            fw.write(vcard);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.setDataAndType(Uri.fromFile(vcfFile), "text/vcard");
        m_activity.startActivity(share);
    }

    public void onActionResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "Return from intent!!! ");

        switch (requestCode){
            case IntentIntegrator.REQUEST_CODE:
                Log.d(LOG_TAG, "Request Scan: ");
                if (resultCode == Activity.RESULT_OK) {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (scanResult == null) {
                        Log.d(LOG_TAG, "Scan is NULL");
                    }else{
                        String contents = scanResult.getContents();
                        Log.d(LOG_TAG, "contents: " + contents);
                        fillCard(contents);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Handle cancel
                    Log.d(LOG_TAG, "Result canceled");
                }else {
                    Log.d(LOG_TAG, "Unkown code: " + resultCode);
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                Log.d(LOG_TAG, "Request Image Capture: ");
                if (resultCode == Activity.RESULT_OK) {
                    saveImage();
                    Log.d(LOG_TAG, "Result is OK");
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Handle cancel
                    Log.d(LOG_TAG, "Result canceled");
                }else {
                    Log.d(LOG_TAG, "Unkown code: " + resultCode);
                }
                break;
            default:
                Log.d(LOG_TAG, "Request code not recognized: " + requestCode);
        }// end switch intent type
    }

    public long getCardId(){ return m_CardId; }

    public boolean loadCard(long cardId){
        if( cardId != CARD_ID_NULL){
            m_CardId = cardId;
            EditText txt_card_detail_company = (EditText) m_view.findViewById(R.id.card_detail_company_name);
            EditText txt_card_detail_email = (EditText) m_view.findViewById(R.id.card_detail_email);
            EditText txt_card_detail_location = (EditText) m_view.findViewById(R.id.card_detail_location);
            EditText txt_card_detail_telephone = (EditText) m_view.findViewById(R.id.card_detail_telephone);
            EditText txt_card_detail_name = (EditText) m_view.findViewById(R.id.card_detail_name);
            ImageView img_photo = (ImageView) m_view.findViewById(R.id.card_detail_photo);

            String [] cardTextInfo = m_CardsDatabase.getCardText(m_CardId);
            txt_card_detail_company.setText(cardTextInfo[CardLoader.Query.COMPANY]);
            txt_card_detail_email.setText(cardTextInfo[CardLoader.Query.EMAIL]);
            txt_card_detail_location.setText(cardTextInfo[CardLoader.Query.ADDRESS]);
            txt_card_detail_name.setText(cardTextInfo[CardLoader.Query.NAME]);
            if (m_appBarLayout != null) m_appBarLayout.setTitle(cardTextInfo[CardLoader.Query.NAME]);
            txt_card_detail_telephone.setText(cardTextInfo[CardLoader.Query.TELEPHONE]);
            if( m_CardsDatabase.getCardPicture(m_CardId) != null )
                img_photo.setImageBitmap(m_CardsDatabase.getCardPicture(m_CardId));
            return true;
        }
        return false;
    }

    private void fillCard(String contents){
        VCard vcard = Ezvcard.parse(contents).first();

        EditText txt_card_detail_company = (EditText) m_view.findViewById(R.id.card_detail_company_name);
        EditText txt_card_detail_email = (EditText) m_view.findViewById(R.id.card_detail_email);
        EditText txt_card_detail_location = (EditText) m_view.findViewById(R.id.card_detail_location);
        EditText txt_card_detail_telephone = (EditText) m_view.findViewById(R.id.card_detail_telephone);
        EditText txt_card_detail_name = (EditText) m_view.findViewById(R.id.card_detail_name);



        String name = "";
        if( vcard.getStructuredName() != null) {
            if (vcard.getStructuredName().getPrefixes().size() > 0)
                for (String s : vcard.getStructuredName().getPrefixes())
                    if (s != "null") {
                        if (name != "") name += " ";
                        name += s;
                    }
            if (vcard.getStructuredName().getGiven() != null){
                if (name != "") name += " ";
                name += vcard.getStructuredName().getGiven();
            }
            if( vcard.getStructuredName().getFamily() != null ){
                if(name != "") name += " ";
                name += vcard.getStructuredName().getFamily();
            }
        }
        txt_card_detail_name.setText(name);
        if (m_appBarLayout != null) m_appBarLayout.setTitle(name);

        String org = "";
        for (String s : vcard.getOrganization().getValues()){ if(org != "") org += ", "; org += s; }
        txt_card_detail_company.setText(org);

        String email = "";
        for (Email s : vcard.getEmails()) { if(email != "") email += ", "; email += s.getValue(); }
        txt_card_detail_email.setText(email);

        String addr = "";
        for (Address s : vcard.getAddresses()) { if (addr != "") addr += ", "; addr += s.getStreetAddressFull(); }
        txt_card_detail_location.setText(addr);

        String tlf = "";
        for (Telephone s : vcard.getTelephoneNumbers() ){ if(tlf != "") tlf += ", "; tlf += s.getText(); }
        txt_card_detail_telephone.setText( tlf );
    }

    private void saveImage(){
        ImageView img_photo = (ImageView) m_view.findViewById(R.id.card_detail_photo);

        if( m_CardId == CARD_ID_NULL ) { m_CardId = saveCard(); }

        if (m_CardId != CARD_ID_NULL && m_photoPath != null) {
            m_CardsDatabase.updateCardImage(m_CardId, m_photoPath);
            try {
                img_photo.setImageBitmap(m_CardsDatabase.getCardPicture(m_CardId));
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to load photo: " + e);
            }
            m_listener.updateList();
        }else{
            Log.e(LOG_TAG, "Error inserting in Database");
        }
    }

    private long saveCard(){
        SQLiteDatabase database = m_CardsDatabase.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(CardsContract.Cards.COMPANY, ((EditText) m_view.findViewById(R.id.card_detail_company_name)).getText().toString());
        contentValues.put(CardsContract.Cards.EMAIL, ((EditText) m_view.findViewById(R.id.card_detail_email)).getText().toString());
        contentValues.put(CardsContract.Cards.ADDRESS, ((EditText) m_view.findViewById(R.id.card_detail_location)).getText().toString());
        contentValues.put(CardsContract.Cards.TELEPHONE, ((EditText) m_view.findViewById(R.id.card_detail_telephone)).getText().toString());
        contentValues.put(CardsContract.Cards.NAME, ((EditText) m_view.findViewById(R.id.card_detail_name)).getText().toString());
        long id = database.insert(CardsProvider.Tables.CARDS, null, contentValues);
        database.close();

        return id;
    }
}

