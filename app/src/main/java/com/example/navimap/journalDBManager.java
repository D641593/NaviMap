package com.example.navimap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class journalDBManager {
    private Context context;
    private SQLiteDatabase database;
    private journalSQLiteHelper dbHelper;

    public journalDBManager(Context c){
        this.context = c;
    }

    public journalDBManager open() throws SQLException{
        this.dbHelper = new journalSQLiteHelper(this.context);
        this.database = this.dbHelper.getWritableDatabase(); //等等用 db.exec("...")
        return this;
    }

    public void close(){
        this.dbHelper.close();
    }

    public void insert(String NoteTitle, String NoteContent){
        ContentValues contentValue = new ContentValues();//一個等等寫入sql的集合
        contentValue.put(journalSQLiteHelper.TITLE, NoteTitle);
        contentValue.put(journalSQLiteHelper.CONTENT, NoteContent);
        this.database.insert(journalSQLiteHelper._TableName, null, contentValue);
    }

    //Cursor 幫忙處理 query後的資料包, 因為query結果可能不只一個
    public Cursor fetch(){
        //select "_TITLE", "_CONTENT" from "JournalNote" (where:"N/A", orderby:"N/A", having:"N/A")
        Cursor cursor = this.database.query(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE, journalSQLiteHelper.CONTENT}, null, null,null, null ,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor fetchcolumns(String[] columnsContent){
        //select "_TITLE", "_CONTENT" from "JournalNote" (where:"N/A", orderby:"N/A", having:"N/A")
        Cursor cursor = this.database.query(journalSQLiteHelper._TableName, columnsContent, null, null,null, null ,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor fetchAllArgs(String tableName, String[] columnsContent, String where, String[] whereArgs, String groupby, String having, String orderby){
        //select "_TITLE", "_CONTENT" from "JournalNote" (where:"N/A", orderby:"N/A", having:"N/A")
        Cursor cursor = this.database.query(tableName, columnsContent, where, whereArgs,groupby, having ,orderby);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String NoteTitle, String NoteContent){
        ContentValues contentValue = new ContentValues();
        contentValue.put(journalSQLiteHelper.TITLE, NoteTitle);
        contentValue.put(journalSQLiteHelper.CONTENT, NoteContent);
        return this.database.update(journalSQLiteHelper._TableName, contentValue, "_id = " + _id, null);
    }

    public void delete(long _id){
        this.database.delete(journalSQLiteHelper._TableName, "_id = " + _id , null);
    }

    public void deleteAll(){
//        database.execSQL("DELETE FROM " + SQLiteHelper._TableName);
        this.database.delete(journalSQLiteHelper._TableName, null, null);
    }
}
