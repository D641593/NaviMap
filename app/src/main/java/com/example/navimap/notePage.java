package com.example.navimap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class notePage extends AppCompatActivity {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0x1000;
    private static final int REQUEST_GALLERY = 0x1001;
    private noteDB DB;
    private Button saveBtn;
    private String title;
    private Intent titleIntent;

    //    Calendar dialog
    private Dialog calendar;
    private EditText year, month, day, hour, min, during;
    private Button calendar_add, calendar_cancel;
    private Spinner unit;
    private LinearLayout Rlayout;
    private ArrayList<Pair<View,String>> contents = new ArrayList<>();
    private AlertDialog.Builder alertDialog;
    private int contentToDelete;
    private ImageView.OnLongClickListener imageLis =  new ImageView.OnLongClickListener(){

        @Override
        public boolean onLongClick(View v) {
            for (int i=0;i<contents.size();i++){
                if(contents.get(i).first == v){
                    System.out.println("Here I am");
                    contentToDelete = i;
                }
            }
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ImageView deleteImage = (ImageView) contents.get(contentToDelete).first;
                    deleteImage.setImageURI(null);
                    contents.remove(contentToDelete);
                    Rlayout.removeView(deleteImage);
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            return true;
        };
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit_page);
        initItem();
        setItem();
    }

    private void initItem(){
        DB = new noteDB(this);
        //DB.onUpgrade(DB.getWritableDatabase(),1,1); // 清除Table
        DB.onCreate(DB.getWritableDatabase()); //建立Table
        saveBtn = findViewById(R.id.saveBtn);
        titleIntent = getIntent();
        title = titleIntent.getStringExtra("Title");
        Rlayout = findViewById(R.id.RLayout);
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("刪除圖片!");
        alertDialog.setMessage("要刪除這張圖片嗎?");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setItem(){

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SQLiteDatabase db = DB.getReadableDatabase();
        String SQLinst = "select * from " + DB.getTableName() + " where _title = '" + title + "';";
        Cursor c = db.rawQuery(SQLinst,null);
        if(c.moveToFirst()){
            while(!c.isAfterLast()){
                String content = c.getString(2);
                System.out.println(content);
                if(c.getInt(3) == 1){
                    Uri uri = Uri.parse(content);
                    addImage(uri,false);
                }else{
                    addEditText(content);
                }
                c.moveToNext();
            }
            c.moveToPrevious();
            if(c.getInt(3) == 1){
                addEditText("");
            }
        }else{
            System.out.println("No Data");
            addEditText("");
        }


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = DB.getWritableDatabase();
                ContentValues values = new ContentValues();
                String DropCondition = "_title = '" + title + "';";
                db.delete(DB.getTableName(),DropCondition,null);
                int i = 0;
                while( i < contents.size()) {
                    values.put("_title", title);
                    if (contents.get(i).first instanceof EditText) {
                        values.put("_content", ((EditText) contents.get(i).first).getText().toString());
                        values.put("_image", 0);
                    } else {
                        values.put("_content", contents.get(i).second);
                        values.put("_image", 1);
                    }
                    db.insert(DB.getTableName(), null, values);
                    values.clear();
                    i++;
                }
                Toast.makeText(getApplicationContext(), "儲存成功", Toast.LENGTH_SHORT).show();
                DBShow();
                db.close();
            }
        });
        c.close();
        db.close();
    }

    private void DBShow(){
        SQLiteDatabase db = DB.getReadableDatabase();
        Cursor c = db.rawQuery("Select * from " + DB.getTableName(),null);
        if(c.moveToFirst()){
            while(!c.isAfterLast()){
                System.out.println(c.getString(1));
                System.out.println(c.getString(2));
                System.out.println(c.getInt(3));
                c.moveToNext();
            }
        }

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
            openGallery();
        }else if(item.getItemId() == R.id.action_time){
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
                            //calIntent.setDescription(saveContent);
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
        }else if(item.getItemId() == android.R.id.home){
            finish();
            return true;
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void addEditText(String content){
        EditText text = new EditText(this);
        text.setText(content);
        text.setTextSize(18);
        text.setBackground(null);
        text.requestFocus();
        contents.add(new Pair<View,String>(text,content));
        Rlayout.addView(text);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void addImage(Uri uri, boolean addEditFlag){
        if (contents.size() > 0 && contents.get(contents.size() - 1).first instanceof EditText) {
            EditText editText = (EditText) contents.get(contents.size() - 1).first;
            if (editText.getText().toString().equals("")) {
                contents.remove(contents.size() - 1);
                Rlayout.removeView(editText);
            }
        }
        ImageView image = new ImageView(this);
        image.setOnLongClickListener(imageLis);
        image.setImageURI(uri);
        image.setPadding(10,10,10,10);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        image.setLayoutParams(layoutParams);
        contents.add(new Pair<View,String>(image, uri.toString()));
        Rlayout.addView(image);
        if(addEditFlag) {
            addEditText("");
        }
    }

    private void openGallery() {
        PhotoSave photoSave = new PhotoSave();
        if(!photoSave.hasPermission(getApplicationContext())) {
            if (photoSave.needCheckPermission(notePage.this)){
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                Uri uri = data.getData();
                addImage(uri,true);
            }
        }
    }

}
