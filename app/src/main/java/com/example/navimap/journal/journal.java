package com.example.navimap.journal;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.navimap.R;
import com.example.navimap.journal.database.journalDBManager;
import com.example.navimap.main.EditPage;
import com.example.navimap.journal.journal_album.journal_album;
import com.example.navimap.map.MainActivity;
import com.example.navimap.note.notePage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class journal extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 0x1001;
    private static final int MY_CUSTOM_GALLERY = 020;

    // add_dialog.xml
    private Dialog dialog;
    private AlertDialog.Builder alertDialog;
    private Button create_journal, create_cancel;
    private EditText journal_title, journal_content;
    private ImageView journal_image;

    //main.xml
    private ListView journal_list;

    //暫存值
    private String imageName = null;
    public static String markerName = null;
    private Boolean is_item_change = false;
    private int journal_item_position = 0;
    private Intent intent = new Intent();

    //主要值
    private static List<Journal_list_item> main_list = new ArrayList<>();

    //用到的java
    private JournalAdapter journal_adapter;
    private PhotoSave photo= new PhotoSave();

    //DataBase
    private journalDBManager dbManager;

    //bottomNavigationBar
    private BottomNavigationView btmView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_main);
        intent = getIntent();
        markerName = intent.getStringExtra("Name");

        journal_list = findViewById(R.id.journalList);
        initList();
        initDia();
        getSupportActionBar().setTitle(markerName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        journal_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Journal_list_item item = main_list.get(position);
                changeDialog(item.getImageName(), item.getTitle(), item.getContent());
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

                        main_list.get(0).setId();
                        main_list.remove(position);
                        resetIndex(position);

                        dbManager.delete(position+1);
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
        btmView = findViewById(R.id.navigationBottomView);
        btmView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int ID = item.getItemId();
                if( ID == R.id.NotePageItem){
                    Intent intent = new Intent(journal.this, notePage.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Title",markerName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if( ID == R.id.GoogleMapItem ){
                    Intent intent = new Intent(journal.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Name",markerName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if( ID == R.id.JournalPageItem){
                    // Do nothing
                }
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.journal_navimenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        新增遊記
        if(item.getItemId() == R.id.journal_menu_newjournal_button) {
            is_item_change = false;
            imageName = null;
            journal_item_position = 0;
            initDia();
            diashow();
//        開啟相簿
        } else if(item.getItemId() == R.id.journal_menu_album_button){
            Intent intent = new Intent(journal.this, journal_album.class);
            Bundle bundle = new Bundle();
            bundle.putString("Name",markerName);
            bundle.putString("isShowOnly", "yes");
            intent.putExtras(bundle);
            startActivityForResult(intent,1);
//        拍照
        }else if(item.getItemId() == R.id.journal_menu_takepicture_button){
            if(!photo.hasPermission(getApplicationContext())){
                if(photo.needCheckPermission(journal.this)){
                    return true;
                }
            }
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,0);
        }else if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            main_list.get(0).resetID();
            main_list.clear();
            dbManager.close();
            return;
        } catch (Exception e){
            return;
        }
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
            journal_image.setImageBitmap(photo.getPhoto(imageName, markerName));
        } else {
            journal_image.setImageResource(R.mipmap.ic_launcher);
        }

    }
    private void initdeleteDialog(){
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("刪除遊記!");
        alertDialog.setMessage("你確定要刪除嗎?");
    }

    public void resetIndex(int position){
        for(int i=position ;i<main_list.size();i++){
            main_list.get(i).setItem_index();
        }
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
                        return;
                    }
                    if(!journal_content.getText().toString().isEmpty()){
                        tmp.setContent(journal_content.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(),"內容不能為空",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(imageName != null){
                        tmp.setImageName(imageName);
                        journal_image.setImageResource(R.mipmap.ic_launcher);
                    }

                    dbManager.create(tmp.getItem_index(),
                            tmp.getImageName(),
                            tmp.getTitle(),
                            tmp.getContent());
                    main_list.add(tmp);
                }
                else {
                    if(!journal_title.getText().toString().isEmpty()){
                        main_list.get(journal_item_position).setTitle(journal_title.getText().toString().trim());
                    } else {
                        Toast.makeText(getApplicationContext(),"主題不能為空",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!journal_content.getText().toString().isEmpty()){
                        main_list.get(journal_item_position).setContent(journal_content.getText().toString().trim());
                    } else {
                        Toast.makeText(getApplicationContext(),"內容不能為空",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(imageName != null){
                        main_list.get(journal_item_position).setImageName(imageName);
                        journal_image.setImageResource(R.mipmap.ic_launcher);
                        imageName = null;
                    }
                    dbManager.change(journal_item_position+1,
                                    main_list.get(journal_item_position).getImageName(),
                                    journal_title.getText().toString().trim(),
                                    journal_content.getText().toString().trim());
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

//        對話視窗..>點選圖片後
        journal_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(journal.this, journal_album.class);
                Bundle bundle = new Bundle();
                bundle.putString("Name",markerName);
                bundle.putString("isShowOnly","No");
                intent.putExtras(bundle);
                startActivityForResult(intent, MY_CUSTOM_GALLERY);
            }
        });

    }


    public void initList(){

        dbManager = new journalDBManager(journal.this, markerName);
        List<Journal_list_item> list = dbManager.initList();
        main_list = new ArrayList<>(list);
        journal_adapter = new JournalAdapter(this, R.layout.journal_item, main_list);
        journal_list.setAdapter(journal_adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == 0){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageName = photo.save(bitmap,markerName);
                journal_image.setBackground(null);
                journal_image.setImageBitmap(bitmap);
            }else if(requestCode == REQUEST_GALLERY) {
                Uri uri = data.getData();
                imageName = getRealFilePath(this, uri);
                journal_image.setImageURI(uri);
            }else if(requestCode == MY_CUSTOM_GALLERY) {
                PhotoSave p = new PhotoSave();
                if(data.getStringExtra("IsEmpty").equals("yes")){
                    Toast.makeText(getApplicationContext(), "當前遊記沒有照片",Toast.LENGTH_SHORT).show();
                    return;
                }
                Bitmap bitmap = p.getPhoto(data.getStringExtra("getJournal_album_photoName"), data.getStringExtra("album_result_markerName"));
                imageName = data.getStringExtra("getJournal_album_photoName");
                journal_image.setImageBitmap(bitmap);
            }else if(requestCode == 1){
                if(data.getStringExtra("IsEmpty").equals("yes")){
                    Toast.makeText(getApplicationContext(), "當前遊記沒有照片",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data.substring(data.lastIndexOf("/") + 1, data.length());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(journal.this, EditPage.class);
        startActivity(intent);

    }

    @Override
    public void onRestart(){
        super.onRestart();
        btmView.setSelectedItemId(R.id.JournalPageItem);
    }

}
