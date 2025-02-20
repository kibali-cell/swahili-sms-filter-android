package com.example.swahilismsfilter;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MessageHistoryAdapter extends RecyclerView.Adapter<MessageHistoryAdapter.ViewHolder> {
    private Cursor cursor;
    private SimpleDateFormat dateFormat;

    public MessageHistoryAdapter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            String sender = cursor.getString(cursor.getColumnIndexOrThrow(MessageDatabaseHelper.COLUMN_SENDER));
            String message = cursor.getString(cursor.getColumnIndexOrThrow(MessageDatabaseHelper.COLUMN_MESSAGE));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MessageDatabaseHelper.COLUMN_TIMESTAMP));
            boolean isSpam = cursor.getInt(cursor.getColumnIndexOrThrow(MessageDatabaseHelper.COLUMN_IS_SPAM)) == 1;

            holder.senderText.setText(sender);
            holder.messageText.setText(message);
            holder.timestampText.setText(timestamp);

            if (isSpam) {
                holder.spamStatusText.setText("SPAM");
                holder.spamStatusText.setTextColor(holder.itemView.getContext()
                        .getResources().getColor(android.R.color.holo_red_dark));
            } else {
                holder.spamStatusText.setText("HAM");
                holder.spamStatusText.setTextColor(holder.itemView.getContext()
                        .getResources().getColor(android.R.color.holo_green_dark));
            }
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderText;
        TextView messageText;
        TextView timestampText;
        TextView spamStatusText;

        ViewHolder(View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.senderText);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
            spamStatusText = itemView.findViewById(R.id.spamStatusText);
        }
    }
}