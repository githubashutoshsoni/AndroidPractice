package com.example.styleplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;

import static com.example.styleplayer.Constants.REQUEST_AUDIO;
import static com.example.styleplayer.Constants.REQUEST_STORAGE;

public class MainActivity extends AppCompatActivity {
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionManager = new PermissionManager(this);

        if (checkAndRequest()) {

            initApp();

        }


    }


    String[] appPerm = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

    public static final int PERMISSION_REQUEST_CODE = 201;


    boolean checkAndRequest() {

        List<String> listPermissionNeeded = new ArrayList<>();

        for (String perm : appPerm) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(perm);
            }
        }

        if (!listPermissionNeeded.isEmpty()) {


            ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }


        return true;
    }

    @OnClick(R.id.retry)
    void initApp() {

        String path = null;

        File sdCardRoot = Environment.getRootDirectory();

        File dir = new File(sdCardRoot.getAbsolutePath());

        if (dir.exists()) {

            if (dir.listFiles() != null) {
                for (File f : dir.listFiles()) {
                    if (f.isFile())
                        path = f.getName();

                    if (path.contains(".mp3")) {
                        Log.d(TAG, "path is" + path);

                    }
                }
            }
        }

    }

    String TAG = MainActivity.class.getSimpleName();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PERMISSION_REQUEST_CODE: {
                int deniedPermission = 0;
                HashMap<String, Integer> permissionResult = new HashMap<>();
                for (int i = 0; i < grantResults.length; i++) {

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                        permissionResult.put(permissions[i], grantResults[i]);
                        deniedPermission++;
                    }

                }

                if (deniedPermission == 0) {
                    initApp();
                } else {

                    for (Map.Entry<String, Integer> entry : permissionResult.entrySet()) {

                        String permName = entry.getKey();
                        Integer permResult = entry.getValue();

                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {

                            new MaterialAlertDialogBuilder(this).setTitle("Permission required " + permName).
                                    setIcon(R.drawable.ic_error_red_14)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            checkAndRequest();
                                        }
                                    }).show();

                        } else {


                            new MaterialAlertDialogBuilder(this).setTitle("Now grant permissions from the settings  you have to do that from the settings").
                                    setIcon(R.drawable.ic_error_red_14)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,

                                                    Uri.fromParts("package", getPackageName(), null));
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();

                                        }
                                    }).show();


                        }

                    }

                }


                break;
            }

        }
    }

}
