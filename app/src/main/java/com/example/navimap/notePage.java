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
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
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
    private Dialog calendar, calendar_show;
    private TextView start_date, end_date, check_date, cancel_date;
    private Button calendar_add, calendar_cancel, choise_start, choise_end;
    private Calendar beginTime = Calendar.getInstance(), endTime = Calendar.getInstance();
    private CalendarView calendarView;
    private SimpleDateFormat sdf = new SimpleDateFormat("E yyyy/MM/dd");

    private LinearLayout Rlayout;
    private ArrayList<Pair<View,String>> contents = new ArrayList<>();
    private AlertDialog.Builder alertDialog;
    private int contentToDelete;
    private BottomNavigationView btmView;
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
        btmView = findViewById(R.id.navigationBottomView);
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
        btmView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int ID = item.getItemId();
                if( ID == R.id.NotePageItem){
                    // Do nothing
                    System.out.println("NotePage");
                }else if( ID == R.id.GoogleMapItem ){
                    System.out.println("GoogleMap");
                    Intent intent = new Intent(notePage.this,MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Name",title);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if( ID == R.id.JournalPageItem){
                    System.out.println("Journal");
                    Intent intent = new Intent(notePage.this,journal.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Name",title);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                System.out.println("Selected btm");
                return true;
            }
        });
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
    public void onRestart() {
        super.onRestart();
        System.out.println("Restart");
        btmView.getMenu().getItem(0).setChecked(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.topbutton,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int ID = item.getItemId();
        if(ID == R.id.action_gallery){
            openGallery();
        }else if(ID == R.id.action_time){
            init_calendar_dialog();
            init_calendar_show();
            calendar.show();
            choise_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendar_show.show();
                    calendar_show.setTitle("設定起始時間");
                    calendarShow(0);
                }
            });
            choise_end.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendar_show.show();
                    calendar_show.setTitle("設定結束時間");
                    calendarShow(1);
                }
            });


            calendar_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(beginTime.getTime().after(endTime.getTime())){
                        Toast.makeText(getApplicationContext(),"結束時間不能早於起始時間",Toast.LENGTH_SHORT).show();
                        return;
                    }
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
                    start_date.setText(R.string.start_date);
                    end_date.setText(R.string.end_date);
                    calendar.dismiss();

                }
            });
            calendar_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    start_date.setText(R.string.start_date);
                    end_date.setText(R.string.end_date);
                    calendar.dismiss();
                }
            });
        }else if( ID == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void calendarShow(final int i){

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                System.out.println(year +"\n"+ month +"\n"+ dayOfMonth);
                switch (i){
                    case 0:
                        beginTime.set(year,month,dayOfMonth);
                        break;
                    case 1:
                        endTime.set(year,month,dayOfMonth);
                        break;
                }

            }
        });
        check_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                now.add(Calendar.DATE,-1);
                switch (i){
                    case 0:
                        if (beginTime.getTime().before(now.getTime())){
                            Toast.makeText(getApplicationContext(),"時間是無法回去的",Toast.LENGTH_SHORT).show();
                            beginTime.clear();
                            return;
                        }
                        start_date.setText("起始時間: " + sdf.format(beginTime.getTime()));
                        break;
                    case 1:
                        if (endTime.getTime().before(now.getTime())){
                            Toast.makeText(getApplicationContext(),"時間是無法回去的",Toast.LENGTH_SHORT).show();
                            endTime.clear();
                            return;
                        }
                        end_date.setText("結束時間: " + sdf.format(endTime.getTime()));
                        break;
                }


                calendar_show.dismiss();
            }
        });
        cancel_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar_show.dismiss();
                switch (i){
                    case 0:
                        beginTime.clear();
                        break;
                    case 1:
                        endTime.clear();
                        break;
                }
                return;
            }

        });
    }

    public void init_calendar_show(){
        calendar_show = new Dialog(this);
        calendar_show.setContentView(R.layout.calendar);
        calendarView = calendar_show.findViewById(R.id.calendarView);
        check_date = calendar_show.findViewById(R.id.date_check);
        cancel_date = calendar_show.findViewById(R.id.date_cancel);
    }
    public void init_calendar_dialog(){
        calendar = new Dialog(this);
        calendar.setTitle("Add time!");
        calendar.setContentView(R.layout.calender_add);
        start_date = calendar.findViewById(R.id.start);
        end_date = calendar.findViewById(R.id.end);
        choise_start = calendar.findViewById(R.id.choise_start);
        choise_end = calendar.findViewById(R.id.choise_end);
        calendar_add = (Button)calendar.findViewById(R.id.calender_add);
        calendar_cancel = (Button)calendar.findViewById(R.id.calender_cancel);
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
