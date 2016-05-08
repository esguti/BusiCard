package com.esguti.busicard.ui;

import com.esguti.busicard.data.CardsDatabase;

/**
 * Created by esguti on 08.05.16.
 */
public interface CardListListener {
    void updateList();
    void changeCard(long cardId);
    CardsDatabase getDatabase();
}
