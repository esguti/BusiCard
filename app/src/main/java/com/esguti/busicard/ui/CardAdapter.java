package com.esguti.busicard.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.esguti.busicard.R;
import com.esguti.busicard.data.CardLoader;
import com.esguti.busicard.data.CardsContract;
import com.esguti.busicard.data.CardsDatabase;

/**
 * Created by esguti on 08.05.16.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private CardsDatabase m_CardsDatabase;
    private Cursor m_Cursor;
    private Context m_Context;
    private Listener m_listener;

    public CardAdapter(Context context) {
        m_Context = context;
        m_listener = (Listener) context;
        m_CardsDatabase = new CardsDatabase(m_Context);
    }

    @Override
    public long getItemId(int position) {
        m_Cursor.moveToPosition(position);
        return m_Cursor.getLong(CardLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_list_content, parent, false);
        final ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_Context.startActivity(new Intent(Intent.ACTION_VIEW,
                        CardsContract.Cards.buildCardUri(getItemId(vh.getAdapterPosition()))));
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                deleteCardItem(vh);
                return true;
            }
        });

        return vh;
    }

    private void deleteCardItem(final ViewHolder vh){
        AlertDialog.Builder builder = new AlertDialog.Builder(m_Context);
        String message = m_Context.getString(R.string.card_list_delete_msg);
        String message_yes = m_Context.getString(R.string.card_list_delete_msg_yes);
        String message_no = m_Context.getString(R.string.card_list_delete_msg_no);
        builder
                .setMessage(message)
                .setPositiveButton(message_yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        m_CardsDatabase.deleteCard(getItemId(vh.getAdapterPosition()));
                        m_listener.updateList();
                    }
                })
                .setNegativeButton(message_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        m_Cursor.moveToPosition(position);
        holder.titleView.setText(m_Cursor.getString(CardLoader.Query.NAME));
        holder.subtitleView.setText(
                m_Cursor.getString(CardLoader.Query.COMPANY)
                        + m_Cursor.getString(CardLoader.Query.EMAIL));
        holder.thumbnailView.setImageBitmap(CardsDatabase.getCardThumbnail(m_Cursor));
    }

    @Override
    public int getItemCount() {
        if (m_Cursor != null)
            return m_Cursor.getCount();
        return 0;
    }

    public void swapCursor(Cursor nuevoCursor) {
        if (nuevoCursor != null) {
            m_Cursor = nuevoCursor;
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.card_list_content_thumb);
            titleView = (TextView) view.findViewById(R.id.card_list_content_name);
            subtitleView = (TextView) view.findViewById(R.id.card_list_content_company);
        }
    }

}