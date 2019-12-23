package com.example.styleplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.example.styleplayer.Constants.REQUEST_AUDIO;
import static com.example.styleplayer.Constants.REQUEST_STORAGE;

public class MainActivity extends AppCompatActivity {
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionManager = new PermissionManager(this);

        askForMultiplePermissions();

    }


    public void askForMultiplePermissions() {


        permissionManager.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionManager.PermissionAskListener() {
            @Override
            public void onNeedPermission() {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);

            }

            @Override
            public void onPermissionPreviouslyDenied() {

            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskAgain() {

            }

            @Override
            public void onPermissionGranted() {


//                ArrayList<HashMap<String, String>> playList = getPlayList();


                Log.d(TAG, "Key here is" + Environment.getExternalStorageDirectory().getAbsolutePath() + "  Valueis ");

//                for (int i = 0; i < playList.size(); i++) {
//
//
//                    Iterator iterator = playList.get(i).entrySet().iterator();
//                    while (iterator.hasNext()) {
//
//                        Map.Entry mapElement = (Map.Entry) iterator.next();
//                        String values = ((String) mapElement.getValue());
//
//
//                        Log.d(TAG, "Key here is" + mapElement.getKey() + "  Valueis " + values);
//
//                    }
//
//
////                    Log.d(TAG, "play list " + i + " " + playList.get(i).get());
//                }
            }


//                Log.d(TAG, "Path of the filed is" +)

//                Toast.makeText(getApplicationContext(), "Storage permission granted", Toast.LENGTH_LONG).show();

//            }
        });


        permissionManager.checkPermission(this, Manifest.permission.RECORD_AUDIO, new PermissionManager.PermissionAskListener() {
            @Override
            public void onNeedPermission() {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            }

            @Override
            public void onPermissionPreviouslyDenied() {

            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskAgain() {

            }

            @Override
            public void onPermissionGranted() {

            }
        });


    }

    String TAG = MainActivity.class.getSimpleName();

    ArrayList<HashMap<String, String>> getPlayList(String rootPath) {


        ArrayList<HashMap<String, String>> fileList = new ArrayList<>();


        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getPlayList(file.getAbsolutePath()) != null) {
                        fileList.addAll(getPlayList(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    HashMap<String, String> song = new HashMap<>();
                    song.put("file_path", file.getAbsolutePath());
                    song.put("file_name", file.getName());
                    fileList.add(song);
                }
            }
            return fileList;
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission was granted. Now you can call your method to open camera, fetch contact or whatever
//                    showAllList();
                } else {
                    // Permission was denied.......
                    // You can again ask for permission from here
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

}
