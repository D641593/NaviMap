package com.example.navimap.ui.journal_album;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navimap.PhotoSave;
import com.example.navimap.R;
import com.example.navimap.journal;

import java.io.File;
import java.util.ArrayList;


public class journal_album_adapter extends RecyclerView.Adapter<journal_album_adapter.ImageViewHolder> {

//    private int[] images;
    private File[] imagesPath = null;

//    public journal_album_adapter(int[] images){
//        this.images = images;
//    }
    public journal_album_adapter(File[] imagesPath){
        this.imagesPath = imagesPath;
        System.out.println("Im here" + imagesPath[0]);
    }
//    constructor(RecyclerView.Adapter) 根據內部 class 的 constructor(ImageViewHolder) 來創建
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_album_image_itemview, parent, false);
        ImageViewHolder imageViewHolder = new ImageViewHolder(view);

        return imageViewHolder;
    }

//    設定佈局(內容物)(我的圖片)
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
//        int images_id = images[position];
//        holder.journal_album_imageView.setImageResource(images_id);

        PhotoSave p = new PhotoSave();

        if(imagesPath[position].getPath()!=null){
            holder.journal_album_imageView.setBackground(null);
            Bitmap bitmap = p.getPhoto(imagesPath[position].getPath().substring(imagesPath[position].getPath().lastIndexOf("/") + 1, imagesPath[position].getPath().length()), journal.markerName);
            System.out.println("Bit :" + imagesPath[position].getPath().substring(imagesPath[position].getPath().lastIndexOf("/") + 1, imagesPath[position].getPath().length()));
            if(bitmap != null){
                holder.journal_album_imageView.setImageBitmap(bitmap);
            } else {
                holder.journal_album_imageView.setImageResource(R.mipmap.ic_launcher);
            }
        } else{
            holder.journal_album_imageView.setImageResource(R.mipmap.ic_launcher);
        }

//        if (item.getImageName() != null) {
//            imageView.setBackground(null);
//            Bitmap bitmap = p.getPhoto(item.getImageName(), journal.markerName);
//            if(bitmap != null){
//                imageView.setImageBitmap(bitmap);
//            } else {
//                imageView.setImageResource(R.mipmap.ic_launcher);
//            }
//
//        }
//        else {
//            imageView.setImageResource(R.mipmap.ic_launcher);
//        }

    }

    @Override
    public int getItemCount() {
        return imagesPath.length;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView journal_album_imageView;

        public ImageViewHolder(View itemView) {
            super (itemView);
            journal_album_imageView = itemView.findViewById(R.id.journal_album_image_item_element);
        }
    }

}
