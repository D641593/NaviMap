package com.example.navimap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class notePage extends AppCompatActivity {

    private noteDB DB;
    private Button saveBtn;
    private EditText content;
    private String title;
    private Intent titleIntent;
    private String saveContent;
    private int id;

    //    Calendar dialog
    private Dialog calendar;
    private EditText year, month, day, hour, min, during;
    private Button calendar_add, calendar_cancel;
    private Spinner unit;
    private AlertDialog.Builder add_calendar_dialog;

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
                    saveContent = content.getText().toString();
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
            if (!saveContent.equals(content.getText().toString())){
                Toast.makeText(getApplicationContext(),"請先儲存",Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            init_calendar_dialog();
            calendar.show();
            calendar_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //建立事件開始時間
                    if(checkEditNull()){
                        Toast.makeText(getApplicationContext(),"不能有空",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Calendar now = Calendar.getInstance();
                        int y = Integer.parseInt(year.getText().toString().trim());
                        if (y < now.get(Calendar.YEAR)) {
                            Toast.makeText(getApplicationContext(), "時間回不去了", Toast.LENGTH_SHORT).show();
                            clearEdit(year);
                        } else if (y >= now.get(Calendar.YEAR) + 100) {
                            Toast.makeText(getApplicationContext(), "你真的能活這麼久??", Toast.LENGTH_SHORT).show();
                            clearEdit(year);
                        } else {
                            int m = Integer.parseInt(month.getText().toString().trim());
                            int d = Integer.parseInt(day.getText().toString().trim());
                            int h = Integer.parseInt(hour.getText().toString().trim());
                            int minute = Integer.parseInt(min.getText().toString().trim());
                            int monment = Integer.parseInt(during.getText().toString().trim());

                            Calendar beginTime = Calendar.getInstance();
                            beginTime.set(y, m - 1, d, h, minute);

                            switch (unit.getSelectedItemPosition()){
                                case 3:
                                    y += monment;
                                    if( y >= now.get(Calendar.YEAR) + 100){
                                        Toast.makeText(getApplicationContext(),"你活不了這麼久",Toast.LENGTH_SHORT).show();
                                        clearEdit(during);
                                        return;
                                    }
                                    break;
                                case 2:
                                    m += monment;
                                    break;
                                case 1:
                                    d += monment;
                                    break;
                                case 0:
                                    h += monment;
                                    break;

                            }

                            //建立事件結束時間
                            Calendar endTime = Calendar.getInstance();

                            endTime.set(y, m - 1, d, h, minute);

                            //建立 CalendarIntentHelper 實體
                            CalendarIntentHelper calIntent = new CalendarIntentHelper();
                            //設定值
                            calIntent.setTitle(title);
                            calIntent.setDescription(saveContent);
                            calIntent.setBeginTimeInMillis(beginTime.getTimeInMillis());
                            calIntent.setEndTimeInMillis(endTime.getTimeInMillis());
                            //                            calIntent.setLocation("事件地點");

                            //全部設定好後就能夠取得 Intent
                            Intent intent = calIntent.getIntentAfterSetting();

                            //送出意圖
                            startActivity(intent);

                            clearEdit("All");
                            calendar.dismiss();
                        }
                    }

                }
            });
            calendar_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearEdit("All");
                    calendar.dismiss();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    public void init_calendar_dialog(){
        calendar = new Dialog(this);
        calendar.setTitle("Add time!");
        calendar.setContentView(R.layout.calender_add);
        year = (EditText)calendar.findViewById(R.id.num_year);
        month = (EditText)calendar.findViewById(R.id.num_month);
        day = (EditText)calendar.findViewById(R.id.num_day);
        hour = (EditText)calendar.findViewById(R.id.num_hour);
        min = (EditText)calendar.findViewById(R.id.num_min);
        during = (EditText)calendar.findViewById(R.id.during);
        unit = (Spinner)calendar.findViewById(R.id.unit);
        calendar_add = (Button)calendar.findViewById(R.id.calender_add);
        calendar_cancel = (Button)calendar.findViewById(R.id.calender_cancel);
    }

    public boolean checkEditNull(){
        if(year.getText().toString().isEmpty() || month.getText().toString().isEmpty() || day.getText().toString().isEmpty()
                || hour.getText().toString().isEmpty() || min.getText().toString().isEmpty() || during.getText().toString().isEmpty()){
            return true;
        }
        return false;
    }

    public void clearEdit(EditText text){
        text.setText("");
    }

    public void clearEdit(String all){
        if(all.equals("All")){
            year.setText("");
            month.setText("");
            day.setText("");
            hour.setText("");
            min.setText("");
            during.setText("");
        }
    }

}
