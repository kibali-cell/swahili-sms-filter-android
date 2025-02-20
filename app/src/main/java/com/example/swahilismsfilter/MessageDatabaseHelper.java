package com.example.swahilismsfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MessageHistory";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_IS_SPAM = "is_spam";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_MESSAGES + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_SENDER + " text not null, "
            + COLUMN_MESSAGE + " text not null, "
            + COLUMN_IS_SPAM + " integer not null, "
            + COLUMN_TIMESTAMP + " datetime default current_timestamp);";

    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public long addMessage(String sender, String message, boolean isSpam) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_IS_SPAM, isSpam ? 1 : 0);
        return db.insert(TABLE_MESSAGES, null, values);
    }

    public Cursor getAllMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MESSAGES, null, null, null, null, null,
                COLUMN_TIMESTAMP + " DESC");
    }

    public void clearAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("messages", null, null);
    }
}