package com.esguti.busicard;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.IOException;

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
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri m_photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_card_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.card_detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_share = (FloatingActionButton) findViewById(R.id.card_detail_fab_share);
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photo = new File(Environment.getExternalStorageDirectory(),"Pic.jpg");
                    m_photoUri = Uri.fromFile(photo);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

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
                    fillImage(data);
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

    private void fillCard(String contents){
        VCard vcard = Ezvcard.parse(contents).first();

        TextView txt_card_detail_company = (TextView) findViewById(R.id.card_detail_company_name);
        TextView txt_card_detail_email = (TextView) findViewById(R.id.card_detail_email);
        TextView txt_card_detail_location = (TextView) findViewById(R.id.card_detail_location);
        TextView txt_card_detail_telephone = (TextView) findViewById(R.id.card_detail_telephone);
        TextView txt_card_detail_name = (TextView) findViewById(R.id.card_detail_name);



        String name = "";
        if( vcard.getStructuredName() != null) {
            if( vcard.getStructuredName().getPrefixes().size() > 0 )
                for (String s : vcard.getStructuredName().getPrefixes())
                    if( s != "null"){ if(name != "") name += " "; name += s; }
            if( vcard.getStructuredName().getGiven()  != "null" ) if(name != "") name += " "; name += vcard.getStructuredName().getGiven();
            if( vcard.getStructuredName().getFamily() != "null" ) if(name != "") name += " "; name += vcard.getStructuredName().getFamily();
        }
        txt_card_detail_name.setText(name);
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) this.findViewById(R.id.card_detail_collapsing_toolbar);
        if (appBarLayout != null) appBarLayout.setTitle(name);

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

    private void fillImage(Intent data){
        Uri selectedImage = m_photoUri;
        getContentResolver().notifyChange(selectedImage, null);
        ImageView img_photo = (ImageView) findViewById(R.id.card_detail_photo);

        ContentResolver cr = getContentResolver();
        Bitmap imgtmp;
        try {
            imgtmp = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
            img_photo.setImageBitmap(imgtmp);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load tmp photo: " + e);
        }
    }
}