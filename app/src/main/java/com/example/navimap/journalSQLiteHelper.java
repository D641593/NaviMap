package com.example.navimap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class journalSQLiteHelper extends SQLiteOpenHelper {

    private final static int _DBVersion = 1; //版本
    private final static String _DBName = "JournalList.db";// 資料庫 name

    final static String _TableName = "JournalNote";// 資料表 name
    final static String _ID = "_id";
    final static String TITLE = "_TITLE";
    final static String CONTENT = "_CONTENT";

    public journalSQLiteHelper(Context context) {
        super(context, _DBName, null, _DBVersion); //給 SQLiteOpenHelper construct
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_TITLE VARCHAR(200) NOT NULL, " +
                "_CONTENT TEXT" +
                ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        final String SQL = "DROP TABLE " + _TableName;
        db.execSQL(SQL);
        onCreate(db);
    }
}
