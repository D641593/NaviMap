package com.example.navimap;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PhotoSave extends FragmentActivity {
    private static int id = 1;
    private final String PERMISSION_WRITE_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

//  SD權限
    boolean needCheckPermission(Activity activity) {
        //MarshMallow(API-23)之後要在 Runtime 詢問權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {PERMISSION_WRITE_STORAGE};
            int permsRequestCode = 200;
            ActivityCompat.requestPermissions(activity, perms, permsRequestCode);
            return true;
        }

        return false;
    }

    boolean hasPermission(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return(ActivityCompat.checkSelfPermission(context, PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200){
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(">>>", "取得授權，可以執行動作了");
                }
            }
        }
    }


    public String save(Bitmap bitmap, String pictureName){

        String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AppCameraPhoto";
        File filePath = new File(file);
        if (!filePath.exists()) {
            filePath.mkdir();
        }

        File finalImageFile = new File(filePath,  pictureName +"_"+ id + ".jpg");
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

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pictureName +"_"+ id++ + ".jpg";
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Bitmap getPhoto(String file)
    {
        try {
            String s = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AppCameraPhoto/";
            Bitmap bitmap = BitmapFactory.decodeFile(s + file);
            return bitmap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
