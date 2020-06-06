package com.example.navimap;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SwitchPage extends AppCompatActivity {

    private BottomNavigationView btmView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_page);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int ID = item.getItemId();
        if( ID == R.id.NotePageItem){

        }else if( ID == R.id.GoogleMapItem ){

        }else if( ID == R.id.JournalPageItem){

        }
        return super.onOptionsItemSelected(item);
    }

    public void viewInit(){

    }

    public void DBinit(){

    }
}
