package com.example.navimap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import androidx.annotation.Nullable;

class noteDB extends SQLiteOpenHelper {
    private final static int DBversion = 1;
    private final static String DBname = "sideListDB";
    private final static String TableName = "noteList";

    public noteDB(@Nullable Context context) {
        super(context, DBname, null, DBversion);
    }

    public String getDBname(){
        return DBname;
    }
    public String getTableName(){
        return TableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL = "create table if not exists " + TableName + "( " +
                "_id integer primary key autoincrement, " +
                "_title varchar(50), " +
                "_content varchar(3000), " +
                "_image integer" + ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "drop table " + TableName;
        db.execSQL(SQL);
    }
}
