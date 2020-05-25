package com.example.navimap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class notePage extends AppCompatActivity {

    private noteDB DB;
    private Button saveBtn;
    private EditText content;
    private String title;
    private Intent titleIntent;
    private String saveContent;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit_page);
        initItem();
        setItem();

    }

    private void initItem(){
        DB = new noteDB(this);
        //DB.onCreate(DB.getWritableDatabase()); //建立Table
        //DB.onUpgrade(DB.getWritableDatabase(),1,1); // 清除Table
        saveBtn = findViewById(R.id.saveBtn);
        content = findViewById(R.id.editContent);
        titleIntent = getIntent();
        title = titleIntent.getStringExtra("Title");
    }

    private void setItem(){

        getSupportActionBar().setTitle(title);
        Cursor c = null;
        SQLiteDatabase db = DB.getReadableDatabase();
        String SQLinst = "select * from " + DB.getTableName() + " where _title = '" + title + "';";
        try {
            c = db.rawQuery(SQLinst,null);
            c.moveToFirst();
            id = c.getInt(0);
            content.setText(c.getString(2));
            saveContent = c.getString(2);
        }catch(Exception e){
            ContentValues values = new ContentValues();
            values.put("_title",title);
            values.put("_content","");
            db.insert(DB.getTableName(),null,values);
            content.setText("");
            saveContent = "";
            c = db.rawQuery(SQLinst,null);
            c.moveToFirst();
            id = c.getInt(0);
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveContent != content.getText().toString()){
                    SQLiteDatabase db = DB.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("_title",title);
                    values.put("_content",content.getText().toString());
                    System.out.println(values.toString());
                    db.update(DB.getTableName(),values,"_id = " + id , null);
                    db.close();
                    // Check if no view has focus:
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        content.clearFocus();
                    }
                    Toast.makeText(getApplicationContext(),"儲存成功",Toast.LENGTH_SHORT).show();
                }

            }
        });
        c.close();
        db.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.topbutton,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_gallery){

        }else if(item.getItemId() == R.id.action_time){

        }
        return super.onOptionsItemSelected(item);
    }
}
