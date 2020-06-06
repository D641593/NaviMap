package com.example.navimap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

class journalDBManager {
    private Context context;
    private SQLiteDatabase database;
    private journalSQLiteHelper dbHelper;
    private String tableName;
    private List<Journal_list_item> t = new ArrayList<>();
//    private list t = new list();
//    class list{
//        List<Journal_list_item> journal_list = new ArrayList<>();
//        List<String> content = new ArrayList<>();
//    }


    public journalDBManager(Context c, String markerName){
        this.context = c;
        this.tableName = "J_"+ markerName;
        this.dbHelper = new journalSQLiteHelper(this.context, markerName);
    }

    public List<Journal_list_item> initList(){
        try {

            database = dbHelper.getReadableDatabase();
            String[] columns={dbHelper.get_id(),dbHelper.getIMAGENAME(), dbHelper.getTITLE(), dbHelper.getCONTENT()};
            Cursor cursor = database.query(this.tableName, columns,null,null,null,null,null);
            while (cursor.moveToNext()){
                Journal_list_item tmp = new Journal_list_item(cursor.getInt(cursor.getColumnIndex(dbHelper.get_id())), cursor.getString(cursor.getColumnIndex(dbHelper.getIMAGENAME())), cursor.getString(cursor.getColumnIndex(dbHelper.getTITLE())), cursor.getString(cursor.getColumnIndex(dbHelper.getCONTENT())));
//                t.content.add(cursor.getString(3));
//                t.journal_list.add(tmp);
                t.add(tmp);
            }

            cursor.close();
            database.close();
            return t;
        } catch (Exception e){
            dbHelper.onCreate(database);
            database.close();
            return t;
        }
    }


    public void create(int id, String imageName, String title, String content){
        database = dbHelper.getWritableDatabase();

        ContentValues contentValue = new ContentValues();//一個等等寫入sql的集合
        contentValue.put(dbHelper.get_id(), id);
        contentValue.put(dbHelper.getIMAGENAME(), imageName);
        contentValue.put(dbHelper.getTITLE(), title);
        contentValue.put(dbHelper.getCONTENT(), content);
        this.database.insert(this.tableName, null, contentValue);
        database.close();
    }

    public void change(int id, String imageName, String title, String content){
        database = dbHelper.getWritableDatabase();

        String update = "UPDATE " + this.tableName + " SET ";
        String attr = "_IMAGENAME = " + "'" + imageName + "'"
                    + ", _TITLE = " + "'" + title + "'"
                    + ", _CONTENT = "  + "'" + content + "'";
        String condition = " WHERE _id = " + id;

        String sql = update + attr + condition;
        System.out.println(sql);

        database.execSQL(sql);
        database.close();
    }

    public void show() {
        database = dbHelper.getReadableDatabase();  //取得查詢物件Cursor
        String[] columns = {dbHelper.get_id(), dbHelper.getIMAGENAME(), dbHelper.getTITLE(), dbHelper.getCONTENT()};
        Cursor cursor = database.query(this.tableName, columns, null, null, null, null, null);
        while (cursor.moveToNext()) {
            System.out.println("Data base show");
            System.out.println(this.tableName);
            System.out.println("ID : " + cursor.getInt(0));
            System.out.println("ImageName : " + cursor.getString(1));
            System.out.println("title : " + cursor.getString(2));
            System.out.println("content : " + cursor.getString(3));
        }
        cursor.close();
        database.close();
    }
    public void close(){
        this.dbHelper.close();
    }
    public void delete(long id){
        this.dbHelper = new journalSQLiteHelper(this.context, this.tableName);
        database = dbHelper.getWritableDatabase();
        database.delete(this.tableName, "_id = " + id, null);

        String update = "UPDATE " + this.tableName + " SET ";
        String attr = "_id = _id - 1";
        String condition = " WHERE _id > " + id;
        String sql = update + attr + condition;
        database.execSQL(sql);

        database.close();
    }

}
