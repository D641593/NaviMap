package com.example.navimap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;

public class journal extends AppCompatActivity {

    private ListView listView;
    private ListAdapter listAdapter;
    private Dialog dialog;
    private TextView titleshow, contentshow;
    private EditText title;
    private AlertDialog.Builder alertDialog;
    private Button add, cancel;
    private Button editnote, deletenote, deleteAllnote;

    private journalDBManager dbManager = new journalDBManager(this);

    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayList<String> listItemsContent = new ArrayList<String>();
    private int listItemIndex = 0;
    private boolean listItemClicked = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_main);

        try{
            dbManager.open();
        }catch(Exception e){
            e.printStackTrace();
        }

        initLoadJournalList();

//        設定"add_note.xml"
        initAddJournalNote();
//        設定delete的警告對話方塊
        initRemoveJoutnalNote();

        titleshow = (TextView)findViewById(R.id.notetitleView);
        contentshow = (TextView) findViewById(R.id.notecontentView);

        editnote = (Button)findViewById(R.id.editNotebtn);
        editnote.setOnClickListener(editNoteclicklistener);
        editnote.setEnabled(listItemClicked && !listItems.isEmpty());

        deletenote = (Button)findViewById(R.id.deleteNotebtn);
        deletenote.setOnClickListener(deleteNoteclicklistener);
        deletenote.setEnabled(listItemClicked && !listItems.isEmpty());

        deleteAllnote = (Button)findViewById(R.id.deleteAllNotebtn);
        deleteAllnote.setOnClickListener(deleteAllNoteclicklistener);

        Button newNote = (Button)findViewById(R.id.addNotebtn);
        newNote.setOnClickListener(addNoteclicklistener);

        listView = (ListView)findViewById(R.id.journalList);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(listitemclicklistener);
    }
//    ---------------------------------methods below-----------------------------

    private AdapterView.OnItemClickListener listitemclicklistener = (new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            titleshow.setText(listItems.get(position));
            listItemIndex = position;

            Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE, journalSQLiteHelper.CONTENT}, "_TITLE = ?", new String[]{titleshow.getText().toString().trim()}, null, null, null);
            contentshow.setText(cursor.getString(cursor.getColumnIndex("_CONTENT")));

            listItemClicked = true;
            editnote.setEnabled(listItemClicked);
            deletenote.setEnabled(listItemClicked);
        }
    });

    //    新增遊記 Dialog 小視窗初始化
    public void initAddJournalNote(){

        //新增空白對話方塊，並設定方塊內容為"add_note.xml"
        dialog = new Dialog(this);
        dialog.setTitle("新增遊記");
        dialog.setContentView(R.layout.journal_add_dialog);

        title = (EditText)dialog.findViewById(R.id.dialognotetitle);
        add = (Button)dialog.findViewById(R.id.addConfirmbtn);
        cancel = (Button)dialog.findViewById(R.id.addCancelbtn);

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                add.setEnabled(!title.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //    編輯按鈕
    private View.OnClickListener editNoteclicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            顯示"add_note.xml"
            title.setText(titleshow.getText().toString().trim());
            final String originTitle = title.getText().toString().trim();
            dialog.show();
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{title.getText().toString().trim()}, null, null, null);
                    if(cursor.getCount() >= 1 && !title.getText().toString().trim().equals(originTitle)){
                        Toast.makeText(getApplicationContext(), "已經有相同的簡介標題", Toast.LENGTH_SHORT).show();
                    }else{
                        //跳轉頁面
                        listItems.set(listItemIndex, title.getText().toString().trim());
                        Intent intent = new Intent();
                        intent.setClass(journal.this, journalContent.class);

                        Bundle bundle = new Bundle();
                        bundle.putString("title", title.getText().toString().trim());
                        bundle.putString("Noteid", cursor.getString(cursor.getColumnIndex("_id")));
                        bundle.putString("Mode", "edit");
                        intent.putExtras(bundle);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    };

    //    刪除按鈕
    private View.OnClickListener deleteNoteclicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialog.setPositiveButton("是,刪除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{titleshow.getText().toString().trim()}, null, null, null);
                    dbManager.delete(cursor.getLong(cursor.getColumnIndex("_id")));
                    listItems.remove(listItemIndex);
                    listView.setAdapter(listAdapter);
                    editnote.setEnabled(!listItems.isEmpty());
                    deletenote.setEnabled(!listItems.isEmpty());
                    titleshow.setText("");
                    contentshow.setText("");
                    listItemClicked = false;
                }
            });
            alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });
            alertDialog.show();
        }
    };

    //    刪除全部按鈕
    private View.OnClickListener deleteAllNoteclicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialog.setPositiveButton("是,刪除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{titleshow.getText().toString().trim()}, null, null, null);
                    dbManager.deleteAll();
                    listItems.clear();
                    listView.setAdapter(listAdapter);
                    editnote.setEnabled(!listItems.isEmpty());
                    deletenote.setEnabled(!listItems.isEmpty());
                    titleshow.setText("");
                    contentshow.setText("");
                    listItemClicked = false;
                }
            });
            alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });
            alertDialog.show();
        }
    };

    //  新增按鈕
    private View.OnClickListener addNoteclicklistener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            dialog.show();
            add.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Cursor cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE}, "_TITLE = ?", new String[]{title.getText().toString().trim()}, null, null, null);
                    System.out.println(cursor.getCount());
                    if(cursor.getCount()>0){
                        Toast.makeText(getApplicationContext(), "已經有相同的簡介標題", Toast.LENGTH_SHORT).show();
                    }else{
                        //跳轉頁面
                        listItems.add(title.getText().toString());
                        Intent intent = new Intent();
                        intent.setClass(journal.this, journalContent.class);

                        Bundle bundle = new Bundle();
//                        -----------------------------NotSure
                        bundle.putString("title", title.getText().toString().trim());
//                        bundle.putString("Noteid", cursor.getString(cursor.getColumnIndex("_id")));
                        bundle.putString("Mode", "new");
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }

                    dialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    };

    //  初始化頁面時, 載入database內容到畫面
    private void initLoadJournalList(){

        Cursor cursor = dbManager.fetch();
        if(cursor != null){//.fetch 裡面就有實作 " != null "
            if(cursor.moveToFirst()){
                do{
//                  載入到畫面的list
                    listItems.add(cursor.getString(cursor.getColumnIndex("_TITLE")));
//                    listItemsContent.add();
                }while(cursor.moveToNext());
            }
        }
    }

    public void initRemoveJoutnalNote(){
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete Note");
        alertDialog.setTitle("Sure To Delete Note?");
    }
}
