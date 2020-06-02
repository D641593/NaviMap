package com.example.navimap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

class journalDBManager {
    private Context context;
    private SQLiteDatabase database;
    private journalSQLiteHelper dbHelper;
    private String tableName;
    private list t = new list();
    class list{
        List<Journal_list_item> journal_list = new ArrayList<>();
        List<String> content = new ArrayList<>();
    }


//    public journalDBManager(Context c){
//        this.context = c;
//
//        this.dbHelper = new journalSQLiteHelper(this.context);
//    }

    public journalDBManager(Context c, String markerName){
        this.context = c;
        this.tableName = markerName;
        this.dbHelper = new journalSQLiteHelper(this.context, markerName);
        dbHelper.close();

    }



//    public void open() throws SQLException{
//        dbHelper.set_TableName(this.tableName);
//        this.dbHelper = new journalSQLiteHelper(this.context);
////        this.database = this.dbHelper.getWritableDatabase(); //等等用 db.exec("...")
////        return this;
//    }

    public list initList(){
//        dbHelper.set_TableName(this.tableName);
//        try {
//
//        } catch (Exception e){
//            dbHelper.onCreate(database);
//            database.close();
//            return t;
//        }
        this.dbHelper = new journalSQLiteHelper(this.context, this.tableName);
        database = dbHelper.getReadableDatabase();
        String[] columns={dbHelper.get_id(),dbHelper.getIMAGENAME(), dbHelper.getTITLE()};
        Cursor cursor = database.query(this.tableName, columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            Journal_list_item tmp = new Journal_list_item(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            t.content.add(cursor.getString(3));
            t.journal_list.add(tmp);
//            int id = cursor.getInt(0);
//            String imageName = cursor.getString(1);
//            String title = cursor.getString(2);
//            String content = cursor.getString(3);
//            resultData.append(id).append(": ");
//            resultData.append(name).append(": ");
//            resultData.append(tel).append(": ");
//            resultData.append(email).append("\n");
        }

        cursor.close();
        database.close();
        dbHelper.close();
        return t;


//        String select = "Select IMAGENAME, TITLE ";
//        String from = "from " + this.tableName;
//        String where = "where name like ? ";
//        String sql = select + from;
//        Cursor cursor = db.rawQuery(sql, selectionArgs);
    }


    public void create(int id, String imageName, String title, String content){
//        dbHelper.set_TableName(this.tableName);
        this.dbHelper = new journalSQLiteHelper(this.context, this.tableName);
        database = dbHelper.getWritableDatabase();

        ContentValues values=new ContentValues();
        ContentValues contentValue = new ContentValues();//一個等等寫入sql的集合
        contentValue.put(dbHelper.get_id(), id);
        contentValue.put(dbHelper.getIMAGENAME(), imageName);
        contentValue.put(dbHelper.getTITLE(), title);
        contentValue.put(dbHelper.getCONTENT(), content);
        this.database.insert(this.tableName, null, contentValue);
        database.close();
        dbHelper.close();
    }

    public void change(int id, String imageName, String title, String content){
//        dbHelper.set_TableName(this.tableName);
        this.dbHelper = new journalSQLiteHelper(this.context, this.tableName);
        database = dbHelper.getWritableDatabase();

        String update = "UPDATE " + this.tableName + " SET ";
        String attr = "_IMAGENAME = " + imageName
                    + " _TITLE = " + title
                    + " _CONTENT = " + content;
        String condition = " WHERE _id = " + id;
        String sql = update + attr + condition;

        database.execSQL(sql);
        database.close();
        dbHelper.close();
    }

    public void show(){
        this.dbHelper = new journalSQLiteHelper(this.context, this.tableName);
        database = dbHelper.getReadableDatabase();  //取得查詢物件Cursor
        String[] columns={dbHelper.get_id(),dbHelper.getIMAGENAME(), dbHelper.getTITLE(), dbHelper.getCONTENT()};
        Cursor cursor = database.query(this.tableName,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            System.out.println("Data base show");
            System.out.println(this.tableName);
            System.out.println("ID : " + cursor.getInt(0));
            System.out.println("ImageName : " +cursor.getString(1));
            System.out.println("title : " +cursor.getString(2));
            System.out.println("content : " +cursor.getString(3));
        }
        cursor.close();
        database.close();
        dbHelper.close();
    }

    public void close(){
        this.dbHelper.close();
    }

//    public void insert(String NoteTitle, String NoteContent){
//        ContentValues contentValue = new ContentValues();//一個等等寫入sql的集合
//        contentValue.put(journalSQLiteHelper.get, NoteTitle);
//        contentValue.put(journalSQLiteHelper.CONTENT, NoteContent);
//        this.database.insert(journalSQLiteHelper._TableName, null, contentValue);
//    }
//
//    //Cursor 幫忙處理 query後的資料包, 因為query結果可能不只一個
//    public Cursor fetch(){
//        //select "_TITLE", "_CONTENT" from "JournalNote" (where:"N/A", orderby:"N/A", having:"N/A")
//        Cursor cursor = this.database.query(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE, journalSQLiteHelper.CONTENT}, null, null,null, null ,null);
//        if(cursor != null){
//            cursor.moveToFirst();
//        }
//        return cursor;
//    }
//    public Cursor fetchcolumns(String[] columnsContent){
//        //select "_TITLE", "_CONTENT" from "JournalNote" (where:"N/A", orderby:"N/A", having:"N/A")
//        Cursor cursor = this.database.query(journalSQLiteHelper._TableName, columnsContent, null, null,null, null ,null);
//        if(cursor != null){
//            cursor.moveToFirst();
//        }
//        return cursor;
//    }
//    public Cursor fetchAllArgs(String tableName, String[] columnsContent, String where, String[] whereArgs, String groupby, String having, String orderby){
//        //select "_TITLE", "_CONTENT" from "JournalNote" (where:"N/A", orderby:"N/A", having:"N/A")
//        Cursor cursor = this.database.query(tableName, columnsContent, where, whereArgs,groupby, having ,orderby);
//        if(cursor != null){
//            cursor.moveToFirst();
//        }
//        return cursor;
//    }
//
//    public int update(long _id, String NoteTitle, String NoteContent){
//        ContentValues contentValue = new ContentValues();
//        contentValue.put(journalSQLiteHelper.TITLE, NoteTitle);
//        contentValue.put(journalSQLiteHelper.CONTENT, NoteContent);
//        return this.database.update(journalSQLiteHelper._TableName, contentValue, "_id = " + _id, null);
//    }
//
//    public void delete(long _id){
//        this.database.delete(journalSQLiteHelper._TableName, "_id = " + _id , null);
//    }
//
//    public void deleteAll(){
////        database.execSQL("DELETE FROM " + SQLiteHelper._TableName);
//        this.database.delete(journalSQLiteHelper._TableName, null, null);
//    }
}
