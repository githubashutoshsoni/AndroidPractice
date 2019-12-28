package com.example.styleplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.styleplayer.Adapters.SongsAdapter;
import com.example.styleplayer.BroadCasts.MusicBroadCast;
import com.example.styleplayer.services.MusicService;
import com.example.styleplayer.services.MusicService.MusicBinder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static com.example.styleplayer.Constants.ACTION_PAUSE;

public class MainActivity extends AppCompatActivity {


    public static final int PERMISSION_REQUEST_CODE = 201;
    public static final int CREATE_FILE = 101;
    public static final int OPEN_FILE = 102;

    String[] appPerm = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

    ArrayList<Song> songList;

    @OnClick(R.id.stop_music)
    void stopMusic() {


        if (musicBound) {

            //todo do something interesting here

        }
    }

    @OnClick(R.id.resume)
    void resumeMusic() {

        if (musicBound) {

            musicService.play();


        }
    }

    @OnClick(R.id.pause)
    void pauseMusic() {


        if (musicBound) {

            musicService.pause();

        }
    }

    @BindView(R.id.song_list)
    ListView songView;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        Timber.d("I'm i at pos");
        Timber.d("I'm long at pos");
        songList = new ArrayList<>();


        if (checkAndRequest()) {


//            Intent intent = new Intent(MainActivity.this, MusicService.class);
//            startService(intent);


        }

        SongsAdapter songAdt = new SongsAdapter(this, songList);
        songView.setAdapter(songAdt);

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Timber.d("I'm i at pos %s", i);
                Timber.d("I'm long at pos %s", l);
                musicService.setSong(Integer.parseInt(view.getTag().toString()));
                musicService.playSong();
            }
        });

        getSongList();

//        for (int i = 0; i < 400; i++)
        showNotification("Hello", "I'm a description I can be really long ", 1);

        practice(null);
    }

    void showNotification(String title, String desc, int i) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelid = "channel_id";
        String channelName = "channel_name";

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelid, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelid)
                .setContentTitle(title)
                .setContentText(desc)
                .setSmallIcon(R.drawable.ic_launcher_background);

        manager.notify(i, builder.build());


    }


    @OnClick(R.id.stop_music)
    public void practice(View view) {

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "CURRENT_MUSIC";
        String channelName = "CURRENT_CHANNEL_NAME";

        Timber.d("I'm inside practice");


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

        }

//        set a pending intent when clicked will launch this activity...
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

//        set a pending intent to stop a music if its playing broadcast
        Intent stopMusicIntent= new Intent(this, MusicBroadCast.class);
        stopMusicIntent.setAction(ACTION_PAUSE);
        stopMusicIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID,0);



        PendingIntent stopMusicPendingIntent= PendingIntent.getBroadcast(this,1,stopMusicIntent,0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_error_red_14)
                .setContentTitle("Sad")
                .setColor(getResources().getColor(R.color.colorPrimaryDark, null))
                .setContentText("This is a long sad story")
                .setContentIntent(mainActivityPendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_stop_blue,"STOP",stopMusicPendingIntent)
                .setContentInfo("Content info: this is it");


        manager.notify(1, builder.build());

    }

    public void songPicked(View view) {
        Timber.d("songPicked");

        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MusicBinder binder = (MusicBinder) iBinder;
            musicService = binder.getService();
            musicBound = true;
            musicService.setList(songList);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);


        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }

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
