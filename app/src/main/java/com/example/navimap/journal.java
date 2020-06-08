package com.example.navimap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class journal extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 0x1001;
    private static final int REQUEST_GALLERY_SHOWONLY = 0x1010;

    // add_dialog.xml
    private Dialog dialog;
    private AlertDialog.Builder alertDialog;
    private Button create_journal, create_cancel;
    private EditText journal_title, journal_content;
    private ImageView journal_image;

    //main.xml
//    private Button create_start, back_btn;
    private ListView journal_list;

    //暫存值
    private String imageName = null;
    public static String markerName = null;
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

    //bottomNavigationBar
    private BottomNavigationView btmView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_main);
        intent = getIntent();
        markerName = intent.getStringExtra("Name");
//        Toast.makeText(getApplicationContext(),markerName,Toast.LENGTH_SHORT).show();

        journal_list = findViewById(R.id.journalList);
        initList();
        initDia();
        getSupportActionBar().setTitle(markerName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        back_btn = findViewById(R.id.BacKToMap);
//        back_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent.setClass(journal.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });

//        create_start = findViewById(R.id.create_start);
//        create_start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                is_item_change = false;
//                imageName = null;
//                journal_item_position = 0;
//                initDia();
//                diashow();
//            }
//        });
        journal_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Journal_list_item item = main_list.get(position);
                changeDialog(item.getImageName(), item.getTitle(), item.getContent());
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

//                        content_list.remove(position);

                        main_list.get(0).setId();
                        main_list.remove(position);
                        resetIndex(position);

                        dbManager.delete(position+1);
                        dbManager.show();
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
                    Intent intent = new Intent(journal.this,notePage.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Title",markerName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if( ID == R.id.GoogleMapItem ){
                    Intent intent = new Intent(journal.this,MainActivity.class);
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
            openGalleryShowOnly();
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
//            content_list.clear();
            dbManager.close();
            System.out.println("I am DESTROY!!!!!!!!!!!!!");
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
//        main_list.get(0).setId();
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


                    System.out.println("create" + journal_content.getText().toString().trim());
                    dbManager.create(tmp.getItem_index(),
                            tmp.getImageName(),
                            tmp.getTitle(),
                            tmp.getContent());
                    dbManager.show();
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

//                if(!photo.hasPermission(getApplicationContext())){
//                    if(photo.needCheckPermission(journal.this)){
//                        return;
//                    }
//                }
//                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent,0);
                openGallery();
            }
        });

    }


    public void initList(){

        dbManager = new journalDBManager(journal.this, markerName);
        List<Journal_list_item> list = dbManager.initList();
        main_list = new ArrayList<>(list);
//        content_list = new ArrayList<String>(list.content);
        journal_adapter = new JournalAdapter(this, R.layout.journal_item, main_list);
        journal_list.setAdapter(journal_adapter);
        System.out.println("InitList");

        dbManager.show();
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
            }else if(requestCode == REQUEST_GALLERY){
                Uri uri = data.getData();
                imageName = getPath(this, uri);
                journal_image.setImageURI(uri);
            }
        }
    }

//  透過 Uri 尋找該圖片的path
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getPath(Context context, Uri uri) {
        String filePath = "";

        if(DocumentsContract.isDocumentUri(context, uri)) {
//        Uri為 Document類型
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String[] column = {MediaStore.Images.Media.DATA};
            String selection = MediaStore.Images.Media._ID + "=?";

        //        SELECT column FROM MediaStore.Images.Media.EXTERNAL_CONTENT_URI WHERE selection
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, selection, new String[]{id}, null);

            if (cursor == null) {
                filePath = uri.getPath();
            } else {
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(cursor.getColumnIndex(column[0]));
                }
            }
            cursor.close();
        }else if(ContentResolver.SCHEME_FILE.equals(uri.getScheme())){
//        Uri為 file類型
            filePath = uri.getPath();
        }

        return filePath;
    }

    private void openGallery() {
        PhotoSave photoSave = new PhotoSave();
        if(!photoSave.hasPermission(getApplicationContext())) {
            if (photoSave.needCheckPermission(journal.this)){
                return;
            }
        }

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AppCameraPhoto";
        File sd = new File(filePath);
        Intent intent=new Intent(Intent.ACTION_PICK, Uri.fromFile(sd));
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }

    private void openGalleryShowOnly(){
        PhotoSave photoSave = new PhotoSave();
        if(!photoSave.hasPermission(getApplicationContext())) {
            if (photoSave.needCheckPermission(journal.this)){
                return;
            }
        }

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AppCameraPhoto";
        File sd = new File(filePath);
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.fromFile(sd));
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Select File"));
    }


}
