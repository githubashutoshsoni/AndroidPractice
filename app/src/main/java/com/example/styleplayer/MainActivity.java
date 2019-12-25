package com.example.styleplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.styleplayer.services.MusicService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {


    public static final int PERMISSION_REQUEST_CODE = 201;
    public static final int CREATE_FILE = 101;
    public static final int OPEN_FILE = 102;

    String[] appPerm = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    MediaPlayer mediaPlayer;


    @OnClick(R.id.stop_music)
    void stopMusic() {
        mediaPlayer.stop();
    }

    @OnClick(R.id.resume)
    void resumeMusic() {

        mediaPlayer.start();
    }

    @OnClick(R.id.pause)
    void pauseMusic() {
        mediaPlayer.pause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mediaPlayer = new MediaPlayer();
        if (checkAndRequest()) {


            Intent intent = new Intent(MainActivity.this, MusicService.class);
            startService(intent);


        }


    }


    ArrayList<Song> videoList = new ArrayList<Song>();

    ArrayList<Song> soothingly() {


        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        String selection = MediaStore.Video.Media.DURATION +
                " >= ?";
        String[] selectionArgs = new String[]{

                String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))
        };
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

        try (Cursor cursor = getApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);

            Timber.d("%d is idcolumn and name column %d", idColumn, nameColumn);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                Timber.d("%d is idcolumn and name column %s", id, name);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                videoList.add(new Song(contentUri, name, duration, size));
            }
        }


        return videoList;
    }

    @OnClick(R.id.btn_retry)
    public void initApp() {

        Timber.d("init app started");

        String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();

        Uri uri = Uri.parse(sdCard);

//        createFile(uri);
        openFile(uri);

    }

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

    private void openFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/mpeg");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, OPEN_FILE);

    }

    private void createFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "invoice.pdf");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case OPEN_FILE:
                    Timber.d("Open file successfully  :)");
                    if ((data != null) && (data.getData() != null)) {

                        Timber.d("intent data is%s", data.getData().toString());

                        Uri uri = data.getData();
                        if (uri != null)
//                            startPlayingAudio();


                        Timber.d("mp3 is not corrupt");


                    }
                    break;
                case CREATE_FILE:
                    Timber.d("Create file successfully");
                    break;

            }


        }


        if (requestCode == CREATE_FILE) {

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


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
//                    initApp();
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
