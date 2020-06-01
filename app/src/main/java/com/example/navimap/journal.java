package com.example.navimap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class journal extends AppCompatActivity {
//
//    private ListView listView;
//    private ListAdapter listAdapter;
//    private Dialog dialog;
//    private TextView titleshow, contentshow;
//    private EditText title;
//    private AlertDialog.Builder alertDialog;
//    private Button add, cancel;
//    private Button editnote, deletenote, deleteAllnote;
//
//    private journalDBManager dbManager = new journalDBManager(this);
//
//    private ArrayList<String> listItems = new ArrayList<>();
//    private ArrayList<String> listItemsContent = new ArrayList<String>();
//    private int listItemIndex = 0;
//    private boolean listItemClicked = false;

    // add_dialog.xml
    private Dialog dialog;
    private AlertDialog.Builder alertDialog;
    private Button create_journal, create_cancel;
    private EditText journal_title, journal_content;
    private ImageView journal_image;

    //main.xml
    private Button create_start;
    private ListView journal_list;

    //暫存值
    private String imageName = null, markerName = null;
    private Boolean is_item_change = false;
    private int journal_item_position = 0;
    private Intent intent = new Intent();

    //主要值
    private static List<String> content_list = new ArrayList<>();
    private static List<Journal_list_item> main_list = new ArrayList<>();

    //用到的java
    private JournalAdapter journal_adapter;
    private PhotoSave photo= new PhotoSave();

    //DataBase
    private journalDBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_main);
        intent = getIntent();
        markerName = intent.getStringExtra("Name");
        Toast.makeText(getApplicationContext(),markerName,Toast.LENGTH_SHORT).show();

        journal_list = findViewById(R.id.journalList);
        initList();

        create_start = findViewById(R.id.create_start);
        create_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_item_change = false;
                imageName = null;
                journal_item_position = 0;
                initDia();
                diashow();
            }
        });
        journal_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Journal_list_item item = main_list.get(position);
                changeDialog(item.getImageName(), item.getTitle(), content_list.get(position));
//                imageName = item.getImageName();
                journal_item_position = position;
                is_item_change = true;
                diashow();
            }
        });
        journal_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                initdeleteDialog();
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        content_list.remove(position);
                        main_list.remove(position);
                        if(main_list.size()!= 0){
                            resetIndex(position);
                        }
                        journal_list.setAdapter(journal_adapter);
                        dialog.dismiss();

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
            }
        });

//        //Toolbar journaltoolbar = findViewById(R.id.toolbar2);
//        //setSupportActionBar(journaltoolbar);
//        getSupportActionBar().setTitle("Journal");
//        System.out.println("setEnable");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        try{
//            dbManager.open();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        initLoadJournalList();
//
////        設定"add_note.xml"
//        initAddJournalNote();
////        設定delete的警告對話方塊
//        initRemoveJoutnalNote();
//
//        titleshow = (TextView)findViewById(R.id.notetitleView);
//        contentshow = (TextView) findViewById(R.id.notecontentView);
//
//        editnote = (Button)findViewById(R.id.editNotebtn);
//        editnote.setOnClickListener(editNoteclicklistener);
//        editnote.setEnabled(listItemClicked && !listItems.isEmpty());
//
//        deletenote = (Button)findViewById(R.id.deleteNotebtn);
//        deletenote.setOnClickListener(deleteNoteclicklistener);
//        deletenote.setEnabled(listItemClicked && !listItems.isEmpty());
//
//        deleteAllnote = (Button)findViewById(R.id.deleteAllNotebtn);
//        deleteAllnote.setOnClickListener(deleteAllNoteclicklistener);
//
//        Button newNote = (Button)findViewById(R.id.addNotebtn);
//        newNote.setOnClickListener(addNoteclicklistener);
//
//        listView = (ListView)findViewById(R.id.journalList);
//        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
//        listView.setAdapter(listAdapter);
//        listView.setOnItemClickListener(listitemclicklistener);

//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        -----------------------Bug
//        journaltoolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Oh Ya", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public void initDia(){
        dialog = new Dialog(this);
        dialog.setTitle("新增遊記");
        dialog.setContentView(R.layout.journal_add_dialog);
        create_journal = dialog.findViewById(R.id.create_journal);
        create_cancel = dialog.findViewById(R.id.journal_cancel);
        journal_title = dialog.findViewById(R.id.creat_title);
        journal_content = dialog.findViewById(R.id.creat_content);
        journal_image = dialog.findViewById(R.id.creat_image);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeDialog(String imageName, String titleName, String contentName){
        dialog.setTitle("變更遊記");
        create_journal.setText("儲存");
        journal_title.setText(titleName);
        journal_content.setText(contentName);
        if(imageName != null){
            journal_image.setBackground(null);
            journal_image.setImageBitmap(photo.getPhoto(imageName));
        } else {
            journal_image.setImageResource(R.mipmap.ic_launcher);
        }

    }
    private void initdeleteDialog(){
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("刪除標記點!");
        alertDialog.setMessage("你確定要刪除嗎?");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageName = photo.save(bitmap,markerName);
            journal_image.setBackground(null);
            journal_image.setImageBitmap(bitmap);
        }

    }

    public void resetIndex(int position){
        for(int i=position ;i<main_list.size();i++){
            main_list.get(i).setItem_index();
        }
        main_list.get(0).setId();
    }

    public void diashow(){
        dialog.show();

        create_journal.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(!is_item_change){
                    Journal_list_item tmp = new Journal_list_item();
                    if(!journal_title.getText().toString().isEmpty()){
                        tmp.setTitle(journal_title.getText().toString().trim());
                    } else {
                        Toast.makeText(getApplicationContext(),"主題不能為空",Toast.LENGTH_SHORT).show();
                    }
                    if(!journal_content.getText().toString().isEmpty()){
                        content_list.add(journal_content.getText().toString().trim());
                    } else {
                        Toast.makeText(getApplicationContext(),"內容不能為空",Toast.LENGTH_SHORT).show();
                    }
                    if(imageName != null){
                        tmp.setImageName(imageName);
                        journal_image.setImageResource(R.mipmap.ic_launcher);
                    }

                    dbManager.create(tmp.getItem_index(),
                                     tmp.getImageName(),
                                     tmp.getTitle(),
                                     journal_content.getText().toString().trim());
                    System.out.println("create" + journal_content.getText().toString().trim());
                    dbManager.show();
                    main_list.add(tmp);
                }
                else {
                    if(!journal_title.getText().toString().isEmpty()){
                        main_list.get(journal_item_position).setTitle(journal_title.getText().toString().trim());
                    } else {
                        Toast.makeText(getApplicationContext(),"主題不能為空",Toast.LENGTH_SHORT).show();
                    }
                    if(!journal_content.getText().toString().isEmpty()){
                        content_list.remove(journal_item_position);
                        content_list.add(journal_item_position, journal_content.getText().toString().trim());
                    } else {
                        Toast.makeText(getApplicationContext(),"內容不能為空",Toast.LENGTH_SHORT).show();
                    }

                    if(imageName != null){
                        main_list.get(journal_item_position).setImageName(imageName);
                        journal_image.setImageResource(R.mipmap.ic_launcher);
                        imageName = null;
                    }
                    dbManager.change(journal_item_position,
                                    main_list.get(journal_item_position).getImageName(),
                                    journal_title.getText().toString().trim(),
                                    journal_content.getText().toString().trim());
                    System.out.println("Change");
                    dbManager.show();
                }
                dialog.dismiss();
                journal_list.setAdapter(journal_adapter);

            }

        });
        create_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        journal_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!photo.hasPermission(getApplicationContext())){
                    if(photo.needCheckPermission(journal.this)){
                        return;
                    }
                }
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });


    }


    public void initList(){

        dbManager = new journalDBManager(journal.this, markerName);
        journalDBManager.list list = dbManager.initList();
        main_list =  list.journal_list;
        content_list = list.content;
        journal_adapter = new JournalAdapter(this, R.layout.journal_item, main_list);
        journal_list.setAdapter(journal_adapter);
        System.out.println("InitList");
        dbManager.show();
    }

//    ---------------------------------methods below-----------------------------

//    private AdapterView.OnItemClickListener listitemclicklistener = (new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            titleshow.setText(listItems.get(position));
//            listItemIndex = position;
//
//            Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE, journalSQLiteHelper.CONTENT}, "_TITLE = ?", new String[]{titleshow.getText().toString().trim()}, null, null, null);
//            contentshow.setText(cursor.getString(cursor.getColumnIndex("_CONTENT")));
//
//            listItemClicked = true;
//            editnote.setEnabled(listItemClicked);
//            deletenote.setEnabled(listItemClicked);
//        }
//    });
//
//    //    新增遊記 Dialog 小視窗初始化
//    public void initAddJournalNote(){
//
//        //新增空白對話方塊，並設定方塊內容為"add_note.xml"
//        dialog = new Dialog(this);
//        dialog.setTitle("新增遊記");
//        dialog.setContentView(R.layout.journal_add_dialog);
//
//        title = (EditText)dialog.findViewById(R.id.dialognotetitle);
//        add = (Button)dialog.findViewById(R.id.addConfirmbtn);
//        cancel = (Button)dialog.findViewById(R.id.addCancelbtn);
//
//        title.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                add.setEnabled(!title.getText().toString().trim().isEmpty());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//    }
//
//    //    編輯按鈕
//    private View.OnClickListener editNoteclicklistener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
////            顯示"add_note.xml"
//            title.setText(titleshow.getText().toString().trim());
//            final String originTitle = title.getText().toString().trim();
//            dialog.show();
//            add.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{title.getText().toString().trim()}, null, null, null);
//                    if(cursor.getCount() >= 1 && !title.getText().toString().trim().equals(originTitle)){
//                        Toast.makeText(getApplicationContext(), "已經有相同的簡介標題", Toast.LENGTH_SHORT).show();
//                    }else{
//                        //跳轉頁面
//                        listItems.set(listItemIndex, title.getText().toString().trim());
//                        Intent intent = new Intent();
//                        intent.setClass(journal.this, journalContent.class);
//
//                        Bundle bundle = new Bundle();
//                        bundle.putString("title", title.getText().toString().trim());
//                        bundle.putString("Noteid", cursor.getString(cursor.getColumnIndex("_id")));
//                        bundle.putString("Mode", "edit");
//                        intent.putExtras(bundle);
//                        startActivity(intent);
//                        dialog.dismiss();
//                    }
//                }
//            });
//
//            cancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
//        }
//    };
//
//    //    刪除按鈕
//    private View.OnClickListener deleteNoteclicklistener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            alertDialog.setPositiveButton("是,刪除", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{titleshow.getText().toString().trim()}, null, null, null);
//                    dbManager.delete(cursor.getLong(cursor.getColumnIndex("_id")));
//                    listItems.remove(listItemIndex);
//                    listView.setAdapter(listAdapter);
//                    editnote.setEnabled(!listItems.isEmpty());
//                    deletenote.setEnabled(!listItems.isEmpty());
//                    titleshow.setText("");
//                    contentshow.setText("");
//                    listItemClicked = false;
//                }
//            });
//            alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    ;
//                }
//            });
//            alertDialog.show();
//        }
//    };
//
//    //    刪除全部按鈕
//    private View.OnClickListener deleteAllNoteclicklistener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            alertDialog.setPositiveButton("是,刪除", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{titleshow.getText().toString().trim()}, null, null, null);
//                    dbManager.deleteAll();
//                    listItems.clear();
//                    listView.setAdapter(listAdapter);
//                    editnote.setEnabled(!listItems.isEmpty());
//                    deletenote.setEnabled(!listItems.isEmpty());
//                    titleshow.setText("");
//                    contentshow.setText("");
//                    listItemClicked = false;
//                }
//            });
//            alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    ;
//                }
//            });
//            alertDialog.show();
//        }
//    };
//
//    //  新增按鈕
//    private View.OnClickListener addNoteclicklistener = new View.OnClickListener(){
//        @Override
//        public void onClick(View v){
//            dialog.show();
//            add.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View v){
//                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{title.getText().toString().trim()}, null, null, null);
//                    System.out.println(cursor.getCount());
//                    if(cursor.getCount()>0){
//                        Toast.makeText(getApplicationContext(), "已經有相同的簡介標題", Toast.LENGTH_SHORT).show();
//                    }else{
//                        //跳轉頁面
//                        listItems.add(title.getText().toString());
//                        Intent intent = new Intent();
//                        intent.setClass(journal.this, journalContent.class);
//
//                        Bundle bundle = new Bundle();
////                        -----------------------------NotSure
//                        bundle.putString("title", title.getText().toString().trim());
////                        bundle.putString("Noteid", cursor.getString(cursor.getColumnIndex("_id")));
//                        bundle.putString("Mode", "new");
//                        intent.putExtras(bundle);
//                        startActivity(intent);
//                    }
//
//                    dialog.dismiss();
//                }
//            });
//
//            cancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
//        }
//    };
//
//    //  初始化頁面時, 載入database內容到畫面
//    private void initLoadJournalList(){
//
//        Cursor cursor = dbManager.fetch();
//        if(cursor != null){//.fetch 裡面就有實作 " != null "
//            if(cursor.moveToFirst()){
//                do{
////                  載入到畫面的list
//                    listItems.add(cursor.getString(cursor.getColumnIndex("_TITLE")));
////                    listItemsContent.add();
//                }while(cursor.moveToNext());
//            }
//        }
//    }
//
//    public void initRemoveJoutnalNote(){
//        alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setTitle("Delete Note");
//        alertDialog.setTitle("Sure To Delete Note?");
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        System.out.println("Clicked");
//        if(item.getItemId() == android.R.id.home){
//            System.out.println("Finish");
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}
