package com.example.navimap;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoSave {
    public String save(Bitmap bitmap, File filePath , String pictureName, int id){

        File finalImageFile = new File(filePath,  pictureName + id + ".jpg");
        if (finalImageFile.exists()) {
            finalImageFile.delete();
        }
        try {
            finalImageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(finalImageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//
//        if (bitmap == null) {
////            Toast.makeText(this, "圖片不存在", Toast.LENGTH_SHORT).show();
////            return;
//        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        try {
            fos.flush();
            fos.close();
//            Toast.makeText(this, "圖片儲存在："+ finalImageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return finalImageFile.getAbsolutePath();
    }
}
