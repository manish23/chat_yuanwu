package com.ihs.demo.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ihs.commons.utils.HSLog;

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
    private SQLiteDatabase sqLiteDatabase;

    public ManagerMessage(Context context){
        super(context, DATA_BASE, null, DATA_VERSION);
        try{
            sqLiteDatabase = getWritableDatabase();
        }catch (Exception e){
            sqLiteDatabase = getReadableDatabase();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HSLog.e("Create DB:", "284694697");
        try{

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + Msgid + " text ," + Mid + " text );");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS" + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }
    public synchronized void insert(String msgid, String tomid){

//        String sql = "insert into message(MsgID, Mid) values (" + msgid + "," + tomid + ");";
//        db.execSQL(sql);
        ContentValues contentValues = new ContentValues();
        contentValues.put("MsgID", msgid);
        contentValues.put("Mid", tomid);
        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
    }
    public synchronized void deleteMid(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = Mid + "=?";
        String[] whereValue = {id};
        db.delete(TABLE_NAME, where, whereValue);
    }
    public synchronized void deleteMsgid(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = Msgid + "=?";
        String[] whereValue = {id};
        db.delete(TABLE_NAME, where, whereValue);
    }
    public synchronized Cursor select(String id){
        SQLiteDatabase database = this.getReadableDatabase();
        String where = Mid + "=?";
        String[] whereValue = {id};
        Cursor cursor = database.rawQuery("select * from message where Mid=?", whereValue);
        return cursor;
    }
}
