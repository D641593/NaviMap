package com.example.navimap;

import android.Manifest;
import android.app.Activity;
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

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private AppBarConfiguration mAppBarConfiguration;
    private GoogleMap mMap;
    private Dialog dialog;
    private EditText title;
    private Button add,cancel,time_add;
    private AlertDialog.Builder alertDialog;

//    GPS定位
    private LocationManager locationMgr;
    private String provider;
    private LatLng nowLocation;
    private Marker lastposiotion;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 11;

    private int menuLength;
    private NavigationView navigationView;
    private boolean writable = false;
    private boolean delete = false;
    private String deleteTmp;
    private int deleteID;
    private tinyDB DB;
    private noteDB notedb;
    private journalSQLiteHelper journaldb;
    private String dbTitle = "";
    private double lati,longi;
    private int dbID;
    private ImageButton btn_edit;
    private ImageButton btn_search;
    private int id;
    private SearchView searchView;
    private ArrayList<Marker> markers = new ArrayList<>();
    private DrawerLayout drawer;
    private BottomNavigationView btmView;
    private String journalName;
    private final int latiIndex = 3;
    private final int longIndex = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        journalName = getIntent().getStringExtra("Name");
        searchView = findViewById(R.id.sv_location);
        btmView = findViewById(R.id.navigationBottomView);
        btmView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int ID = item.getItemId();
                if( ID == R.id.NotePageItem){
                    Intent intent = new Intent(MainActivity.this,notePage.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Title",journalName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if( ID == R.id.GoogleMapItem ){
                    // Do nothing
                }else if( ID == R.id.JournalPageItem){
                    Intent intent = new Intent(MainActivity.this,journal.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Name",journalName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                return true;
            }
        });
        DB = new tinyDB(this);
        DB.onCreate(DB.getWritableDatabase());
        notedb = new noteDB(this);
        notedb.onCreate(notedb.getWritableDatabase());

        //DBshow();
        btn_search = findViewById(R.id.mostCloseLocationSearch);
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
                    if(nowLocation!=null){
                        lastposiotion.remove();
                        Toast.makeText(getApplicationContext(),"asd",Toast.LENGTH_SHORT).show();
                    }
                    nowLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    shortDistance();
                    BitmapDescriptor descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                    lastposiotion = mMap.addMarker(new MarkerOptions().position(nowLocation).title("所在位置").icon(descriptor));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(nowLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
                        getCurrentFocus().clearFocus();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(),"找不到位置",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
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

        drawer = findViewById(R.id.drawer_layout);
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
                    lati = c.getDouble(latiIndex);
                    longi = c.getDouble(longIndex);

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

    @Override
    protected void onRestart() {
        super.onRestart();
        initMenuAndMarker();
        drawer.closeDrawers();
        btmView.setSelectedItemId(R.id.GoogleMapItem);
    }

    private void shortDistance(){
        SQLiteDatabase db = DB.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + DB.getTableName() + ";",null);
        c.moveToFirst();
        float min = 0;
        String name = null;
        while (!c.isAfterLast()){

            lati = c.getDouble(latiIndex);
            longi = c.getDouble(longIndex);
            System.out.println(longi+ "\n" + lati);
            float[] result = new float[1];
            Location.distanceBetween(nowLocation.latitude, nowLocation.longitude, lati, longi, result);
            if(min > result[0] || min == 0){
                name = c.getString(1);
                min = result[0];
            }
            c.moveToNext();
        }
        Toast.makeText(getApplicationContext(),"目前最近的點為" + name , Toast.LENGTH_SHORT).show();
        c.close();
        db.close();

    }

    private void initMenuAndMarker(){
        Menu m = navigationView.getMenu();
        m.clear();
        for(int i=0;i<markers.size();i++){
            markers.get(i).remove();
        }
        markers.clear();
        SQLiteDatabase db = DB.getReadableDatabase();
        String SQLinst = "select * from " + DB.getTableName() + " where _title = '" + journalName + "';";
        Cursor c = db.rawQuery(SQLinst,null);
        if(c == null){
            System.out.println("c is null");
        }
        c.moveToFirst();
        while(!c.isAfterLast()){
            dbID = c.getInt(0);
            dbTitle = c.getString(2);
            lati = c.getDouble(latiIndex);
            longi = c.getDouble(longIndex);
            m.add(R.id.sideList,dbID,0,dbTitle).setIcon(R.drawable.ic_marker_location);
            System.out.println("add Menu item : " + dbTitle);
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(lati,longi)).title(dbTitle).draggable(true)));
            System.out.println("add Marker : " + dbTitle + " Lati : " + String.valueOf(lati) + " longi : " + String.valueOf(longi));
            c.moveToNext();
        }
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                SQLiteDatabase db = DB.getWritableDatabase();
                ContentValues markerValues = new ContentValues();
                System.out.println("marker" + marker.getTitle() + marker.getPosition().toString());
                markerValues.put("_title",journalName);
                markerValues.put("_markerName",marker.getTitle());
                markerValues.put("_latitude",marker.getPosition().latitude);
                markerValues.put("_longitude",marker.getPosition().longitude);
                String[] para = new String[]{marker.getId()};
                db.update(DB.getTableName(),markerValues,"_title = '" + marker.getTitle() + "';",null);
                db.close();
            }
        });
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
                lati = c.getDouble(latiIndex);
                longi = c.getDouble(longIndex);
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
                lati = c.getDouble(latiIndex);
                longi = c.getDouble(longIndex);
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


    private void DBadd(String Title,double latitude,double longitude){
        SQLiteDatabase db = DB.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put("_title",journalName);
        values.put("_markerName",Title);
        values.put("_latitude",latitude);
        values.put("_longitude",longitude);
        db.insert(DB.getTableName(),null,values);
        System.out.println("input col title is " + Title);
        db.close();
    }

    private void DBdelete(String title){
        SQLiteDatabase db = DB.getWritableDatabase();
        journaldb = new journalSQLiteHelper(this, title);
        SQLiteDatabase jdb = journaldb.getWritableDatabase();
        db.delete(DB.getTableName(),"_title = '" + journalName + "' and " + "_markerName = '"+title + "';",null);
        jdb.execSQL("DROP TABLE IF EXISTS " + journaldb.get_TableName());
        db.close();
        jdb.close();
        DBshow();
    }

    private int DBsearch(String title){
        SQLiteDatabase db = DB.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + DB.getTableName() + " where _title = '" + journalName + "' and " + "_markerName = '" + title + "';",null);
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
                            SQLiteDatabase db = DB.getReadableDatabase();
                            Cursor c = db.rawQuery("select * from " + DB.getTableName() + " where _title = '" + journalName + "' and " + "_markerName = '" + title.getText().toString() + "';",null);
                            if (!c.moveToFirst()){
                                googleMap.addMarker(new MarkerOptions().position(latLng).title(title.getText().toString()).draggable(true));
                                writable = true;
                                DBadd(title.getText().toString(), latLng.latitude, latLng.longitude);
                                supportInvalidateOptionsMenu();
                                dialog.dismiss();
                            }else{
                                Toast.makeText(getApplicationContext(),"標題名稱不可重複",Toast.LENGTH_SHORT).show();
                            }
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
