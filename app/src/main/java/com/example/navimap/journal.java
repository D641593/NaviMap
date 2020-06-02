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

//                    dbManager.create(tmp.getItem_index(),
//                                     tmp.getImageName(),
//                                     tmp.getTitle(),
//                                     journal_content.getText().toString().trim());
                    System.out.println("create" + journal_content.getText().toString().trim());
//                    dbManager.show();
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
//                    dbManager.change(journal_item_position,
//                                    main_list.get(journal_item_position).getImageName(),
//                                    journal_title.getText().toString().trim(),
//                                    journal_content.getText().toString().trim());
                    System.out.println("Change");
//                    dbManager.show();
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

//        dbManager = new journalDBManager(journal.this, markerName);
//        journalDBManager.list list = dbManager.initList();
//        main_list =  list.journal_list;
//        content_list = list.content;
        journal_adapter = new JournalAdapter(this, R.layout.journal_item, main_list);
        journal_list.setAdapter(journal_adapter);
//        System.out.println("InitList");
//        dbManager.show();
    }


}
