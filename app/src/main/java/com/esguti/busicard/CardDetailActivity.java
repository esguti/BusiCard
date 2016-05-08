package com.esguti.busicard;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.esguti.busicard.data.CardLoader;
import com.esguti.busicard.data.CardsContract;
import com.esguti.busicard.data.CardsDatabase;
import com.esguti.busicard.data.CardsProvider;
import com.esguti.busicard.ui.SaveOnTextChanged;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

/**
 * An activity representing a single card detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CardListActivity}.
 */
public class CardDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = CardDetailActivity.class.getSimpleName();
    CollapsingToolbarLayout m_appBarLayout = null;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String m_photoPath = null;

    private CardsDatabase m_CardsDatabase;
    private long m_CardId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_card_detail);

        m_CardsDatabase = new CardsDatabase(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.card_detail_toolbar);
        setSupportActionBar(toolbar);

        m_appBarLayout = (CollapsingToolbarLayout) this.findViewById(R.id.card_detail_collapsing_toolbar);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                m_CardId = CardsContract.Cards.getCardId(getIntent().getData());
                loadCard();
            }
        }

        addListeners();
    }

    private void addListeners(){
        FloatingActionButton fab_share = (FloatingActionButton) findViewById(R.id.card_detail_fab_share);
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareCard();
            }
        });
        FloatingActionButton fab_scan = (FloatingActionButton) findViewById(R.id.card_detail_fab_scan);
        fab_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(CardDetailActivity.this);
                scanIntegrator.initiateScan();
            }
        });
        FloatingActionButton fab_photo = (FloatingActionButton) findViewById(R.id.card_detail_fab_photo);
        fab_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { takePicture(); }
        });
        EditText txt_card_detail_company = (EditText) findViewById(R.id.card_detail_company_name);
        txt_card_detail_company.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_company, m_CardId, CardsContract.Cards.COMPANY, m_CardsDatabase));
        EditText txt_card_detail_email = (EditText) findViewById(R.id.card_detail_email);
        txt_card_detail_email.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_email, m_CardId, CardsContract.Cards.EMAIL, m_CardsDatabase));
        EditText txt_card_detail_location = (EditText) findViewById(R.id.card_detail_location);
        txt_card_detail_location.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_location, m_CardId, CardsContract.Cards.ADDRESS, m_CardsDatabase));
        EditText txt_card_detail_telephone = (EditText) findViewById(R.id.card_detail_telephone);
        txt_card_detail_telephone.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_telephone, m_CardId, CardsContract.Cards.TELEPHONE, m_CardsDatabase));
        EditText txt_card_detail_name = (EditText) findViewById(R.id.card_detail_name);
        txt_card_detail_name.addTextChangedListener(
                new SaveOnTextChanged(txt_card_detail_name, m_CardId, CardsContract.Cards.NAME, m_CardsDatabase));
        ImageView img_photo = (ImageView) findViewById(R.id.card_detail_photo);
    }

    void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            m_photoPath = photo.getPath();
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    void shareCard() {
        EditText txt_card_detail_company = (EditText) findViewById(R.id.card_detail_company_name);
        EditText txt_card_detail_email = (EditText) findViewById(R.id.card_detail_email);
        EditText txt_card_detail_location = (EditText) findViewById(R.id.card_detail_location);
        EditText txt_card_detail_telephone = (EditText) findViewById(R.id.card_detail_telephone);
        EditText txt_card_detail_name = (EditText) findViewById(R.id.card_detail_name);
        String vcard =
                "BEGIN:VCARD\r\n" +
                "VERSION:3.0\r\n" +
                        "N:" + txt_card_detail_name.getText().toString() + "\r\n" +
                        "TEL:" + txt_card_detail_telephone.getText().toString() + "\r\n" +
                        "ADR:" + txt_card_detail_location.getText().toString() + "\r\n" +
                        "ORG:" + txt_card_detail_company.getText().toString() + "\r\n" +
                        "EMAIL:" + txt_card_detail_email.getText().toString() + "\r\n" +
                        "END:VCARD\r\n";
//        Intent share = new Intent(Intent.ACTION_SEND);
//        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        share.setType("text/x-vcard");
//        share.putExtra(Intent.EXTRA_TEXT, vcard);
//        startActivity(share);

        Intent share = new Intent();
        share.setAction(android.content.Intent.ACTION_VIEW);
        File vcfFile = new File(this.getExternalFilesDir(null), "tmp.vcf");
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
        startActivity(share);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "Return from intent!!! ");

        switch (requestCode){
            case IntentIntegrator.REQUEST_CODE:
                Log.d(LOG_TAG, "Request Scan: ");
                if (resultCode == RESULT_OK) {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (scanResult == null) {
                        Log.d(LOG_TAG, "Scan is NULL");
                    }else{
                        String contents = scanResult.getContents();
                        Log.d(LOG_TAG, "contents: " + contents);
                        fillCard(contents);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // Handle cancel
                    Log.d(LOG_TAG, "Result canceled");
                }else {
                    Log.d(LOG_TAG, "Unkown code: " + resultCode);
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                Log.d(LOG_TAG, "Request Image Capture: ");
                if (resultCode == RESULT_OK) {
                    saveImage();
                    Log.d(LOG_TAG, "Result is OK");
                } else if (resultCode == RESULT_CANCELED) {
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

    private void loadCard(){
        if( m_CardId != -1){
            EditText txt_card_detail_company = (EditText) findViewById(R.id.card_detail_company_name);
            EditText txt_card_detail_email = (EditText) findViewById(R.id.card_detail_email);
            EditText txt_card_detail_location = (EditText) findViewById(R.id.card_detail_location);
            EditText txt_card_detail_telephone = (EditText) findViewById(R.id.card_detail_telephone);
            EditText txt_card_detail_name = (EditText) findViewById(R.id.card_detail_name);
            ImageView img_photo = (ImageView) findViewById(R.id.card_detail_photo);

            String [] cardTextInfo = m_CardsDatabase.getCardText(m_CardId);
            txt_card_detail_company.setText(cardTextInfo[CardLoader.Query.COMPANY]);
            txt_card_detail_email.setText(cardTextInfo[CardLoader.Query.EMAIL]);
            txt_card_detail_location.setText(cardTextInfo[CardLoader.Query.ADDRESS]);
            txt_card_detail_name.setText(cardTextInfo[CardLoader.Query.NAME]);
            if (m_appBarLayout != null) m_appBarLayout.setTitle(cardTextInfo[CardLoader.Query.NAME]);
            txt_card_detail_telephone.setText(cardTextInfo[CardLoader.Query.TELEPHONE]);
            img_photo.setImageBitmap(m_CardsDatabase.getCardPicture(m_CardId));
        }
    }

    private void fillCard(String contents){
        VCard vcard = Ezvcard.parse(contents).first();

        EditText txt_card_detail_company = (EditText) findViewById(R.id.card_detail_company_name);
        EditText txt_card_detail_email = (EditText) findViewById(R.id.card_detail_email);
        EditText txt_card_detail_location = (EditText) findViewById(R.id.card_detail_location);
        EditText txt_card_detail_telephone = (EditText) findViewById(R.id.card_detail_telephone);
        EditText txt_card_detail_name = (EditText) findViewById(R.id.card_detail_name);



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
        ImageView img_photo = (ImageView) findViewById(R.id.card_detail_photo);

        if( m_CardId == -1 ) {
            m_CardId = saveCard();
        }

        if (m_CardId != -1 && m_photoPath != null) {
            m_CardsDatabase.updateCardImage(m_CardId, m_photoPath);
            try {
                img_photo.setImageBitmap(m_CardsDatabase.getCardPicture(m_CardId));
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to load photo: " + e);
            }
        }else{
            Log.e(LOG_TAG, "Error inserting in Database");
        }
    }

    private long saveCard(){
        SQLiteDatabase database = m_CardsDatabase.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(CardsContract.Cards.COMPANY, ((EditText) findViewById(R.id.card_detail_company_name)).getText().toString());
        contentValues.put(CardsContract.Cards.EMAIL, ((EditText) findViewById(R.id.card_detail_email)).getText().toString());
        contentValues.put(CardsContract.Cards.ADDRESS, ((EditText) findViewById(R.id.card_detail_location)).getText().toString());
        contentValues.put(CardsContract.Cards.TELEPHONE, ((EditText) findViewById(R.id.card_detail_telephone)).getText().toString());
        contentValues.put(CardsContract.Cards.NAME, ((EditText) findViewById(R.id.card_detail_name)).getText().toString());
        long id = database.insert(CardsProvider.Tables.CARDS, null, contentValues);
        database.close();

        return id;
    }
}