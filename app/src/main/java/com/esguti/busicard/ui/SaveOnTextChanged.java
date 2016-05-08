package com.esguti.busicard.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.esguti.busicard.data.CardsDatabase;

/**
 * Created by esguti on 07.05.16.
 */
public class SaveOnTextChanged implements TextWatcher {
    long m_card_id;
    CardsDatabase m_db;
    String m_column_id;
    EditText m_txt;

    public SaveOnTextChanged(EditText txt, long card_id, String column_id, CardsDatabase db){
        m_txt = txt;
        m_card_id = card_id;
        m_column_id = column_id;
        m_db = db;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if( s.hashCode() != m_txt.hashCode() ) {
            m_db.updateTextField(m_card_id, m_column_id, s.toString());
        }
    }
}
