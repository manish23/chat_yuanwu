package com.ihs.demo.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wuchen on 15-9-11.
 */
public class ManagerMessage extends SQLiteOpenHelper{
    private static ManagerMessage managerMessage = null;
    private static final String DATA_BASE = "message.db";
    private static final String TABLE_NAME = "message";
    private static final String Mid = "Mid";
    private static final String Msgid = "MsgID";
    private static final int DATA_VERSION = 1;

    public ManagerMessage(Context context){
        super(context, DATA_BASE, null, DATA_VERSION);
    }
    public ManagerMessage getInstance(Context context){
        if (managerMessage == null){
            managerMessage = new ManagerMessage(context);
        }
        return managerMessage;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE" + TABLE_NAME + "(" + Msgid + "msgid," + Mid + "tomid);";

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS" + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }
    public long insert(String msgid, String tomid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Msgid, msgid);
        contentValues.put(Mid, tomid);
        long raw = db.insert(TABLE_NAME, null, contentValues);
        return raw;
    }
    public void deleteMid(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = Mid + "=?";
        String[] whereValue = {id};
        db.delete(TABLE_NAME, where, whereValue);
    }
    public void deleteMsgid(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = Msgid + "=?";
        String[] whereValue = {id};
        db.delete(TABLE_NAME, where, whereValue);
    }
    public Cursor select(String id){
        SQLiteDatabase database = this.getReadableDatabase();
        String where = Mid + "=?";
        String[] whereValue = {id};
        Cursor cursor = database.rawQuery("select * from message where Mid=?", whereValue);
        return cursor;
    }
}
