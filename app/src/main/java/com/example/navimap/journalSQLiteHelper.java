package com.example.navimap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class journalSQLiteHelper extends SQLiteOpenHelper {

    private final static int _DBVersion = 1; //版本
    private final static String _DBName = "JournalList.db";// 資料庫 name

    private static String _TableName = "JournalNote";// 資料表 name
    private static String _ID = "_id";
    private static String IMAGENAME = "_IMAGENAME";
    private static String TITLE = "_TITLE";
    private static String CONTENT = "_CONTENT";

    public journalSQLiteHelper(Context context, String tableName) {
        super(context, _DBName, null, _DBVersion); //給 SQLiteOpenHelper construct
        _TableName = tableName;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_IMAGENAME VARCHAR(100)," +
                "_TITLE VARCHAR(200) NOT NULL, " +
                "_CONTENT TEXT" +
                ")";
        System.out.println("SQL");
        System.out.println(SQL);
        System.out.println(_TableName);
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        final String SQL = "DROP TABLE " + _TableName;
        db.execSQL(SQL);
        onCreate(db);
    }

    public void set_TableName(String tableName){
        _TableName = tableName;
    }
    public void set_id(String id){
        _ID = id;
    }
    public void setTITLE(String title){
        TITLE = title;
    }
    public void setCONTENT(String content){
        CONTENT = content;
    }
    public void setIMAGENAME(String imagename){
        IMAGENAME = imagename;
    }

    public String get_TableName(){
        return _TableName;
    }
    public String get_id(){
        return _ID;
    }
    public String getTITLE(){
        return TITLE;
    }
    public String getCONTENT(){
        return CONTENT;
    }
    public String getIMAGENAME(){
        return IMAGENAME;
    }
}
