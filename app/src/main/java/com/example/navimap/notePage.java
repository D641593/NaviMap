package com.example.navimap;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
        saveBtn = findViewById(R.id.saveBtn);
        content = findViewById(R.id.editContent);
        titleIntent = getIntent();
        title = titleIntent.getStringExtra("Title");
    }

    private void setItem(){

        SQLiteDatabase db = DB.getReadableDatabase();
        String SQLinst = "select * from " + DB.getTableName() + " where _title = " + title + ";";
        Cursor c = db.rawQuery(SQLinst,null);
        if(c.moveToFirst()){
            id = c.getInt(0);
            content.setText(c.getString(2));
            saveContent = c.getString(2);
        }else{
            content.setText("");
            saveContent = "";
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
