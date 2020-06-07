package com.example.navimap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class EditDB extends SQLiteOpenHelper {
    private final static int DBversion = 1;
    private final static String DBname = "TravelDB";
    private final static String TableName = "TravelList";


    public EditDB(@Nullable Context context) {
        super(context, DBname, null, DBversion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL = "CREATE TABLE IF NOT EXISTS " + TableName + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_title VARCHAR(200) NOT NULL" +
                ")";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE " + TableName;
        db.execSQL(SQL);
    }

    public String getTableName(){
        return TableName;
    }

    public void insert(String title, SQLiteDatabase db, int index){
        String sql = "insert into " + TableName + " (" + "_id," +  "_title" + ") values (" + index + ", "+"'"+ title + "'"+")";
        db.execSQL(sql);
        db.close();
    }
    public void onDelete(SQLiteDatabase db, String title,int id){
        db.delete(TableName,"_title = '" + title  +"';",null);
        String update = "UPDATE " + TableName + " SET ";
        String attr = "_id = _id - 1";
        String condition = " WHERE _id > " + id;
        String sql = update + attr + condition;
        db.execSQL(sql);
        db.close();
    }
    public void dbshow(SQLiteDatabase db){
        String[] columns = {"_id","_title"};
        Cursor cursor = db.query(TableName, columns, null, null, null, null, null);
        System.out.println("Data base show");
        System.out.println(TableName);
        while (cursor.moveToNext()) {
            System.out.println("ID : " + cursor.getInt(0));
            System.out.println("title : " + cursor.getString(1));
        }
        cursor.close();
        db.close();
    }

}
