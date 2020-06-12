package com.example.navimap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Build;
import android.os.Bundle;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.baoyz.swipemenulistview.BaseSwipListAdapter;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.List;



public class EditPage extends AppCompatActivity{
    private static int Id = 0;
    private static List<String> markerList = new ArrayList<String>();
    private AppAdapter mAdapter;
    private SwipeMenuListView mListView;
    private tinyDB DB;
    private noteDB notedb;
    private journalSQLiteHelper journaldb;
    private Button travel;
    private Dialog add_travel;
    private EditText travel_name;
    private Button travel_create, travel_cancel;
    private EditDB editDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editpage);
        DB = new tinyDB(this);
        notedb = new noteDB(this);
        editDB = new EditDB(this);
        CatchDB();

        getSupportActionBar().setTitle("旅遊企劃");
        travel = findViewById(R.id.create_travel);

        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        initDialog();
        mAdapter = new AppAdapter();
        mListView.setAdapter(mAdapter);
        travel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_travel.show();
                travel_create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!travel_name.getText().toString().isEmpty()){
                            if(storeDB(travel_name.getText().toString().trim())){
                                markerList.add(travel_name.getText().toString().trim());
                                mListView.setAdapter(mAdapter);
                                travel_name.setText("");
                                add_travel.dismiss();
                            }
                            return;
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"名稱不能為空",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                travel_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        travel_name.setText("");
                        add_travel.dismiss();
                        return;
                    }
                });
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // 创建“打开”项
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                openItem.setWidth(dp2px(90));
                openItem.setTitle("Open");
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(openItem);

                // 创建“打开”项
                SwipeMenuItem openforget = new SwipeMenuItem(getApplicationContext());
                openforget.setBackground(new ColorDrawable(Color.rgb(0xA5, 0xC9, 0xCE)));
                openforget.setWidth(dp2px(90));
                openforget.setTitle("Note");
                openforget.setTitleSize(18);
                openforget.setTitleColor(Color.WHITE);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(openforget);

                // 创建“删除”项
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(dp2px(90));
                deleteItem.setIcon(R.drawable.ic_delete);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(deleteItem);
            }


            private int dp2px(int value) {
                // 第一个参数为我们待转的数据的单位，此处为 dp（dip）
                // 第二个参数为我们待转的数据的值的大小
                // 第三个参数为此次转换使用的显示量度（Metrics），它提供屏幕显示密度（density）和缩放信息
                return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                        getResources().getDisplayMetrics());
            }
        };
        mListView.setMenuCreator(creator);



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = markerList.get(position);
                Intent intent = new Intent();
                intent.setClass(EditPage.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Name",title);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                //position:列表项的下标。如：0，1，2，3，4，...
                //index:菜单项的下标。如：0，1，2，3，4，...
                String item = markerList.get(position);
                switch (index) {
                    case 0:
                        // open
                        open(item);
                        break;
                    case 1:
                        NoteOpen(item);
                        break;
                    case 2:
                        // delete
                        try{
                            deleteDB(item);
                        } catch (Exception e){
                            System.out.println("Error: " + e.getMessage());
                        }
                        editDB.onDelete(editDB.getWritableDatabase(), item, position);
                        markerList.remove(position);
                        Id = markerList.size();
                        //通知监听者数据集发生改变，更新ListView界面
                        mListView.setAdapter(mAdapter);
                        break;

                }
                // true：其他已打开的列表项的菜单状态将保持原样，不会受到其他列表项的影响而自动收回
                // false:已打开的列表项的菜单将自动收回
                return false;
            }
        });
    }

    public void initDialog(){
        add_travel = new Dialog(this);
        add_travel.setTitle("新增旅行企劃");
        add_travel.setContentView(R.layout.dialoglayout);
        travel_name = add_travel.findViewById(R.id.title);
        travel_create = add_travel.findViewById(R.id.btn_add);
        travel_create.setText("新增旅行企劃");
        travel_cancel = add_travel.findViewById(R.id.btn_cancel);
    }

    private boolean storeDB(String title){
       SQLiteDatabase db = editDB.getWritableDatabase();
       Cursor c = db.rawQuery("select * from " + editDB.getTableName() + " where _title = '" + title + "';",null);
       if(!c.moveToFirst()){
           editDB.insert(title, db, Id);
           Id++;
           c.close();
           db.close();
           return true;
       }
       else{
           Toast.makeText(getApplicationContext(),"標題名稱不可重複",Toast.LENGTH_SHORT).show();
           c.close();
           db.close();
           return false;
       }

    }

    private void CatchDB(){
        SQLiteDatabase db = editDB.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("select * from " + editDB.getTableName() + ";", null);
            c.moveToFirst();
            System.out.println("catch");
            markerList.clear();
            while (!c.isAfterLast()) {
                System.out.println(c.getString(1));
                markerList.add(c.getString(1));
                c.moveToNext();
                Id++;
            }
            c.close();
            db.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            editDB.onCreate(db);
            db.close();
            System.out.println("Create");
        }
    }

    private void deleteDB(String title){
        SQLiteDatabase db = DB.getWritableDatabase();
        db.delete(DB.getTableName(),"_title = '"+title + "';",null);
        db.close();

        SQLiteDatabase ndb = notedb.getWritableDatabase();
        ndb.delete(notedb.getTableName(),"_title = '"+title + "';",null);
        ndb.close();

        journaldb = new journalSQLiteHelper(this, title);
        SQLiteDatabase jdb = journaldb.getWritableDatabase();
        jdb.execSQL("DROP TABLE IF EXISTS " + journaldb.get_TableName());
        jdb.close();



        //刪除時要改資料庫
    }

    private void open(String item) {
        Intent intent = new Intent();
        intent.setClass(EditPage.this, journal.class);
        Bundle bundle = new Bundle();
        bundle.putString("Name",item);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void NoteOpen(String name){
        Intent intent = new Intent();
        intent.setClass(EditPage.this, notePage.class);
        intent.putExtra("Title",name);
        startActivity(intent);
    }


    class AppAdapter extends BaseSwipListAdapter {
        @Override
        public int getCount() {
            return markerList.size();
        }

        @Override
        public String getItem(int position) {
            return markerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.markerattr, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            // 获取信息
            String item = getItem(position);
            // 加载图标
            holder.iv_icon.setBackground(getResources().getDrawable(R.drawable.ic_default));
            // 加载标题
            holder.tv_name.setText(item);
            return convertView;
        }

        class ViewHolder {
            ImageView iv_icon;
            TextView tv_name;

            //根据传进来的convertView创建ViewHolder，并且将其设置为convertView的Tag
            @RequiresApi(api = Build.VERSION_CODES.M)
            public ViewHolder(View view) {
                iv_icon = (ImageView) view.findViewById(R.id.image);
                tv_name = (TextView) view.findViewById(R.id.name);
                view.setTag(this);
            }
        }

        //这里我们可以根据列表项的位置来设置某项是否允许侧滑
        //(此处我们设置的是当下标为偶数项的时候不允许侧滑)
        @Override
        public boolean getSwipEnableByPosition(int position) {
            return true;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Id = 0;
        DB.close();
        editDB.close();
        notedb.close();
    }

}
