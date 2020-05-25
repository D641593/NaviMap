package com.example.navimap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLData;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private AppBarConfiguration mAppBarConfiguration;
    private GoogleMap mMap;
    private Dialog dialog;
    private EditText title;
    private Button add,cancel,time_add;
    private AlertDialog.Builder alertDialog;

//    Calendar dialog
    private Dialog calendar;
    private EditText year, month, day, hour, min, during;
    private Button calendar_add, calendar_cancel;
    private Spinner unit;
    private AlertDialog.Builder add_calendar_dialog;

//    GPS定位
    private LocationManager locationMgr;
    private String provider;
    private LatLng nowLocation;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 11;

    private int menuLength;
    private NavigationView navigationView;
    private boolean writable = false;
    private boolean delete = false;
    private String deleteTmp;
    private int deleteID;
    private tinyDB DB;
    private noteDB notedb;
    private String dbTitle = "";
    private double lati,longi;
    private int dbID;
    private ImageButton btn_edit;
    private ImageButton btn_search;
    private int id;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.sv_location);

        DB = new tinyDB(this);
        DB.onCreate(DB.getWritableDatabase());
        notedb = new noteDB(this);
        notedb.onCreate(notedb.getWritableDatabase());

        //DBshow();
        btn_edit = findViewById(R.id.edit);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,EditPage.class);
                startActivity(intent);
            }
        });
        btn_search = findViewById(R.id.search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provider = LocationManager.GPS_PROVIDER;
                locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                        // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }

                Location location = locationMgr.getLastKnownLocation(provider);
                if(location != null){
                    nowLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    Toast.makeText(getApplicationContext(),nowLocation.toString(),Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"定位中",Toast.LENGTH_SHORT).show();
                }
            }
        });

        System.out.println("DB create");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initAddDialog();
        initCancelDialog();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if (location !=null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location,1);
                        Address address = addressList.get(0);
                        LatLng latLng =  new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally{
                        Toast.makeText(getApplicationContext(),"找不到位置",Toast.LENGTH_SHORT).show();
                        return false;
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);

        floatingActionButtonSetting();

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_gallery,R.id.nav_home,R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        if (navigationView != null){
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    id = item.getItemId();
                    navigationView.getMenu().setGroupCheckable(R.id.sideList,false,true);
                    navigationView.getMenu().setGroupCheckable(R.id.sideList,true,true);
                    item.setChecked(true);
                    System.out.println("title is " + item.getTitle());
                    System.out.println("id is " + id);
                    SQLiteDatabase db = DB.getReadableDatabase();
                    Cursor c = db.rawQuery("select * from " + DB.getTableName() + " where _id = " + id + ";",null);
                    c.moveToFirst();
                    lati = c.getDouble(2);
                    longi = c.getDouble(3);
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lati,longi)).zoom(16).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    drawer.closeDrawer(GravityCompat.START);
                    c.close();
                    db.close();
                    return true;
                }
            });
        }
    }




    private void initMenuAndMarker(){
        Menu m = navigationView.getMenu();
        SQLiteDatabase db = DB.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + DB.getTableName() + ";",null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            dbID = c.getInt(0);
            dbTitle = c.getString(1);
            lati = c.getDouble(2);
            longi = c.getDouble(3);
            m.add(R.id.sideList,dbID,0,dbTitle).setIcon(R.drawable.ic_marker_location);
            System.out.println("add Menu item : " + dbTitle);
            mMap.addMarker(new MarkerOptions().position(new LatLng(lati,longi)).title(dbTitle));
            System.out.println("add Marker : " + dbTitle + " Lati : " + String.valueOf(lati) + " longi : " + String.valueOf(longi));
            c.moveToNext();
        }
        c.close();
        db.close();

    }

    private void floatingActionButtonSetting(){
        FloatingActionButton upbutton = findViewById(R.id.up);
        upbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu m = navigationView.getMenu();
                int i;
                MenuItem beChecked = m.getItem(0);
                for(i=0;i<m.size();i++){
                    if(m.getItem(i).isChecked()){
                        beChecked = m.getItem(i);
                        break;
                    }
                }
                beChecked.setChecked(false);
                i--;
                if(i < 0){
                    beChecked = m.getItem(m.size() - 1);
                }else{
                    beChecked = m.getItem(i);
                }
                id = beChecked.getItemId();
                beChecked.setChecked(true);
                System.out.println("title is " + beChecked.getTitle());
                System.out.println("id is " + id);
                SQLiteDatabase db = DB.getReadableDatabase();
                Cursor c = db.rawQuery("select * from " + DB.getTableName() + " where _id = " + id + ";",null);
                c.moveToFirst();
                lati = c.getDouble(2);
                longi = c.getDouble(3);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lati,longi)).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                c.close();
                db.close();
            }
        });

        FloatingActionButton downbutton = findViewById(R.id.down);
        downbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu m = navigationView.getMenu();
                int i;
                MenuItem beChecked = m.getItem(0);
                for(i=0;i<m.size();i++){
                    if(m.getItem(i).isChecked()){
                        beChecked = m.getItem(i);
                        break;
                    }
                }
                beChecked.setChecked(false);
                i++;
                if(i > m.size() - 1){
                    beChecked = m.getItem(0);
                }else{
                    beChecked = m.getItem(i);
                }
                id = beChecked.getItemId();
                beChecked.setChecked(true);
                System.out.println("title is " + beChecked.getTitle());
                System.out.println("id is " + id);
                SQLiteDatabase db = DB.getReadableDatabase();
                Cursor c = db.rawQuery("select * from " + DB.getTableName() + " where _id = " + id + ";",null);
                c.moveToFirst();
                lati = c.getDouble(2);
                longi = c.getDouble(3);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lati,longi)).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                c.close();
                db.close();
            }
        });
    }

    private void initAddDialog(){
        dialog = new Dialog(this);
        dialog.setTitle("新增標記點");
        dialog.setContentView(R.layout.dialoglayout);
        title = dialog.findViewById(R.id.title);
        add =  dialog.findViewById(R.id.btn_add);
        cancel = dialog.findViewById(R.id.btn_cancel);
    }

    private void initCancelDialog(){
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("刪除標記點!");
        alertDialog.setMessage("你確定要刪除嗎?");
    }

    private void init_alertCalendar_dialog(final String titleName){
        add_calendar_dialog = new AlertDialog.Builder(this);
        add_calendar_dialog.setTitle("時間提醒");
        add_calendar_dialog.setMessage("你想要設置時間提醒嗎?");
        add_calendar_dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
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
                                    case 0:
                                        y += monment;
                                        if( y >= now.get(Calendar.YEAR) + 100){
                                            Toast.makeText(getApplicationContext(),"你活不了這麼久",Toast.LENGTH_SHORT).show();
                                            clearEdit(during);
                                            return;
                                        }
                                        break;
                                    case 1:
                                        m += monment;
                                        break;
                                    case 2:
                                        d += monment;
                                        break;
                                    case 3:
                                        h += monment;
                                        break;

                                }

                                //建立事件結束時間
                                Calendar endTime = Calendar.getInstance();

                                endTime.set(y, m - 1, d, h, minute);

                                //建立 CalendarIntentHelper 實體
                                CalendarIntentHelper calIntent = new CalendarIntentHelper();
                                //設定值
                                calIntent.setTitle(titleName);
                                //                            calIntent.setDescription("事件內容描述");
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
        });
        add_calendar_dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
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

    private void DBadd(String Title,double latitude,double longitude){
        SQLiteDatabase db = DB.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put("_title",Title);
        values.put("_latitude",latitude);
        values.put("_longitude",longitude);
        db.insert(DB.getTableName(),null,values);
        System.out.println("input col title is " + Title);
        db.close();
    }

    private void DBdelete(String title){
        SQLiteDatabase db = DB.getWritableDatabase();
        SQLiteDatabase ndb = notedb.getWritableDatabase();
        db.delete(DB.getTableName(),"_title = '"+title + "';",null);
        ndb.delete(notedb.getTableName(),"_title = '"+title + "';",null);
        db.close();
        ndb.close();
        DBshow();

    }

    private int DBsearch(String title){
        SQLiteDatabase db = DB.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + DB.getTableName() + " where _title = '" + title + "';",null);
        if(c == null){
            return -1;
        }
        c.moveToFirst();
        int ans = c.getInt(0);
        c.close();
        db.close();
        return ans;
    }

    private int DBgetLastID(){
        SQLiteDatabase db = DB.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + DB.getTableName() + ";",null);
        assert c != null;
        if( c != null ){
            c.moveToLast();
            return c.getInt(0);
        }
        c.close();
        db.close();
        return -1;
    }

    private void DBshow(){
        SQLiteDatabase db = DB.getWritableDatabase();
        SQLiteDatabase ndb = notedb.getWritableDatabase();
        Cursor c = db.rawQuery("select * from " + DB.getTableName() + ";",null);
        Cursor nc = ndb.rawQuery("select * from " + notedb.getTableName() + ";",null);
        if (c == null || nc == null){
            System.out.println("C is null pointer");
        }
        c.moveToFirst();
        nc.moveToFirst();
        System.out.println("DataBase List");
        while (!c.isAfterLast()){
            System.out.println("title is " + c.getString(1));
            c.moveToNext();
        }
        while (!nc.isAfterLast()){
            System.out.println("title is " + nc.getString(1));
            System.out.println("content is " + nc.getString(2));
            nc.moveToNext();
        }
        c.close();
        nc.close();
        db.close();
        ndb.close();
    }

    private void initInfoWindowsClick(GoogleMap googleMap) {
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete = true;
                        deleteTmp = marker.getTitle();
                        System.out.println("deleteTmp is " + deleteTmp);
                        deleteID = DBsearch(deleteTmp);
                        System.out.println("deleteID is " + deleteID);
                        if(deleteID != -1) {
                            DBdelete(deleteTmp);
                            marker.remove();
                            supportInvalidateOptionsMenu();
                        }
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void initMapClick(final GoogleMap googleMap){
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                title.setText("");
                dialog.show();
                add.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if(title.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(),"can't empty",Toast.LENGTH_SHORT).show();
                        }else {
                            googleMap.addMarker(new MarkerOptions().position(latLng).title(title.getText().toString()));
                            writable = true;
                            DBadd(title.getText().toString(), latLng.latitude, latLng.longitude);
                            supportInvalidateOptionsMenu();
                            dialog.dismiss();
                            init_alertCalendar_dialog(title.getText().toString());
                            add_calendar_dialog.show();
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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        if(writable){
            Menu m = navigationView.getMenu();
            menuLength = DBgetLastID();
            System.out.println("menuLength : " + menuLength);
            if(menuLength != -1){
                m.add(R.id.sideList,menuLength,0,title.getText().toString()).setIcon(R.drawable.ic_marker_location);
            }
            writable = false;
        }
        if(delete){
            Menu m = navigationView.getMenu();
            m.removeItem(deleteID);
            delete = true;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng FengChan = new LatLng(24.179343, 120.649688);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(FengChan));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        initInfoWindowsClick(googleMap);
        initMapClick(googleMap);
        initMenuAndMarker();


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DB.close();
    }

}
