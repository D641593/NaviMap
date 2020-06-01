package com.example.navimap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class journalContent extends AppCompatActivity {

    private Button doneEdit;
    private TextView editNoteText;

    private journalDBManager dbManager = new journalDBManager(this);
    private String DBnoteid = "";
    private String noteMode = "";
    private String DBtitle = "";

    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_note_content);

        Bundle bundle = this.getIntent().getExtras();
        DBtitle = bundle.getString("title");
        DBnoteid = bundle.getString("Noteid");
        noteMode = bundle.getString("Mode");

        doneEdit = (Button)findViewById(R.id.doneNoteContentbtn);
        doneEdit.setOnClickListener(doneEditclicklistener);

        editNoteText = (EditText)findViewById(R.id.editNoteView);

        try{
            dbManager.open();
        }catch(Exception e){
            e.printStackTrace();
        }

        if(noteMode.equals("edit")){
            cursor = dbManager.fetchAllArgs(journalSQLiteHelper._TableName, new String[]{journalSQLiteHelper._ID, journalSQLiteHelper.TITLE, journalSQLiteHelper.CONTENT}, "_id = ?", new String[]{DBnoteid}, null, null,null);
            editNoteText.setText(cursor.getString(cursor.getColumnIndex("_CONTENT")));
        }

    }

    private View.OnClickListener doneEditclicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(noteMode.equals("edit")){
                dbManager.update(cursor.getInt(cursor.getColumnIndex("_id")), DBtitle, editNoteText.getText().toString());
                noteMode = "none";
            }
            else if(noteMode.equals("new")){
                dbManager.insert(DBtitle, editNoteText.getText().toString());
                noteMode = "none";
            }
            else{
                Toast.makeText(getApplicationContext(), "寫入DataBase的模式有錯誤", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent();
            intent.setClass(journalContent.this, journal.class);
            startActivity(intent);
        }
    };
}
