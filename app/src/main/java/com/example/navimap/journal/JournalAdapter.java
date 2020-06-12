package com.example.navimap.journal;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.navimap.R;

import java.util.List;

public class JournalAdapter extends ArrayAdapter<Journal_list_item> {
    public JournalAdapter(@NonNull Context context, int resource, List<Journal_list_item> list) {
        super(context, resource, list);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoSave p = new PhotoSave();
// 獲取老師的資料
        Journal_list_item item = getItem(position);

// 建立佈局
        View oneItemView = LayoutInflater.from(getContext()).inflate(R.layout.journal_item, parent, false);
// 獲取ImageView和TextView
        ImageView imageView = (ImageView) oneItemView.findViewById(R.id.image_show);
        TextView textView = (TextView) oneItemView.findViewById(R.id.title_text);
        TextView textView_journalcontent = (TextView) oneItemView.findViewById(R.id.content_text);
// 根據老師資料設定ImageView和TextView的展現
        if (item.getImageName() != null) {
            imageView.setBackground(null);
            Bitmap bitmap = p.getPhoto(item.getImageName(), journal.markerName);
            if(bitmap != null){
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.mipmap.ic_launcher);
            }

        }
        else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }

        textView.setText(item.getTitle());
        textView_journalcontent.setText(item.getContent());
        return oneItemView;
    }
}
